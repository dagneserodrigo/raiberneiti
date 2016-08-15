package br.com.raiberneiti;

public interface ClasseDeNegocio {
    
    public void beforeInsert( Conexao cnx, Object registro ) throws RaiberneitiException;
    public void beforeUpdate( Conexao cnx, Object registro ) throws RaiberneitiException;
    public void beforeDelete( Conexao cnx, Object registro ) throws RaiberneitiException;
    
    public void afterInsert( Conexao cnx, Object registro ) throws RaiberneitiException;
    public void afterUpdate( Conexao cnx, Object registro ) throws RaiberneitiException;
    public void afterDelete( Conexao cnx, Object registro ) throws RaiberneitiException;
}
