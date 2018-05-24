package com.mst.util;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.DateConverter;

import com.mongodb.MongoClient;
import com.mst.model.util.MongoConnectionEntity;

public class MongoConnectionProvider {

	public enum DbProviderType { testDb, rawDb}; 

	public enum TestDBProvider {
		INSTANCE;
		private Datastore datastore;
		private void createInstance(MongoConnectionEntity entity) {
			try {
		    	Morphia morphia = new Morphia();
		    	morphia.getMapper().getConverters().addConverter(new LocalDateTimeConverter());
		    	datastore = morphia.createDatastore(new MongoClient(entity.getIpAddress()), entity.getDatabaseName());
		    	datastore.ensureIndexes();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public Datastore getDatastore(MongoConnectionEntity entity) {
			if(this.datastore==null) 
				createInstance(entity);
			return this.datastore;
		}
    }
	
	public enum RawDBProvider {
		INSTANCE;
		private Datastore datastore;
		private void createInstance(MongoConnectionEntity entity) {
			try {
		    	Morphia morphia = new Morphia();
		    	morphia.getMapper().getConverters().addConverter(new LocalDateTimeConverter());
		    	datastore = morphia.createDatastore(new MongoClient(entity.getIpAddress()), entity.getDatabaseName());
		    	datastore.ensureIndexes();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public Datastore getDatastore(MongoConnectionEntity entity) {
			if(this.datastore==null) 
				createInstance(entity);
			return this.datastore;
		}
    }
}
