package com.mst.util;

import org.mongodb.morphia.Datastore;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.util.MongoConnectionProvider.RawDBProvider;
import com.mst.util.MongoConnectionProvider.TestDBProvider;
 

public abstract class MongoDatastoreProviderBase {

	protected MongoConnectionEntity connectionEntity; 

	public Datastore getDefaultDb() {
		return TestDBProvider.INSTANCE.getDatastore(connectionEntity);
	}
	
	public Datastore getRawDb() {
		return RawDBProvider.INSTANCE.getDatastore(connectionEntity);
	}
	
	public void set(MongoConnectionEntity connection){
		this.connectionEntity = connection;
	}
}
