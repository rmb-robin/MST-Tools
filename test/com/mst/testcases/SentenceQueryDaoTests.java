package com.mst.testcases;

import java.util.List;

import org.junit.Test;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.util.MongoDatastoreProviderDefault;

public class SentenceQueryDaoTests {

	@Test
	public void runQuery(){
		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
		SentenceQueryInput input = new SentenceQueryInput();
		input.getTokens().add("right");
		input.setEdgeName("laterality");
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<SentenceQueryResult> sentences = dao.getSentences(input);	
		List<SentenceQueryResult> tenmp = sentences;
	}
	
}

