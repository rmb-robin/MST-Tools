package com.mst.interfaces;

import org.mongodb.morphia.Datastore;

public interface MongoDatastoreProvider {

	Datastore getDataStore();
}
