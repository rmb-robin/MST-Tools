package com.mst.util;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;

public class MongoDatastoreProviderDefault extends MongoDatastoreProviderBase implements MongoDatastoreProvider {
	
	public MongoDatastoreProviderDefault(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("test_new");
		connectionEntity.setIpAddress("10.210.192.4"); //
//		connectionEntity.setIpAddress("10.12.128.98");
	}
}
