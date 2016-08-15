package br.com.raiberneiti;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Parametros {
	
	private Properties parametros;
	
	private static Parametros mySelf;
	
	private Parametros() {
		parametros = new Properties();
	}
	
	public static Parametros getInstance() {

		if( mySelf == null ) {
			mySelf = new Parametros();
		}
		
		return mySelf;
	}
	
	public void addParametro( String chave, String valor ) {
		parametros.put( chave, valor );
	}
	
	public String getParametro( String nomeParametro ) {
		return parametros.getProperty( nomeParametro );
	}

	public void salvaParametros( String nomeArquivoParametros ) throws RaiberneitiException {
		
		try {
			FileOutputStream fos = new FileOutputStream( nomeArquivoParametros );
			
			try {
				parametros.storeToXML( fos, "Arquivo de Parametros do Raiberneiti" );
			} finally {
				fos.close();
			}
		} catch ( Exception e) {
			throw new RaiberneitiException( e );
		}
	}
	
	public void carregaParametros( String nomeArquivoParametros ) throws RaiberneitiException {

		try {
			FileInputStream fis = new FileInputStream( nomeArquivoParametros );
			try {
				parametros.loadFromXML( fis );
			} finally {
				fis.close();
			}
		} catch ( Exception e ) {
			throw new RaiberneitiException( e );
		}
	}
}










