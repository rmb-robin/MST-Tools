package com.mst.dao;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;

public class SentenceDiscoveryDaoImpl extends BaseDocumentDaoImpl<SentenceDiscovery> implements SentenceDiscoveryDao {

	public SentenceDiscoveryDaoImpl() {
		super(SentenceDiscovery.class);
	}
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}

	@Override
	public void saveSentenceDiscovieries(List<SentenceDiscovery> sentenceDiscoveries) {
		super.saveCollection(sentenceDiscoveries);	
	}
}
