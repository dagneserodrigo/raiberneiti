package br.com.raiberneiti;

import java.util.ArrayList;

public class PoolDeConexoes {
	
	private static PoolDeConexoes mySelf;
	private ArrayList<Conexao> conexoes;
	
	private PoolDeConexoes() {
		conexoes = new ArrayList<Conexao>();
	}
	
	public static PoolDeConexoes getInstance() {
		
		if( mySelf == null ) {
			mySelf = new PoolDeConexoes();
		}
		
		return mySelf;
	}
	
	public static synchronized Conexao getConexao() throws RaiberneitiException {
	
		PoolDeConexoes pc = getInstance();
		
		for( Conexao c : pc.conexoes ) {

			if( c.isLivre() ) {
				c.reserva();
				return c;
			}
		}
		
		Conexao c = new Conexao();
		c.reserva();
		pc.conexoes.add( c );

		return c;
	}
	
	
}