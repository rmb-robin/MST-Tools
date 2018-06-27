package com.mst.util;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;

public class SentenceServiceMongoDatastoreProvider extends MongoDatastoreProviderBase
		implements MongoDatastoreProvider {

	public SentenceServiceMongoDatastoreProvider() {
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("test");
		connectionEntity.setIpAddress("10.0.129.218"); // qa - radius..
		// connectionEntity.setIpAddress("10.0.129.219"); // prod -- radius..

		// connectionEntity.setIpAddress("10.210.192.4"); // dev..
		// connectionEntity.setIpAddress("10.12.128.98"); // prod..

	}

	public SentenceServiceMongoDatastoreProvider(String ipAddress, String databaseName) {
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName(databaseName);
		connectionEntity.setIpAddress(ipAddress);
	}

}
