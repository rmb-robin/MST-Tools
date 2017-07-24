package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;

public class SentenceDaoImpl extends BaseDocumentDaoImpl<SentenceDb> implements SentenceDao {

	public SentenceDaoImpl() {
		super(SentenceDb.class);
	}

	public void saveSentences(List<SentenceDb> sentences, DiscreteData discreteData,SentenceProcessingFailures failures) {
		Datastore ds = datastoreProvider.getDataStore();
		if(discreteData!=null){
			discreteData.setId(new ObjectId());
			discreteData.setTimeStamps();
			ds.save(discreteData);
			
			if(discreteData!=null){
				for(SentenceDb sentence: sentences){
					sentence.setDiscreteData(discreteData);
					sentence.setOrganizationId(discreteData.getOrganizationId());
				}
			}
		}
		ds.save(sentences);
		
		if(failures!=null){
			failures.setDate(LocalDate.now());
			if(discreteData!=null)
				failures.setDiscreteDataId(discreteData.getId().toString());
				failures.setOrgId(discreteData.getOrganizationId());
			ds.save(failures);
		}
	}
	
	public List<SentenceDb> getSentenceByDate(String orgId){
		//Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		
		LocalDate localDate = LocalDate.of(2017,7, 20);
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Query<SentenceDb> query = datastoreProvider.getDataStore().createQuery(SentenceDb.class);
		query.field("organizationId").equal(orgId);
		query
		 .filter("processingDate >", date);

		return query.asList();		 
		 
	}

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}
}
