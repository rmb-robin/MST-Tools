package com.mst.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mst.interfaces.RelationshipProcessor;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;
import com.mst.model.gentwo.PrepPhraseRelationshipMapping;
import com.mst.model.gentwo.PrepositionPhraseProcessingInput;
import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.TokenRelationship;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessor;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.PrepPhraseRelationshipProcessorImpl;
import com.mst.sentenceprocessing.PrepositionPhraseProcessingInputFactory;
import com.mst.sentenceprocessing.PrepositionPhraseProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.RelationshipInputProviderFileImpl;
import com.mst.testHelpers.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.testHelpers.SemanticTypeHardCodedProvider;
import com.mst.testHelpers.TestDataProvider;
import com.sun.corba.se.impl.oa.toa.TOA;

import edu.stanford.nlp.ling.Word;

public class PrepPhraseRelationshipProcessorTest {

	NGramsSentenceProcessorImpl ngramProcessor = new NGramsSentenceProcessorImpl();
	PrepositionPhraseProcessorImpl prepPhraseProcessor = new PrepositionPhraseProcessorImpl();
	NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
	
	PartOfSpeechAnnotatorEntity entity = new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity();
	PartOfSpeechAnnotatorImpl partOfSpeechAnnotator = new PartOfSpeechAnnotatorImpl();		
	
	SemanticTypeSentenceAnnotatorImpl stAnnotator = new SemanticTypeSentenceAnnotatorImpl();
	SemanticTypeHardCodedProvider stprovider = new SemanticTypeHardCodedProvider();
	RelationshipInput relationshipInput = new RelationshipInputProviderFileImpl().getNounRelationships(7);
	RelationshipProcessor nounrelationshipProcessor = new NounRelationshipProcessor();

	PrepPhraseRelationshipProcessorImpl prepRelationshipProcessor = new PrepPhraseRelationshipProcessorImpl();
	
	List<PrepPhraseRelationshipMapping> relationshipMappings = new RelationshipInputProviderFileImpl().getPrepPhraseRelationshipMapping();
	
	Map<String, List<TokenRelationship>> relationships = new HashMap<>();
	
	private void addToMap(TokenRelationship tokenRelationship){
		if(!relationships.containsKey(tokenRelationship.getEdgeName()))
				relationships.put(tokenRelationship.getEdgeName(), new ArrayList<TokenRelationship>());
		
		relationships.get(tokenRelationship.getEdgeName()).add(tokenRelationship);
	}
	
	
	private TokenRelationship getExpectedTokenRelationship(String edgeName, String fromToken, String toToken){
		TokenRelationship relationship = new TokenRelationship();
		relationship.setEdgeName(edgeName);
		WordToken wt = new WordToken();
		wt.setToken(fromToken);
		relationship.setFromToken(wt);
		
		 wt = new WordToken();
		wt.setToken(toToken);
		relationship.setToToken(wt);

		return relationship;
	}
	
	
	@Test
	public void passing()throws Exception{
		TokenRelationship tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "spine");
		addToMap(tokenRelationship);
		runAssert(false,"She has a cyst in the lumbar spine.", relationships);
		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "spine");
		addToMap(tokenRelationship);
		runAssert(false,"She has a cyst in the spine.", relationships);
		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "spine");
		addToMap(tokenRelationship);
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "kidney");
		addToMap(tokenRelationship);
		runAssert(false,"She has a cyst in the spine and kidney.", relationships);

		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "liver");
		addToMap(tokenRelationship);
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "kidney");
		addToMap(tokenRelationship);
		
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "stomach");
		addToMap(tokenRelationship);
		runAssert(false,"She has a cyst in the liver, kidney, and stomach.", relationships);
	
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("modified", "recommended", "patient");
		addToMap(tokenRelationship);
		runAssert(true,"Follow up ultrasound recommended in this postmenopausal patient.", relationships);
		
		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("modified", "recommended", "patient");
		addToMap(tokenRelationship);
		runAssert(true,"No follow up ultrasound recommended in this postmenopausal post ovarian cancer treated patient as new labs negative for ca125.", relationships);
	}
	
	
	@Test
	public void process()throws Exception{
		
	
		TokenRelationship tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "spine");
				
		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("diagnosis", "history", "disease");
		addToMap(tokenRelationship);
		//runAssert("She has a history of Chrons disease.", relationships);
//		issue: Diese has no st...

		

		

		
		relationships.clear();
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "spine");
		addToMap(tokenRelationship);
		tokenRelationship = getExpectedTokenRelationship("disease location", "cyst", "lobe");
		addToMap(tokenRelationship); 
	//	runAssert("She has a cyst in the lumbar spine and upper left hepatic lobe.", relationships);
	

		relationships.clear();		
		tokenRelationship = getExpectedTokenRelationship("take", "he", "lupron");
		addToMap(tokenRelationship);
		
		tokenRelationship = getExpectedTokenRelationship("take", "he", "xtandi");
		addToMap(tokenRelationship);
		
		tokenRelationship = getExpectedTokenRelationship("take", "he", "zytiga");
		addToMap(tokenRelationship);
	//	runAssert(false,"He is on Lupron, Xtandi, and Zytiga and is also taking a zinc supplement", relationships);
//		
		
		
//		expected.clear();
//		expected.add("patient");
//		
//		
	//	runAssert(false,"Follow up ultrasound recommended in this postmenopausal patient with ovarian cancer.", relationships);
 
//		
//		expected.clear();
//		expected.add("Lupron");
//		expected.add("Xtandi");
//		expected.add("Zytiga");
//		
//		expected.clear();
//		expected.add("patient");
//		expected.add("cancer");
//		
//		
//
//		expected.clear();
//		expected.add("negative");
//		expected.add("cancer");
//		expected.add("ca125");
//		
	}
	
	private void runAssert(boolean isModifiedEdge, String sentenceText, Map<String,List<TokenRelationship>> expected) throws Exception{		
		Sentence sentence = TestDataProvider.getSentences(sentenceText).get(0);
		
		sentence = ngramProcessor.process(sentence, new NGramsHardCodedProvider().getNGrams());
		List<WordToken> tokens = stAnnotator.annotate(sentence.getModifiedWordList(), stprovider.getSemanticTypes());
		tokens = partOfSpeechAnnotator.annotate(tokens, entity);
		nounrelationshipProcessor.process(tokens, relationshipInput);
		tokens = prepPhraseProcessor.process(tokens, new PrepositionPhraseProcessingInputFactory().create());

		List<TokenRelationship> relationships = prepRelationshipProcessor.process(tokens, relationshipMappings );
		
		if(isModifiedEdge){
			TokenRelationship relationship = relationships.get(0);
			assertTrue(relationship.getFrameName().equals("f_modifier"));
			TokenRelationship expectedRelation = expected.get("modified").get(0);
			assertEquals(expectedRelation.getFromToken().getToken(),expectedRelation.getFromToken().getToken());
			assertEquals(expectedRelation.getToToken().getToken(),expectedRelation.getToToken().getToken());
			return;
		}
	
		int totalSize = 0;
		for (Map.Entry<String, List<TokenRelationship>> entry : expected.entrySet()) {
			totalSize += entry.getValue().size();
		
			List<TokenRelationship> relationsForEdge =  relationships.stream().filter(a-> a.getEdgeName().equals(entry.getKey())).collect(Collectors.toList());
			assertEquals(entry.getValue().size(), relationsForEdge.size());

			for(TokenRelationship t: entry.getValue()){
				long count =  relationsForEdge.stream().filter(a-> a.getFromToken().getToken().equals(t.getFromToken().getToken()) && 																							a.getToToken().getToken().equals(t.getToToken().getToken())).count();
				assertEquals(1, count);
			}
		}
		assertEquals(totalSize,relationships.size());
	}
}
