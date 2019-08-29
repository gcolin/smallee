package net.gcolin.database.ext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.dbutils.QueryRunner;

import net.gcolin.database.Db;
import net.gcolin.database.DbAdapter;

public abstract class AbstractManager<T extends HasKey> {

	private int start = 0;
	private int length = 20;
	private String url;
	private T value;
	private Class<T> type;
	private String listQuery;
	private String addQuery;
	private String updateQuery;
	private Object[] filterArgument;
	private String where = "";
	@Inject private ConnectionManager connectionManager;

	public AbstractManager(Class<T> type, String listQuery, String addQuery, String updateQuery) {
		this.listQuery = listQuery;
		this.addQuery = addQuery;
		this.updateQuery = updateQuery;
		this.type = type;
	}

	public void setFilter(String filter, Object... filterArgument) {
		this.filterArgument = filterArgument;
		where = " where " + filter;
	}

	public List<T> list() throws SQLException {
		return new QueryRunner().query(connectionManager.getConnection(),
				listQuery + where + connectionManager.getAdapter().offsetlimit, rs -> {
					List<T> list = new ArrayList<>();
					while (rs.next()) {
						list.add(convert(rs));
					}
					return list;
				}, getListArguements(connectionManager.getAdapter()));
	}

	public Long count() throws SQLException {
		return new QueryRunner().query(connectionManager.getConnection(),
				"select count(id) from " + type.getSimpleName() + where, Db.GET_LONG, getCountArguments());
	}

	public void load(Long id) throws SQLException {
		value = new QueryRunner().query(connectionManager.getConnection(), listQuery + " where c.id = ?",
				rs -> rs.next() ? convert(rs) : null, id);
	}

	public void load(String where, Object... arguments) throws SQLException {
		value = new QueryRunner().query(connectionManager.getConnection(), listQuery + where,
				rs -> rs.next() ? convert(rs) : null, arguments);
	}

	protected abstract T convert(ResultSet rs) throws SQLException;

	protected Object[] createAddArguments() {
		throw new UnsupportedOperationException();
	}

	public void remove() throws SQLException {
		new QueryRunner().update(connectionManager.getConnection(),
				"delete from " + type.getSimpleName() + " c where c.id=?", getValue().getId());
	}
	
	public void remove(String where, Object...arguments) throws SQLException {
		new QueryRunner().update(connectionManager.getConnection(),
				"delete from " + type.getSimpleName() + " c " + where, arguments);
	}

	protected Object[] createUpdateArguments() {
		Object[] addArray = createAddArguments();
		Object[] r = new Object[addArray.length];
		System.arraycopy(addArray, 1, r, 0, addArray.length - 1);
		r[r.length - 1] = addArray[0];
		return r;
	}

	public void save() throws SQLException {
		if (value.getId() == null) {
			value.setId(Db.getNextVal(type.getSimpleName() + "seq", connectionManager.getConnection(),
					connectionManager.getAdapter()));
			new QueryRunner().update(connectionManager.getConnection(), addQuery, createAddArguments());
		} else {
			new QueryRunner().update(connectionManager.getConnection(), updateQuery, createUpdateArguments());
		}
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public int getStart() {
		return start;
	}

	public int getLength() {
		return length;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	private Object[] getCountArguments() {
		if(filterArgument == null) {
			return new Object[] {};
		}
		return filterArgument;
	}

	private Object[] getListArguements(DbAdapter adapter) {
		Object[] args = new Object[2];
		if (this.filterArgument != null) {
			args = new Object[filterArgument.length + 2];
			System.arraycopy(filterArgument, 0, args, 0, filterArgument.length);
		}
		args[args.length - 2 + adapter.offsetIdx] = start;
		args[args.length - 2 + adapter.limitIdx] = length;
		return args;
	}

}
