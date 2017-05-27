package com.mst.testcases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.util.MongoDatastoreProviderDefault;

public class SentenceQueryDaoIntergrationTest {

	SentenceQueryDaoImpl dao =  new SentenceQueryDaoImpl();
	
	
	@Test 
	public void getEdgeNamesByTokenTest(){
		
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		List<String> result = dao.getEdgeNamesByTokens(tokens);
	}
	
	@Test
	public void getSentencesTest(){
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		SentenceQueryInput input = new SentenceQueryInput();
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		input.setTokens(tokens);
		List<String> edgeNames = new ArrayList<>();
		edgeNames.add("laterality");
		edgeNames.add("disease location");
		input.setEdgeNames(edgeNames);
		List<SentenceQueryResult> result = dao.getSentences(input);
		List<SentenceQueryResult> resultN = result; 
		List<SentenceQueryResult> resultF = resultN; 
		
		dao.getEdgeNamesByTokens(tokens);
	} 
	
	@Test
	public void testAllEdges(){
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		SentenceQueryInput input = new SentenceQueryInput();
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		tokens.add("mass");
		tokens.add("tumor");
		
		input.setTokens(tokens);
		List<String> edgeNames = new ArrayList<>();
		edgeNames.add("laterality");

		
		List<String> edges = dao.getEdgeNamesByTokens(tokens);
		input.setEdgeNames(edges);
		dao.getSentences(input);
		
		
	}
	
	
//	@Test
//	public void runQuery(){
//		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
//		SentenceQueryInput input = new SentenceQueryInput();
//		input.getTokens().add("cyst");
//		input.setEdgeName("disease location");
//		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
//		List<SentenceQueryResult> sentences = dao.getSentences(input);	
//	}	

}
