package net.gcolin.database.ext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
	private int[] filterTypes;
	private String where = "";
	private String order = "";
	@Inject
	private ConnectionManager connectionManager;

	public AbstractManager(Class<T> type, String listQuery, String addQuery, String updateQuery) {
		this.listQuery = listQuery;
		this.addQuery = addQuery;
		this.updateQuery = updateQuery;
		this.type = type;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void setFilter(String filter, Object[] filterArgument, int[] filterTypes) {
		this.filterArgument = filterArgument;
		this.filterTypes = filterTypes;
		where = filter;
	}

	public List<T> list() throws SQLException {
		return Db.query(connectionManager.getConnection(),
				listQuery + where + order + connectionManager.getAdapter().getOffsetlimit(), rs -> {
					List<T> list = new ArrayList<>();
					while (rs.next()) {
						list.add(convert(rs));
					}
					return list;
				}, getListArguements(connectionManager.getAdapter()), getListTypes(connectionManager.getAdapter()));
	}

	public Long count() throws SQLException {
		return Db.query(connectionManager.getConnection(),
				"select count(c.id) from " + type.getSimpleName() + " c " + where, Db.GET_LONG, getCountArguments(), getCountTypes());
	}

	public void load(Long id) throws SQLException {
		value = Db.query(connectionManager.getConnection(), listQuery + " where c.id = ?",
				rs -> rs.next() ? convert(rs) : null, new Object[] {id}, new int[] {Types.BIGINT});
	}

	public void load(String where, Object[] arguments, int[] types) throws SQLException {
		value = Db.query(connectionManager.getConnection(), listQuery + where,
				rs -> rs.next() ? convert(rs) : null, arguments, types);
	}

	protected abstract T convert(ResultSet rs) throws SQLException;

	protected Object[] createAddArguments() {
		throw new UnsupportedOperationException();
	}
	
	protected int[] createAddTypes() {
		throw new UnsupportedOperationException();
	}

	public void remove() throws SQLException {
		Db.update(connectionManager.getConnection(),
				"delete from " + type.getSimpleName() + " c where c.id=?", new Object[] {getValue().getId()}, new int[] {Types.BIGINT});
	}

	public void remove(String where, Object[] arguments, int[] types) throws SQLException {
		Db.update(connectionManager.getConnection(),
				"delete from " + type.getSimpleName() + " c " + where, arguments, types);
	}

	protected Object[] createUpdateArguments() {
		Object[] addArray = createAddArguments();
		Object[] r = new Object[addArray.length];
		System.arraycopy(addArray, 1, r, 0, addArray.length - 1);
		r[r.length - 1] = addArray[0];
		return r;
	}
	
	protected int[] createUpdateTypes() {
		int[] addArray = createAddTypes();
		int[] r = new int[addArray.length];
		System.arraycopy(addArray, 1, r, 0, addArray.length - 1);
		r[r.length - 1] = addArray[0];
		return r;
	}

	public void save() throws SQLException {
		if (value.getId() == null) {
			value.setId(Db.getNextVal(type.getSimpleName() + "seq", connectionManager.getConnection(),
					connectionManager.getAdapter()));
			Db.update(connectionManager.getConnection(), addQuery, createAddArguments(), createAddTypes());
		} else {
			Db.update(connectionManager.getConnection(), updateQuery, createUpdateArguments(), createUpdateTypes());
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
		if (filterArgument == null) {
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
		args[args.length - 2 + adapter.getOffsetIdx()] = start;
		args[args.length - 2 + adapter.getLimitIdx()] = length;
		return args;
	}
	
	private int[] getCountTypes() {
		if (filterTypes == null) {
			return new int[] {};
		}
		return filterTypes;
	}

	private int[] getListTypes(DbAdapter adapter) {
		int[] args = new int[2];
		if (this.filterTypes != null) {
			args = new int[filterTypes.length + 2];
			System.arraycopy(filterTypes, 0, args, 0, filterTypes.length);
		}
		args[args.length - 2 + adapter.getOffsetIdx()] = Types.INTEGER;
		args[args.length - 2 + adapter.getLimitIdx()] = Types.INTEGER;
		return args;
	}

}
