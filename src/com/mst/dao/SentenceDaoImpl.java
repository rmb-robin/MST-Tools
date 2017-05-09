package com.mst.dao;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceDaoImpl extends BaseDocumentDaoImpl<SentenceDb> implements SentenceDao {

	public SentenceDaoImpl() {
		super(SentenceDb.class);
	}

	public void saveSentences(List<SentenceDb> sentences, DiscreteData discreteData) {
		Datastore ds = datastoreProvider.getDataStore();
		if(discreteData!=null){
			discreteData.setId(new ObjectId());
			//discreteData.setTimeStamps();
			ds.save(discreteData);
			
			for(SentenceDb sentence: sentences){
				sentence.setDiscreteData(discreteData);
			}
		}
		ds.save(sentences);
	}

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}
}
