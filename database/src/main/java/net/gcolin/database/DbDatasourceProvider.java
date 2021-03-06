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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.dbcp2.BasicDataSource;

import net.gcolin.common.io.Io;
import net.gcolin.common.lang.Strings;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.config.Config;
import net.gcolin.di.atinject.producer.Produces;
import net.gcolin.di.atinject.web.ApplicationScoped;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
@ApplicationScoped
public class DbDatasourceProvider {

	@Config(name = "db.path")
	private String dbPath;
	@Config(name = "db.type")
	private String type;
	@Config(name = "db.driver")
	private String driver;
	@Config(name = "db.url")
	private String url;
	@Config(name = "db.user")
	private String user;
	@Config(name = "db.password")
	private String password;
	@Config(name = "db.maxPoolSize", defaultValue = "10")
	private int maxPoolSize;
	@Config(name = "db.minPoolSize", defaultValue = "0")
	private int minPoolSize;
	@Config(name = "db.testQuery")
	private String testQuery;
	@Inject
	private Environment env;
	
	private DbDatasource datasource;
	
	@Produces
	public DbDatasource getDbDatasource() {
		return datasource;
	}
	
	@PostConstruct
	void init() {
		if (!Strings.isNullOrEmpty(dbPath)) {
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
		s.setDriverClassName(driver);
		s.setUrl(url);
		s.setUsername(user);
		s.setPassword(password);
		s.setMaxTotal(maxPoolSize);
		s.setMinIdle(minPoolSize);
		
		try {
			Db.query(s, testQuery, Db.GET_LONG);
		} catch (SQLException e1) {
			if(Db.LOG.isDebugEnabled()) {
				Db.LOG.debug("test query fail: " + testQuery, e1);
			}
			Db.LOG.info("load database");
			try {
				Db.init(env.getClassLoader(), type, s);
			} catch (SQLException e) {
				throw new DbException(e);
			}
		}
		
		datasource = new DbDatasource(DbAdapter.ALL.get(type), s);
	}
	
	
	@PreDestroy
	void doStopped() {
		if (datasource.getAdapter() == DbAdapter.DERBY) {
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
		if (datasource.getSource() instanceof AutoCloseable) {
			try {
				((AutoCloseable) datasource.getSource()).close();
			} catch (Exception e) {
				Db.LOG.error(e.getMessage(), e);
			}
		}
		Db.VALIDATOR_FACTORY.close();
	}

}
