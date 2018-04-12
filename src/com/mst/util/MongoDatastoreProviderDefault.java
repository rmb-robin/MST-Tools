package com.mst.util;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;

public class MongoDatastoreProviderDefault extends MongoDatastoreProviderBase implements MongoDatastoreProvider {
	
	public MongoDatastoreProviderDefault(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("sentencediscoveryDB");
	//	connectionEntity.setIpAddress("10.210.192.4"); //
		connectionEntity.setIpAddress("10.0.129.218");
	}
}
