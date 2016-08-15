package br.com.raiberneiti;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

	private Connection cnx;
	private boolean livre;

	public Conexao() throws RaiberneitiException {

		Parametros prm = Parametros.getInstance();
		livre = true;

		try {
			Class.forName( prm.getParametro( "driverJDBC" ) );

			String baseUrl = prm.getParametro( "baseURL" );
			String endBanco = prm.getParametro( "endBanco" );
			String nroPorta = prm.getParametro( "nroPorta" );
			String nomeDatabase = prm.getParametro( "nomeDatabase" );

			String urlBanco = baseUrl + endBanco + ":" + nroPorta + "/" + nomeDatabase;
			String nome = prm.getParametro( "nomeUsuario" );
			String senha = prm.getParametro( "senhaUsuario" );

			cnx = DriverManager.getConnection( urlBanco, nome, senha );
		} catch( Exception e ) {
			throw new RaiberneitiException( e );
		}

	}

	public void beginTransaction() throws RaiberneitiException {

		try {
			Statement st = cnx.createStatement();
			st.execute( "start transaction;" );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}

	public void commit() throws RaiberneitiException {

		try {
			Statement st = cnx.createStatement();
			st.execute( "commit;" );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}

	public void rollback() throws RaiberneitiException {

		try {
			Statement st = cnx.createStatement();
			st.execute( "rollback;" );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}

	public void executaComandoSQL( String comando ) throws RaiberneitiException {

		try {
			Statement st = cnx.createStatement();
			st.execute( comando );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}

	public PreparedStatement getPreparedStatement( String cmdSql ) throws RaiberneitiException {

		try {
			return cnx.prepareStatement( cmdSql );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}
	
	public PreparedStatement getPreparedStatement( String cmdSql, int cmd ) throws RaiberneitiException {

		try {
			return cnx.prepareStatement( cmdSql, cmd );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}
	
	public PreparedStatement getPreparedStatement( String cmdSql, String[] autoIncs ) throws RaiberneitiException {

		try {
			return cnx.prepareStatement( cmdSql, autoIncs );
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}
	
	public ResultSet executaQuery( String cmdSQL ) throws RaiberneitiException {

		try {
			Statement st = cnx.createStatement();
			return st.executeQuery( cmdSQL );
		} catch (Exception e) {
			throw new RaiberneitiException( e );
		}
	}

	public void insereRegistro( String nomeTabela, String[] nomesCampos, Object[] valoresCampos ) throws RaiberneitiException {

		if( nomesCampos.length != valoresCampos.length ) {
			throw new RaiberneitiException( "Quantidade de campos difere da quantidade de valores" );
		}

		StringBuilder cmd = new StringBuilder( "insert into " );
		StringBuilder prm = new StringBuilder();

		cmd.append( nomeTabela );
		cmd.append( " ( " );

		for( int i = 0; i < nomesCampos.length; i++ ) {

			if( prm.length() != 0 ) {
				cmd.append( ", " );
				prm.append( ", " );
			}

			cmd.append( nomesCampos[ i ] );
			prm.append( "?" );
		}

		cmd.append( " ) values ( " );
		cmd.append( prm );
		cmd.append( " );" );

		PreparedStatement ps = getPreparedStatement( cmd.toString() );
		int i = 1;

		try {
			for( Object vl : valoresCampos ) {
				ps.setObject( i++, vl );
			}

			ps.execute();
		} catch( SQLException e ) {
			throw new RaiberneitiException( e );
		}
	}

	public Connection getConnection() {
		return cnx;
	}

	public void desconecta() {
		try {
			cnx.close();
		} catch( SQLException ex ) {
			// nada a fazer
		}
	}
	
	public void reserva() throws RaiberneitiException {

		if( livre ) {
			livre = false;
		} else {
			throw new RaiberneitiException( "Conexão já em uso!" );
		}
	}
	
	public void libera() {
		livre = true;
	}

	public boolean isLivre() {
		return livre;
	}
}
