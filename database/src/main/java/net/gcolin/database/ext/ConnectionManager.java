package net.gcolin.database.ext;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import net.gcolin.common.io.Io;
import net.gcolin.database.DbAdapter;
import net.gcolin.database.DbDatasource;
import net.gcolin.di.atinject.web.RequestScoped;

@RequestScoped
public class ConnectionManager {

	private Connection connection;
	private DbAdapter adapter;
	
	@Inject DbDatasource db;
	
	@PostConstruct
	void init() throws SQLException {
		adapter = db.getAdapter();
		connection = db.getSource().getConnection();
		connection.setAutoCommit(true);
	}
	
	@PreDestroy
	void end() {
		Io.close(connection);
	}
	
	public DbAdapter getAdapter() {
		return adapter;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
}
