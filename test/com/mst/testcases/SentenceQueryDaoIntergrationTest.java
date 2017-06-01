package com.mst.testcases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.util.MongoDatastoreProviderDefault;

public class SentenceQueryDaoIntergrationTest {

	SentenceQueryDaoImpl dao =  new SentenceQueryDaoImpl();
	
	
	//@Test 
	public void getEdgeNamesByTokenTest(){
		
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		List<String> result = dao.getEdgeNamesByTokens(tokens);
	}
	
	private EdgeQuery createEdgeQuery(String name, String value){
		EdgeQuery q = new EdgeQuery();
		q.setName(name);
		q.setValue(value);
		return q;
	}
	
//	@Test
	public void getSentencesTest(){
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		SentenceQueryInput input = new SentenceQueryInput();
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		input.setTokens(tokens);
		List<EdgeQuery> edgeNames = new ArrayList<>();
		
		
		
		edgeNames.add(createEdgeQuery("laterality",null));
		edgeNames.add(createEdgeQuery("disease location",null));
		
		
		input.setEdges(edgeNames);
		List<SentenceQueryResult> result = dao.getSentences(input);
		List<SentenceQueryResult> resultN = result; 
		List<SentenceQueryResult> resultF = resultN; 
		
		dao.getEdgeNamesByTokens(tokens);
	} 
	
//	@Test
	public void testAllEdges(){
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		SentenceQueryInput input = new SentenceQueryInput();
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		tokens.add("mass");
		tokens.add("tumor");
		
		input.setTokens(tokens);
	

		
		List<String> edges = dao.getEdgeNamesByTokens(tokens);
		List<EdgeQuery> edgeNames = new ArrayList<>();
		
		for(String e: edges){
			edgeNames.add(createEdgeQuery(e,null));
		}
	
		input.setEdges(edgeNames);
		dao.getSentences(input);
		
		
	}
	
	
	@Test
	public void runQuery(){
		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
		SentenceQueryInput input = new SentenceQueryInput();
		input.getTokens().add("cyst");
		List<EdgeQuery> edges = new ArrayList<>();
		edges.add(createEdgeQuery("laterality",null));
		input.setEdges(edges);
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<SentenceQueryResult> sentences = dao.getSentences(input);	
		int size = sentences.size();
		int t = size;
	}	

}
