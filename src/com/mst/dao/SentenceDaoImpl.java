package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;

public class SentenceDaoImpl extends BaseDocumentDaoImpl<SentenceDb> implements SentenceDao {

	private DiscreteDataDao discreteDataDao; 
	
	
	public SentenceDaoImpl() {
		super(SentenceDb.class);
		discreteDataDao = new DiscreteDataDaoImpl();
	}

	public void saveSentences(List<SentenceDb> sentences, DiscreteData discreteData,SentenceProcessingFailures failures) {
		Datastore ds = datastoreProvider.getDefaultDb();
		if(discreteData!=null){
				discreteDataDao.save(discreteData, false);
				
			for(SentenceDb sentence: sentences){
				sentence.setDiscreteData(discreteData);
				sentence.setOrganizationId(discreteData.getOrganizationId());
			}
		}
		ds.save(sentences);
		
		if(failures!=null){
			failures.setDate(LocalDate.now());
			if(discreteData!=null){
				failures.setDiscreteDataId(discreteData.getId().toString());
				failures.setOrgId(discreteData.getOrganizationId());
			}
			ds.save(failures);
		}
	}
	
	public void saveReprocess(List<SentenceDb> sentences,SentenceProcessingFailures failures){
		if(sentences.isEmpty()) return ;
		Datastore ds = datastoreProvider.getDefaultDb();
		ds.save(sentences);
		
		String orgId = sentences.get(0).getOrganizationId();
		
		if(failures!=null){
			failures.setDate(LocalDate.now());
			failures.setOrgId(orgId);
			ds.save(failures);
		}
	}
	
	public List<SentenceDb> getSentenceByDate(String orgId){
		//Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		
		LocalDate localDate = LocalDate.of(2017,7, 20);
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
		query.field("organizationId").equal(orgId);
		query
		 .filter("processingDate >", date);

		return query.asList();		 
		 
	}

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
		discreteDataDao.setMongoDatastoreProvider(provider);
	}

	@Override
	public List<SentenceDb> getByOrgId(String orgId) {
		Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
		query.field("organizationId").equal(orgId);
		return query.asList();		
	}
}
