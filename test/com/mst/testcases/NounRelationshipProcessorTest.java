package com.mst.testcases;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.NounrRelationshipExpectedProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessor;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;

import static org.junit.Assert.*;

public class NounRelationshipProcessorTest {

	RelationshipInput input = new RelationshipInput();
	Map<Integer, List<TokenRelationship>> expectedMap = new NounrRelationshipExpectedProvider().get();
	
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	NGramsSentenceProcessorImpl ngramsProcessor = new NGramsSentenceProcessorImpl();
	 
	RelationshipProcessor processor = new NounRelationshipProcessor();

	
	SemanticTypeSentenceAnnotatorImpl annotator = new SemanticTypeSentenceAnnotatorImpl();
	SemanticTypeHardCodedProvider provider = new SemanticTypeHardCodedProvider();
	
	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\nounrelatationshipsentences.txt";
	}
	
	private List<Sentence> getSentences(){
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		return TestDataProvider.getSentences(fileText);
	}
	
	@Test
	public void processNoun(){
		String text = "The simple cyst measures 3.5 cm.";
		Sentence sentence = TestDataProvider.getSentences(text).get(0);
		List<TokenRelationship> relationships = getTokenRelationships(sentence, 0);
		
		assertTrue(relationships.size()==3);
		assertTrue(relationships.get(1).getEdgeName().equals("unit of measure"));
		
	}
	
	
	@Test
	public void run(){
		String text = "recent ct shows a 3.5 mm simple cyst";
		Sentence sentence = TestDataProvider.getSentences(text).get(0);
		List<TokenRelationship> relationships = getTokenRelationships(sentence, 0);
	}
	
	//@Test
	public void process(){
		List<Sentence> sentences = getSentences();
		int index = 0;
		for(Sentence sentence: sentences){
			List<TokenRelationship> relationships = getTokenRelationships(sentence, index);
			List<TokenRelationship> expectedValues = expectedMap.get(index);
			assertRelations(index, relationships, expectedValues);
			index+=1;
		}
	}
	
	private List<TokenRelationship> getTokenRelationships(Sentence sentence,int index){
//		Sentence ngramsProcessedSentence = ngramsProcessor.process(sentence,ngramsProvider.getNGrams());
//		List<WordToken> modifiedTokens = annotator.annotate(ngramsProcessedSentence.getModifiedWordList(), provider.getSemanticTypes());
//		if(index==0){
//			System.out.println("STOP");
//		}
//		List<TokenRelationship> relationships =  processor.process(modifiedTokens, input);
//		
//		System.out.println(sentence.getNormalizedSentence());
//		for(TokenRelationship tr :  relationships){
//			System.out.println("From " + tr.getFromToken().getToken());
//			System.out.println("To " + tr.getToToken().getToken());
//			
//			System.out.println(tr.getEdgeName());
//			System.out.println("**********************************************");
//		}
//		return relationships;
		return null;
	}
	
	private void assertRelations(int index,List<TokenRelationship> actual, List<TokenRelationship> expected){
		int sentenceNumber = index + 1;
		String failMessage = "Failed at Sentence # " + sentenceNumber;
		assertEquals(failMessage,expected.size(),actual.size());
	}

	private void WriteInput(RelationshipInput input ){
		for(RelationshipMapping r : input.getRelationshipMappings()){
			System.out.println(r.getFromToken());
			System.out.println(r.getIsFromSemanticType());
		
			System.out.println(r.getToToken());
			System.out.println(r.getIsToSemanticType());


			System.out.println(r.getEdgeName());
			System.out.println("**************************************");
		}
	}
	
}
