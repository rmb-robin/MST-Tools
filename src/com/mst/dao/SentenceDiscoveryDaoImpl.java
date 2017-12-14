package com.mst.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;

import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;

public class SentenceDiscoveryDaoImpl extends BaseDocumentDaoImpl<SentenceDiscovery> implements SentenceDiscoveryDao {


	private DiscreteDataDao discreteDataDao; 
	
	public SentenceDiscoveryDaoImpl() {
		super(SentenceDiscovery.class);
		discreteDataDao = new DiscreteDataDaoImpl();
		
	}
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
		discreteDataDao.setMongoDatastoreProvider(provider);
	}

	@Override
	public void saveSentenceDiscovieries(List<SentenceDiscovery> sentenceDiscoveries) {
		super.saveCollection(sentenceDiscoveries);	
	}
	
	public void saveSentenceDiscoveries(List<SentenceDiscovery> sentenceDiscoveries, DiscreteData discreteData,SentenceProcessingFailures failures) {
		Datastore ds = datastoreProvider.getDefaultDb();
		if(discreteData!=null){
				discreteDataDao.save(discreteData, false);
				
			for(SentenceDiscovery sentence: sentenceDiscoveries){
				sentence.setDiscreteData(discreteData);
				sentence.setOrganizationId(discreteData.getOrganizationId());
			}
		}
		ds.save(sentenceDiscoveries);
		
		if(failures!=null){
			failures.setDate(LocalDate.now());
			if(discreteData!=null){
				failures.setDiscreteDataId(discreteData.getId().toString());
				failures.setOrgId(discreteData.getOrganizationId());
			}
			ds.save(failures);
		}
	}
}
