package br.com.raiberneiti;

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

		// este é o método que busca um cliente baseado em sua chave primária que você deverá preencher
		
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

	public boolean isFound() {
		return found;
	}
	
	public T getRegistro() {
		return registro;
	}
}