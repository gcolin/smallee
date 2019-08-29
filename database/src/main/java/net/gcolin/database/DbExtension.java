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
import java.util.logging.Level;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

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
					Db.LOG.log(Level.FINE, "directory created {0}", file);
				}
				File derbyProperties = new File(file, "derby.properties");
				try (InputStream in = Db.class.getClassLoader().getResourceAsStream("derby.properties")) {
					try (OutputStream out = new FileOutputStream(derbyProperties)) {
						Io.copy(in, out);
					}
				} catch (IOException ex) {
					Db.LOG.log(Level.SEVERE, "cannot create derby.properties", ex);
				}
			}
		}

		BasicDataSource s = new BasicDataSource();
		s.setDriverClassName(props.get("driver"));
		s.setUrl(props.get("url"));
		s.setUsername(props.get("user"));
		s.setPassword(props.get("password"));
		s.setMaxTotal(10);
		s.setMinIdle(0);
		env.bind(new DbDatasource(DbAdapter.ALL.get(props.get("type")), s));
		String testQuery = props.get("testQuery");
		
		try {
			new QueryRunner(s).query(testQuery, Db.GET_LONG);
		} catch (SQLException e1) {
			Db.LOG.log(Level.FINE, "test query fail: " + testQuery, e1);
			Db.LOG.warning("load database");
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
						Db.LOG.log(Level.SEVERE, "cannot shutdown derby cleanly", e);
					}
				}
			});
		}
		if (db.getSource() instanceof AutoCloseable) {
			try {
				((AutoCloseable) db.getSource()).close();
			} catch (Exception e) {
				Db.LOG.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		Db.VALIDATOR_FACTORY.close();
	}

}
