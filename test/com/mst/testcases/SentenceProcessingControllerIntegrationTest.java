package com.mst.testcases;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import static org.junit.Assert.*;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;

public class SentenceProcessingControllerIntegrationTest {

	private SentenceProcessingMetaDataInput input; 
	List<TokenRelationship> expectedRelationships;
	
	
	
	private void addToExpectedRelationships(String edgeName, String from, String to){
		TokenRelationship tokenRelationship = new TokenRelationship();
		tokenRelationship.setEdgeName(edgeName);
		WordToken wordToken = new WordToken();
		wordToken.setToken(from);
		tokenRelationship.setFromToken(wordToken);
		
		wordToken = new WordToken();
		wordToken.setToken(to);
		tokenRelationship.setToToken(wordToken);
		expectedRelationships.add(tokenRelationship);
	}
	
	@Test
	public void passing() throws Exception{
		init();
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence", "she","cyst");
		runTest("She has a 3.5 mm simple cyst");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence", "there","cyst");
		runTest("There is a simple 3.5 mm simple cyst");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence", "ct","cyst");
		runTest("CT shows a 3.5 mm simple cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence-no", "she","cyst");
		runTest("She has no cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence-no", "ct","cyst");
		runTest("CT shows no cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence-no", "ct","cyst");
		runTest("CT reveals no cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence-no", "she","cyst");
		runTest("She denies ovarian cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence-maybe", "ct","cyst");
		runTest("CT may reveal a cyst.");
		
		expectedRelationships = new ArrayList<>();
		addToExpectedRelationships("existence", "ct-scan","cyst");
		addToExpectedRelationships("existence", "ultrasound","cyst");
		runTest("CT scan and ultrasound demonstrates a benign cyst.");	
	}
	
	@Test
	public void process() throws Exception{
		init();
		 
//		expectedRelationships = new ArrayList<>();
//		addToExpectedRelationships("complicates", "diabetes","surgery");
//		runTest("Her diabetes complicates the surgery.");
//////		
//		expectedRelationships = new ArrayList<>();
//		addToExpectedRelationships("existence-no", "ct","uptake");
//		runTest("CT reveals no radiotracer uptake.");
		
//		expectedRelationships = new ArrayList<>();
//		addToExpectedRelationships("existence-maybe", "she","cyst");
//		runTest("She may have a cyst.");
//		
//		expectedRelationships = new ArrayList<>();
//		addToExpectedRelationships("existence-maybe", "ct","cyst");
//		runTest("CT shows a possible cyst");
//		

//		expectedRelationships = new ArrayList<>();
//		addToExpectedRelationships("existence-maybe", "ct","cyst");
//		runTest("CT probably reveals a cyst.");
		
	
	}
	
	private void init(){
		SentenceProcessingHardcodedMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingHardcodedMetaDataInputFactory();
		input = sentenceProcessingDbMetaDataInputFactory.create();
		
	}
	
	private void runTest(String text) throws Exception{
		SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
		controller.setMetadata(input);
		
		SentenceRequest request = new SentenceRequest();
		request.getSenteceTexts().add(text);
		Sentence sentence = controller.processSentences(request).get(0);
		
		
		for(TokenRelationship expected: expectedRelationships){
			List<TokenRelationship> matchedOnEdgeName = sentence.getTokenRelationships().stream().
					filter(a->a.getEdgeName().equals(expected.getEdgeName())).collect(Collectors.toList());
			
			if(matchedOnEdgeName.size()==0)
			{
				String error = "Sentence did not contained Edgename: " + expected.getEdgeName();
				assertTrue(error,1==2);
			}
			
			boolean isMatch = false;
			for(TokenRelationship sentenceRelation: matchedOnEdgeName){
				if(sentenceRelation.getToToken().getToken().equals(expected.getToToken().getToken()) &&
						sentenceRelation.getFromToken().getToken().equals(expected.getFromToken().getToken())){
					isMatch = true;
					break;
				}
			}
			
			if(!isMatch){
				String error = "Sentence did not match on Edgename: " + expected.getEdgeName();
				assertTrue(error,1==2);
			}
			
		}
		
	}
	
}
