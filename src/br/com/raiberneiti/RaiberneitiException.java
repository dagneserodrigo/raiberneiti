package br.com.raiberneiti;

public class RaiberneitiException extends Exception {

	private static final long serialVersionUID = 8136419859511335445L;

	public RaiberneitiException( String msg ) {
		super( msg );
	}
	
	public RaiberneitiException( Exception e ) {
		super( e );
	}
}