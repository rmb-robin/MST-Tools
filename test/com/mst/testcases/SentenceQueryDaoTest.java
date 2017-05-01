package com.mst.testcases;

import java.util.List;

import org.junit.Test;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceQueryDaoTest {

	@Test
	public void runQuery(){
		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
		SentenceQueryInput input = new SentenceQueryInput();
		input.getTokens().add("cyst");
		input.setEdgeName("disease location");
		List<SentenceDb> sentences = dao.getSentences(input);	
		List<SentenceDb> tenmp = sentences;
	}
	
}

