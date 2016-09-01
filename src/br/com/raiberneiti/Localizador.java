package br.com.raiberneiti;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Localizador <T extends Object> {
	
	private T registro;
	private boolean found;
	private Class<T> c;
	
	public Localizador( Class<T> c ) {
		this.c = c;
		found = false;
	}
	
	public boolean localiza( Object... id ) throws RaiberneitiException {

		found = false;
		
		Field[] fields = c.getDeclaredFields();
		StringBuilder sql = new StringBuilder("SELECT ");
		StringBuilder clause = new StringBuilder(" WHERE ");
		int keysNumber = 0;

		for (Field field : fields) {
			sql.append(field.getName());
			sql.append(",");

			if (field.isAnnotationPresent(PrimaryKey.class)) {
				clause.append(field.getName());
				clause.append("= ? and ");

				keysNumber++;
			}
		}

		if (keysNumber != id.length) {
			throw new RaiberneitiException("");
		}
		
		sql.delete(sql.length() - 1, sql.length());
		clause.delete(clause.length() - 5, clause.length());

		String tableName = c.getAnnotation(Table.class).name();

		sql.append(" FROM ");
		sql.append(tableName != null ? tableName : c.getSimpleName());
		sql.append(clause);

		executeStatement(sql, id);

		return isFound();
	}

	public boolean localizaPorAlternateKey( String alternateKeyName, Object... id ) throws RaiberneitiException {

		found = false;
		
		Field[] fields = c.getDeclaredFields();
		StringBuilder sql = new StringBuilder("SELECT ");
		StringBuilder clause = new StringBuilder(" WHERE ");
		int keysNumber = 0;

		for (Field field : fields) {
			sql.append(field.getName());
			sql.append(",");

			if(!field.isAnnotationPresent(AlternateKey.class))
			{
				continue;
			}
			
			boolean alternateKey = field.getAnnotation(AlternateKey.class).keyName().equals(alternateKeyName);

			if (alternateKey) {
				clause.append(field.getName());
				clause.append("= ? and ");

				keysNumber++;
			}
		}

		if (keysNumber != id.length) {
			throw new RaiberneitiException("");
		}
		
		sql.delete(sql.length() - 1, sql.length());
		clause.delete(clause.length() - 5, clause.length());

		String tableName = c.getAnnotation(Table.class).name();

		sql.append(" FROM ");
		sql.append(tableName != null ? tableName : c.getSimpleName());
		sql.append(clause);

		executeStatement(sql, id);

		return isFound();
	}
	
	public List<T> localiza() throws RaiberneitiException {
		return localiza( (String) null, (Object[]) null );
	}
	        
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao ) throws RaiberneitiException {
		return localiza( clausulaWhere, camposSelecao, null, null, null );
	}
	
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao, String orderBy ) throws RaiberneitiException {
		return localiza( clausulaWhere, camposSelecao, orderBy, null, null );
	}
	
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao, String orderBy, Integer limit ) throws RaiberneitiException {
		return localiza( clausulaWhere, camposSelecao, orderBy, limit, null );
	}
	
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao, Integer limit ) throws RaiberneitiException {
		return localiza( clausulaWhere, camposSelecao, null, limit, null );
	}
	
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao, Integer limit, Integer offSet ) throws RaiberneitiException {
		return localiza( clausulaWhere, camposSelecao, null, limit, offSet );
	}
	
	public List<T> localiza( String clausulaWhere, Object[] camposSelecao, String orderBy, Integer limit, Integer offSet ) throws RaiberneitiException {

		Field[] fields = c.getDeclaredFields();
		StringBuilder sql = new StringBuilder("SELECT ");

		for (Field field : fields) {
			ColumnAlias columnAlias = field.getAnnotation(ColumnAlias.class);
			ColumnName columnName = field.getAnnotation(ColumnName.class);

			if (columnAlias != null) {
					sql.append(columnAlias.value());
					sql.append(".");
			}

			sql.append(columnName != null ? columnName.value() : field.getName());
			sql.append(",");
		}
		
		sql.delete(sql.length() - 1, sql.length());

		Table tableName = c.getAnnotation(Table.class);
		sql.append(" FROM ");

		if (tableName != null) {
			sql.append(tableName.name());

			if (tableName.alias().trim().length() > 0) {
				sql.append(" ");
				sql.append(tableName.alias());
			}

		} else {
			sql.append(c.getSimpleName());
		}

		List<JoinRule> joins = getInclude();

		if (joins != null) {
			include(joins, sql);
		}

		if (clausulaWhere != null) {
			String clause = clausulaWhere.trim();

			if( !clause.toLowerCase().startsWith("WHERE ")) {
				sql.append(" WHERE ");
			}

			sql.append(clause);
		}

		if (orderBy != null) {
			sql.append(" ORDER BY ");
			sql.append(orderBy);
		}

		if (limit != null) {
			sql.append(" LIMIT ");
			sql.append(limit);
		}

		if (offSet != null) {
			sql.append(" OFFSET ");
			sql.append(offSet);
		}

		return executeStatementLocator(sql, camposSelecao);
	}

	private List<T> executeStatementLocator(StringBuilder sql, Object[] fields) throws RaiberneitiException {
		ArrayList<T> resultList = new ArrayList<T>();
		Conexao connection = PoolDeConexoes.getConexao();

		try {
			PreparedStatement statement = connection.getPreparedStatement(sql.toString());
			
			for (int i = 0; i < fields.length; i++) {
				statement.setObject(i + 1, fields[i]);
			}
			
			ResultSet result = statement.executeQuery();
			
			if (result.next()) {

				while (!result.isAfterLast()) {
					registro = c.newInstance();

					setRegistro(registro, result);
					
					resultList.add(registro);
					result.next();
				}
			}

		} catch (Exception e) {
			throw new RaiberneitiException(e);
		} finally {
			connection.libera();
		}

		return resultList;
	}

	private void executeStatement(StringBuilder sql, Object... keys) throws RaiberneitiException {
		Conexao connection = PoolDeConexoes.getConexao();

		try {
			PreparedStatement statement = connection.getPreparedStatement(sql.toString());
			
			for (int i = 0; i < keys.length; i++) {
				statement.setObject(i + 1, keys[i]);
			}
			
			ResultSet result = statement.executeQuery();
			
			if (result.next()) {
				registro = c.newInstance();
				setRegistro(registro, result);
				found = true;
			}

		} catch (Exception e) {
			throw new RaiberneitiException(e);
		} finally {
			connection.libera();
		}
	}

	private void setRegistro(T registro, ResultSet result) throws SQLException, RaiberneitiException {
		Field[] fields = c.getDeclaredFields();
		int columnIndex = 1;
		
		for (Field field : fields) {
			String methodName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
			Object column = result.getObject(columnIndex++);
			
			if(column != null) {
				
				try {
					Method method = registro.getClass().getDeclaredMethod(methodName, field.getType());
					
					if (column.getClass().getSimpleName().equals("BigDecimal")) {
						method.invoke(registro, ((BigDecimal) column).doubleValue()); 
					} else if(column.getClass().getSimpleName().equals("Timestamp") && field.getType().getSimpleName().equals("Date")) {
						method.invoke( registro, new Date(((Timestamp) column).getTime()));
					} else {
						method.invoke(registro, column);
					}

				} catch(Exception e) {
					throw new RaiberneitiException(e);
				}
			}
		}
	}
	private void include(List<JoinRule> includes, StringBuilder sql) {
		for (JoinRule include : includes) {
			sql.append(" ");

			switch (include.type()) {
				case FULL: sql.append("FULL"); break;
				case INNER: sql.append("INNER"); break;
				case LEFT: sql.append("LEFT"); break;
				case RIGHT: sql.append("RIGHT"); break;
			}

			sql.append(" JOIN ");
			sql.append(include.tableName());

			if (!include.alias().trim().equals(" ")) {
				sql.append(" ");
				sql.append(include.alias());
			}

			sql.append(" on ");
			sql.append(include.condition());
		}
	}

	private List<JoinRule> getInclude() {

		List<JoinRule> includes = new ArrayList<JoinRule>();
		JoinRule rule = c.getAnnotation(JoinRule.class);

		if (rule != null) {
			includes.add(rule);
		}

		JoinList includeList = c.getAnnotation(JoinList.class);

		if (includeList != null) {
			for (JoinRule include : includeList.value()) {
				includes.add(include);
			}
		}

		return includes;
	}

	public boolean isFound() {
		return found;
	}
	
	public T getRegistro() {
		return registro;
	}
	
}