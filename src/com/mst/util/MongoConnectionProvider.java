package com.mst.util;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import com.mst.model.util.MongoConnectionEntity;

public class MongoConnectionProvider {

	public static Datastore getDatastore(MongoConnectionEntity entity) {
		Morphia morphia = new Morphia();
		morphia.getMapper().getConverters().addConverter(new LocalDateTimeConverter());
    	Datastore datastore;
		try {
			datastore = morphia.createDatastore(new MongoClient(entity.getIpAddress()), entity.getDatabaseName());
			// remove connection properties...
	    	datastore.ensureIndexes();
	    	return datastore;
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
}
