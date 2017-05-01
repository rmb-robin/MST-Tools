package com.mst.util;

import org.mongodb.morphia.Datastore;

import com.mst.model.util.MongoConnectionEntity;

public abstract class MongoDatastoreProviderBase {

	protected MongoConnectionEntity connectionEntity; 

	public Datastore getDataStore() {
		return MongoConnectionProvider.getDatastore(connectionEntity);
	}
}
