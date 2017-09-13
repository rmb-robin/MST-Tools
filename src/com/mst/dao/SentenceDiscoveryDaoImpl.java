package com.mst.dao;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.model.recommandation.SentenceDiscovery;

public class SentenceDiscoveryDaoImpl extends BaseDocumentDaoImpl<SentenceDiscovery> implements SentenceDiscoveryDao {

	public SentenceDiscoveryDaoImpl() {
		super(SentenceDiscovery.class);
	}
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}
}
