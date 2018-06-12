package com.mst.cache.implementation;

import com.google.gson.Gson;
import com.mst.dao.RedisManagerImpl;
import com.mst.interfaces.dao.RedisManager;
import com.mst.model.metadataTypes.CacheKeyTypes;
import com.mst.util.CacheKeyGenerator;

public abstract class CacheManagerBase<T> {

	private String keyType; 
	private Class<T> entityClass;
	protected RedisManager redisManager; 
	Gson gson = new Gson();
	
	public CacheManagerBase(String keyType,Class<T> entityClass){
		this.keyType = keyType;
		redisManager = new RedisManagerImpl();
		this.entityClass = entityClass;
	}

	protected String createkey(String key){
		 return CacheKeyGenerator.getKey(keyType, key);
	}
	
	protected String objectToString(T entityClass){
		return gson.toJson(entityClass);
	}
	
	protected T stringToObject(String json){
		return gson.fromJson(json, entityClass);
	}
}
