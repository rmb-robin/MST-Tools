package com.mst.util;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;

public class MongoDatastoreProviderDefault extends MongoDatastoreProviderBase implements MongoDatastoreProvider {
	
	public MongoDatastoreProviderDefault(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("test");
		connectionEntity.setIpAddress("10.0.129.218");
	}

    public MongoDatastoreProviderDefault(String ipAddress, String databaseName) {
        connectionEntity = new MongoConnectionEntity();
        connectionEntity.setIpAddress(ipAddress);
        connectionEntity.setDatabaseName(databaseName);
    }
}
