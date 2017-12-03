package com.mst.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.util.MongoConnectionProvider.DbProviderType;

public abstract class BaseDocumentDaoImpl<T> {

	private Class<T> entityClass;
	protected MongoDatastoreProvider datastoreProvider;
	
	private DbProviderType dbProviderType; 
	public BaseDocumentDaoImpl(Class<T> entityClass, DbProviderType providerType){
		this.entityClass = entityClass;  
		this.dbProviderType = providerType;
	}

	public BaseDocumentDaoImpl(Class<T> entityClass){
		this.entityClass = entityClass;  
		this.dbProviderType = DbProviderType.testDb;
	}

	protected Datastore getDatastore(){
		if(dbProviderType.equals(DbProviderType.testDb))
			return datastoreProvider.getDefaultDb();
		return datastoreProvider.getRawDb();
	}
	
	public String save(T entity){
		Key<T> keys = getDatastore().save(entity);
		return keys.getId().toString();
	}

	public void saveCollection(List<T> entities){
		getDatastore().save(entities);
	}
	
	
	public T get(String id)  {
		ObjectId objectId = new ObjectId(id);
		return getDatastore().get(entityClass, objectId);
	}
	
	public Query<T> getQueryById(String id){
		Query<T> query = getDatastore().createQuery(entityClass);
		 query
		 .field("id").equal(new ObjectId(id));
		 return query;
	}
	
	public Query<T> getQueryByFieldName(String id,String fieldName){
		Query<T> query = getDatastore().createQuery(entityClass);
		 query
		 .field(fieldName).equal(id);
		 return query;
	}
	
	public List<T> getAll(){
		Query<T> query = getDatastore().createQuery(entityClass);
		return query.asList();
	}
	
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;
		
	}
}
