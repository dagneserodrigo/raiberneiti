package br.com.raiberneiti;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Persistor {

	private Object tbl;
	private int affectedRows;


	public Persistor( Object tbl ) throws RaiberneitiException {
		this.tbl = tbl;
	}

	public void insere() throws RaiberneitiException {
		Conexao connection = PoolDeConexoes.getConexao();
		
		try 
		{
			StringBuilder cmdSql = new StringBuilder();
			StringBuilder parameters = new StringBuilder();
			
			String tableName = tbl.getClass().getSimpleName();
			Table alternativeTableName = tbl.getClass().getAnnotation(Table.class);
			
			if(alternativeTableName != null)
			{
				tableName = alternativeTableName.name();
			}
			
			cmdSql.append("INSERT INTO " + tableName + " (");
			
			Field[] fields = tbl.getClass().getDeclaredFields();
			Object[] values = new Object[ fields.length ];
			ArrayList<Object[]> autoIncrements = null;
			int counter = 0;
			
			for(Field field : fields)
			{
				if(!field.isAnnotationPresent(AutoIncrement.class))
				{
					Object value = getFieldValue(field.getName());
					
					cmdSql.append( field.getName() + ", " );
					parameters.append((value != null ? "? " : "null") + ", ");
					
					values[counter++] = value;
				}
				else
				{
					if(autoIncrements == null)
					{
						autoIncrements = new ArrayList<Object[]>();
					}
					
					autoIncrements.add(new Object[] { field.getName(), field.getType() });
					
				}
			}
			
			cmdSql.delete(cmdSql.length() - 2, cmdSql.length());
			parameters.delete(parameters.length() - 2, parameters.length());
			cmdSql.append(") VALUES (" + parameters.toString() + ");" );
			
			String[] autoIncs = null;
			
			if(autoIncrements != null)
			{
				autoIncs =  new String[autoIncrements.size()];
				
				int autoIncsCounter = 0;
				
				for(Object[] fieldInfo : autoIncrements)
				{
					autoIncs[autoIncsCounter++] = fieldInfo[0].toString().toLowerCase();
				}
			}
			
			PreparedStatement ps = connection.getPreparedStatement(cmdSql.toString(), autoIncs);
			
			try
			{
				int iterator = 1;
				for (int parameterNumber = 0; parameterNumber < values.length; parameterNumber++) {
					Object value = values[parameterNumber];
					
					if(value != null)
					{
						ps.setObject(iterator++, value);
					}
					
				}
				
				ps.execute();
				
				if(autoIncrements != null)
				{
					ResultSet rs = ps.getGeneratedKeys();
					
					if(rs.next())
					{
						int columnIndex = 1;
						
						for(Object[] fieldInfo : autoIncrements)
						{
							String fieldName = fieldInfo[0].toString();
							Class<?> fieldType = (Class<?>)fieldInfo[1];
							
							String methodSetName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
							
							Object value = rs.getObject(columnIndex++);
							
							if(value != null)
							{
								try {
									
									Method method = tbl.getClass().getDeclaredMethod(methodSetName, fieldType);
									
									try {
										if(value.getClass().getSimpleName().equals("BigDecimal"))
										{
											method.invoke(tbl, ((BigDecimal)value).doubleValue());
										}
										else if(value.getClass().getSimpleName().equals("Timestamp") && fieldType.getClass().getSimpleName().equals("Date"))
										{
											method.invoke(tbl, ((Timestamp)value).getTimestamp());
										}
										else
										{
											method.invoke(tbl, value);
										}
									} catch (Exception e) {
										throw new RaiberneitiException(e);
									}
									
								} catch (Exception e) {
									throw new RaiberneitiException(e);
								}
							}
						}
					}
				}
				
			}
			finally {
				connection.libera();
			}
		}
		catch (Exception e) {
			throw new RaiberneitiException(e);
		}
	}


	public void deleta() throws RaiberneitiException {
		Conexao connection = PoolDeConexoes.getConexao();
		
		try {
	
			StringBuilder cmdSql = new StringBuilder();
	
			String tableName = tbl.getClass().getSimpleName();
			Table alternativeTableName = tbl.getClass().getAnnotation( Table.class );
	
			if( alternativeTableName != null ) {
				tableName = alternativeTableName.name();
			}
	
			cmdSql.append( "DELETE FROM " + tableName + " WHERE " );
	
			Field[] fields = tbl.getClass().getDeclaredFields();
			ArrayList<Object> values = new ArrayList<Object>();
			int iterator = 0;
	
			for( Field field : fields ) {
	
				if( field.isAnnotationPresent( PrimaryKey.class ) ) {
	
					cmdSql.append( field.getName() );
					cmdSql.append( " = ? AND " );
	
					values.add( getFieldValue( field.getName() ) );
				}
			}
	
			cmdSql.delete( cmdSql.length() - 4, cmdSql.length() );

			PreparedStatement ps = connection.getPreparedStatement( cmdSql.toString() );

			try {

				iterator = 1;
				for( Object value : values ) {
					if( value != null ) {
						ps.setObject( iterator++, value );
					}
				}

				affectedRows = ps.executeUpdate();

			} catch( Exception e ) {
				throw new RaiberneitiException( e );
			}
		} finally {
			connection.libera();
		}
	}

	public void altera() throws RaiberneitiException {
		Conexao connection = PoolDeConexoes.getConexao();
		
		try { 
				
			StringBuilder cmdSql = new StringBuilder();
			StringBuilder parameters = new StringBuilder();
	
			String tableName = tbl.getClass().getSimpleName();
			Table alternativeTableName = tbl.getClass().getAnnotation( Table.class );
	
			if( alternativeTableName != null ) {
				tableName = alternativeTableName.name();
			}
	
			cmdSql.append( "UPDATE " + tableName + " SET " );
	
			Field[] fields = tbl.getClass().getDeclaredFields();
			ArrayList<Object> keys = new ArrayList<Object>();
			Object[] values = new Object[ fields.length ];
	
			int iterator = 0;
	
			for( Field field : fields ) {
	
				Object value = getFieldValue( field.getName() );
				values[ iterator++ ] = value;
	
				cmdSql.append( field.getName() + " = " + ( value == null ? "null" : "?" ) + ", " );
	
				if( field.isAnnotationPresent( PrimaryKey.class ) ) {
	
					parameters.append( field.getName() + " = ? AND " );
	
					keys.add( value );
				}
			}
	
			cmdSql.delete( cmdSql.length() - 2, cmdSql.length() );
			parameters.delete( parameters.length() - 4, parameters.length() );
	
			cmdSql.append( " WHERE " + parameters.toString() );

			PreparedStatement ps = connection.getPreparedStatement( cmdSql.toString() );

			try {

				iterator = 1;
				for( Object value : values ) {
					if( value != null ) {
						ps.setObject( iterator++, value );
					}
				}

				for( Object value : keys ) {
					if( value != null ) {
						ps.setObject( iterator++, value );
					}
				}

				affectedRows = ps.executeUpdate();

			} catch( Exception e ) {
				throw new RaiberneitiException( e );
			}
		} finally {
			connection.libera();
		}
	}
	
	private Object getFieldValue( String fieldName ) throws RaiberneitiException {

		String methodGetName = "get" + Character.toUpperCase( fieldName.charAt( 0 ) ) + fieldName.substring( 1 );

		try {
			Method m = tbl.getClass().getDeclaredMethod( methodGetName, (Class[]) null );
			Object vl = m.invoke( tbl, (Object[]) null );

			return vl;

		} catch( Exception e ) {
			throw new RaiberneitiException( e );
		}
	}
}
