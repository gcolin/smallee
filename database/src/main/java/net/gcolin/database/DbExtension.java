/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import net.gcolin.common.io.Io;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class DbExtension implements Extension {

	@Override
	public void doStart(Environment env) {
		Map<String, String> props = Db.getProperties(env.getClassLoader());

		String dbPath = props.get("path");
		if (dbPath != null) {
			System.setProperty("derby.system.home", dbPath);
			File file = new File(dbPath);
			if (!file.exists()) {
				if (file.mkdirs()) {
					Db.LOG.debug("directory created {}", file);
				}
				File derbyProperties = new File(file, "derby.properties");
				try (InputStream in = Db.class.getClassLoader().getResourceAsStream("derby.properties")) {
					try (OutputStream out = new FileOutputStream(derbyProperties)) {
						Io.copy(in, out);
					}
				} catch (IOException ex) {
					Db.LOG.error("cannot create derby.properties", ex);
				}
			}
		}

		BasicDataSource s = new BasicDataSource();
		s.setDriverClassName(props.get("driver"));
		s.setUrl(props.get("url"));
		s.setUsername(props.get("user"));
		s.setPassword(props.get("password"));
		String max = props.get("maxPoolSize");
		if (max == null) {
			s.setMaxTotal(10);
		} else {
			s.setMaxTotal(Integer.parseInt(max.trim()));
		}
		String min = props.get("minPoolSize");
		if (min == null) {
			s.setMinIdle(0);
		} else {
			s.setMinIdle(Integer.parseInt(min.trim()));
		}
		env.bind(new DbDatasource(DbAdapter.ALL.get(props.get("type")), s));
		String testQuery = props.get("testQuery");
		
		try {
			Db.query(s, testQuery, Db.GET_LONG);
		} catch (SQLException e1) {
			if(Db.LOG.isDebugEnabled()) {
				Db.LOG.debug("test query fail: " + testQuery, e1);
			}
			Db.LOG.warn("load database");
			try {
				Db.init(env.getClassLoader(), props.get("type"), s);
			} catch (SQLException e) {
				throw new DbException(e);
			}
		}
	}

	@Override
	public void doStopped(Environment environment) {
		DbDatasource db = environment.get(DbDatasource.class);
		if (db.getAdapter() == DbAdapter.DERBY) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						Db.LOG.info("shutdown derby");
						DriverManager.getConnection("jdbc:derby:;shutdown=true").close();
					} catch (SQLException e) {
						Db.LOG.error("cannot shutdown derby cleanly", e);
					}
				}
			});
		}
		if (db.getSource() instanceof AutoCloseable) {
			try {
				((AutoCloseable) db.getSource()).close();
			} catch (Exception e) {
				Db.LOG.error(e.getMessage(), e);
			}
		}
		Db.VALIDATOR_FACTORY.close();
	}

}
