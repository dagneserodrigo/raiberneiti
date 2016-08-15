package br.com.raiberneiti;

public class ClasseDeNegocioImplementation implements ClasseDeNegocio {

    @Override
    public void beforeInsert(Conexao cnx, Object registro) throws RaiberneitiException {}

    @Override
    public void beforeUpdate(Conexao cnx, Object registro) throws RaiberneitiException {}

    @Override
    public void beforeDelete(Conexao cnx, Object registro) throws RaiberneitiException {}

    @Override
    public void afterInsert(Conexao cnx, Object registro) throws RaiberneitiException {}

    @Override
    public void afterUpdate(Conexao cnx, Object registro) throws RaiberneitiException {}

    @Override
    public void afterDelete(Conexao cnx, Object registro) throws RaiberneitiException {}
    
}
