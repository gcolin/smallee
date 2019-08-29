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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import net.gcolin.common.io.Io;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class Db {

	public static final Logger LOG = Logger.getLogger("net.gcolin.database");
	public static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

	public static final ResultSetHandler<Long> GET_LONG = rs -> rs.next() ? rs.getLong(1) : 0;

	private Db() {
	}

	public static Long getNextVal(String seqName, Connection c, DbAdapter adapter) throws SQLException {
		return new QueryRunner().query(c, String.format(adapter.nextVal, seqName), GET_LONG);
	}
	
	public static Object[] getOffsetLimit(int start, int length, DbAdapter adapter) {
		if(adapter.offsetIdx == 0) {
			return new Object[] {start, length};
		} else {
			return new Object[] {length, start};
		}
	}

	public static void validate(Object o, Class<?>... groups) {
		Set<ConstraintViolation<Object>> violations = VALIDATOR_FACTORY.getValidator().validate(o, groups);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}

	public static void init(ClassLoader cl, String type, DataSource datasource) throws SQLException {
		Db.LOG.log(Level.INFO, "init database for database {0}", type);
		Connection conn = datasource.getConnection();
		conn.setAutoCommit(false);
		String current = null;
		try {
			Enumeration<URL> en = cl.getResources("db." + type + ".sql");
			List<String> all = new ArrayList<>();
			while (en.hasMoreElements()) {
				URL u = en.nextElement();
				Db.LOG.log(Level.INFO, "find sql file : {0}", u);
				try (InputStream in = u.openStream()) {
					all.add(Io.toString(in));
				}
			}
			Collections.sort(all,
					(o1, o2) -> o1.substring(0, o1.indexOf('\n')).compareTo(o2.substring(0, o2.indexOf('\n'))));

			for (String sql : all) {
				for (String part : sql.split(";")) {
					String trim = current = part.trim();
					if (!trim.isEmpty()) {
						new QueryRunner().update(conn, trim);
					}
				}
			}
			conn.commit();
		} catch (SQLException | IOException e) {
			conn.rollback();
			throw new DbException(current, e);
		} finally {
			DbUtils.close(conn);
		}
		Db.LOG.info("database initialized");
	}
	
	public static Map<String, String> getProperties(ClassLoader cl) {
		String path = System.getProperty("db");
		InputStream in = null;
		if (path != null) {
			try {
				in = new FileInputStream(path);
			} catch (FileNotFoundException e1) {
				throw new DbException("cannot open " + path, e1);
			}
		} else {
			in = Db.class.getClassLoader().getResourceAsStream("META-INF/db.properties");
		}
		if (in == null) {
			throw new DbException("missing file META-INF/db.properties");
		}
		Properties props = new Properties();
		try {
			props.load(in);
		} catch (IOException e) {
			throw new DbException("cannot load META-INF/db.properties", e);
		}
		Map<String, String> properties = new HashMap<>();
		for (Entry<Object, Object> e : props.entrySet()) {
			properties.put(e.getKey().toString(), e.getValue().toString());
		}
		return properties;
	}
}
