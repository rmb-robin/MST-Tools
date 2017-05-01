package com.mst.dao;

import java.util.List;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceDaoImpl implements SentenceDao {

	private MongoDatastoreProvider datastoreProvider;

	public void saveSentences(List<SentenceDb> sentences) {
		datastoreProvider.getDataStore().save(sentences);
	}

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;
		
	}
}
