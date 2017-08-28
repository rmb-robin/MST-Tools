package com.mst.testcases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
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
		q.getValues().add(value);
		return q;
	}
	
	private EdgeQuery createNumericEdgeQuery(String name, String value, String valueTwo){
		EdgeQuery q = new EdgeQuery();
		q.setName(name);
		q.getValues().add(value);
		q.getValues().add(valueTwo);
		return q;
	}
	
//	@Test
	public void getSentencesTest(){
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		SentenceQueryInput input = new SentenceQueryInput();
		SentenceQueryInstance queryInstance = new SentenceQueryInstance();
		
		List<String> tokens = new ArrayList<>();
		tokens.add("cyst");
		queryInstance.setTokens(tokens);
		List<EdgeQuery> edgeNames = new ArrayList<>();
		
		
		
		edgeNames.add(createEdgeQuery("laterality",null));
		edgeNames.add(createEdgeQuery("disease location",null));
		
		
		queryInstance.setEdges(edgeNames);
		input.getSentenceQueryInstances().add(queryInstance);
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
		SentenceQueryInstance queryInstance = new SentenceQueryInstance();
		queryInstance.setTokens(tokens);

		
		List<String> edges = dao.getEdgeNamesByTokens(tokens);
		List<EdgeQuery> edgeNames = new ArrayList<>();
		
		for(String e: edges){
			edgeNames.add(createEdgeQuery(e,null));
		}
	
		queryInstance.setEdges(edgeNames);
		input.getSentenceQueryInstances().add(queryInstance);

		dao.getSentences(input);

	}

	//@Test 
	public void runQueryForNumericMatch(){
		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
		SentenceQueryInput input = new SentenceQueryInput();
		SentenceQueryInstance queryInstance = new SentenceQueryInstance();
		
		queryInstance.getTokens().add("cyst");
		List<EdgeQuery> edges = new ArrayList<>();
		edges.add(createNumericEdgeQuery("disease quantity","1","5"));
		queryInstance.setEdges(edges);
		input.getSentenceQueryInstances().add(queryInstance);
		
		input.getSentenceQueryInstances().add(queryInstance);
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		
		List<SentenceQueryResult> sentences = dao.getSentences(input);	
		int size = sentences.size();
		int t = size;
	}
	
	//@Test
	public void runQuery(){
		SentenceQueryDaoImpl dao = new SentenceQueryDaoImpl();
		SentenceQueryInput input = new SentenceQueryInput();
		SentenceQueryInstance queryInstance = new SentenceQueryInstance();
		
		queryInstance.getTokens().add("cyst");
		List<EdgeQuery> edges = new ArrayList<>();
		edges.add(createEdgeQuery("laterality",null));
		queryInstance.setEdges(edges);
		input.getSentenceQueryInstances().add(queryInstance);
		
		queryInstance = new SentenceQueryInstance();
		queryInstance.setAppender("and");
		queryInstance.getTokens().add("cyst");
	//	queryInstance.setEdges(edges);
		
		input.getSentenceQueryInstances().add(queryInstance);
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		
		List<SentenceQueryResult> sentences = dao.getSentences(input);	
		int size = sentences.size();
		int t = size;
	}	

}
