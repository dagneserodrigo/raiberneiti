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
		StringBuilder clause = new StringBuilder("WHERE ");

		for (Field field : fields) {
			sql.append(field.getName());
			sql.append(",");

			if (field.isAnnotationPresent(PrimaryKey.class)) {
				clause.append(field.getName());
				clause.append("= ? and ");
			}
		}
		
		sql.delete(sql.length() - 1, sql.length());
		clause.delete(clause.length() - 5, clause.length());

		String tableName = c.getAnnotation(Table.class).name();

		sql.append(" FROM ");
		sql.append(tableName != null ? tableName : c.getSimpleName());
		sql.append(clause);

		executeStatement(sql.toString(), id);

		return isFound();
	}

	public boolean localizaPorAlternateKey( String alternateKeyName, Object... id ) throws RaiberneitiException {

		// este é o método que busca um cliente baseado na chave alternativa indicada que você deverá preencher

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
		
		ArrayList<T> result = new ArrayList<T>();
		
		// Este é o método que retorna um list de registros que vc deverá preencher
		
		return result;
	}

	private void executeStatement(String sql, Object... keys) throws RaiberneitiException {
		Conexao connection = PoolDeConexoes.getConexao();

		try {
			PreparedStatement statement = connection.getPreparedStatement(sql);
			
			for (int i = 0; i > keys.length; i++) {
				statement.setObject(i, keys[i]);
			}
			
			ResultSet result = statement.executeQuery();
			
			if (result.next()) {
				setRegistro(c.newInstance(), result);
				found = true;
			}

		} catch (Exception e) {
			throw new RaiberneitiException(e);
		} finally {
			connection.libera();
		}
	}

	private void setRegistro(T registro, ResultSet result) throws SQLException {
		Field[] fields = c.getDeclaredFields();
		int columnIndex = 1;
		
		for (Field field : fields) {
			String methodName = "set" + Character.toUpperCase( field.getName().charAt( 0 ) ) + field.getName().substring( 1 );
			Object column = result.getObject( columnIndex++ );
			
			if( column != null ) {
				
				try {
					Method method = registro.getClass().getDeclaredMethod( methodName, field.getType() );
					
					try {
						method.invoke(registro, column);
					
					} catch(Exception e) {

						if (column.getClass().getSimpleName().equals("BigDecimal")) {
							method.invoke(registro, ((BigDecimal) column).doubleValue() ); 
						} else if( column.getClass().getSimpleName().equals( "Timestamp" ) && field.getType().getSimpleName().equals( "Date" ) ) {
							method.invoke( registro, new Date( ((Timestamp) column).getTime() ) );
						}
					}

				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isFound() {
		return found;
	}
	
	public T getRegistro() {
		return registro;
	}
	
}