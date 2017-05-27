package com.mst.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;

public abstract class BaseDocumentDaoImpl<T> {

	private Class<T> entityClass;
	protected MongoDatastoreProvider datastoreProvider;
	
	public BaseDocumentDaoImpl(Class<T> entityClass){
		this.entityClass = entityClass;
	}


	public String save(T entity){
		Key<T> keys = datastoreProvider.getDataStore().save(entity);
		return keys.getId().toString();
	}

	public T get(String id)  {
		ObjectId objectId = new ObjectId(id);
		return datastoreProvider.getDataStore().get(entityClass, objectId);
	}
	
	public Query<T> getQueryById(String id){
		Query<T> query = datastoreProvider.getDataStore().createQuery(entityClass);
		 query
		 .field("id").equal(new ObjectId(id));
		 return query;
	}
	
	public Query<T> getQueryByFieldName(String id,String fieldName){
		Query<T> query = datastoreProvider.getDataStore().createQuery(entityClass);
		 query
		 .field(fieldName).equal(id);
		 return query;
	}
	
	public List<T> getAll(){
		Query<T> query = datastoreProvider.getDataStore().createQuery(entityClass);
		return query.asList();
	}
	
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;
		
	}
}
