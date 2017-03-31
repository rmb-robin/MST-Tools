package com.mst.testcases;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.interfaces.NounRelationshipProcessor;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.gentwo.NounRelationship;
import com.mst.model.gentwo.NounRelationshipInput;
import com.mst.model.gentwo.TokenRelationship;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.NounRelationshipProcessorImpl;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.NounRelationshipInputProviderFileImpl;
import com.mst.testHelpers.NounrRelationshipExpectedProvider;
import com.mst.testHelpers.SemanticTypeHardCodedProvider;
import com.mst.testHelpers.TestDataProvider;
import static org.junit.Assert.*;

public class NounRelationshipProcessorTest {

	
	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\nounrelatationshipsentences.txt";
	}
	
	private List<Sentence> getSentences(){
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		return TestDataProvider.getSentences(fileText);
	}
	
	@Test
	public void process(){
		List<Sentence> sentences = getSentences();
		
		NounRelationshipInput input = new NounRelationshipInputProviderFileImpl().get("f_related",7);
		Map<Integer, List<TokenRelationship>> expectedMap = new NounrRelationshipExpectedProvider().get();
		
		NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
		NGramsSentenceProcessorImpl ngramsProcessor = new NGramsSentenceProcessorImpl();
		 
		NounRelationshipProcessor processor = new NounRelationshipProcessorImpl();

		
		SemanticTypeSentenceAnnotatorImpl annotator = new SemanticTypeSentenceAnnotatorImpl();
		SemanticTypeHardCodedProvider provider = new SemanticTypeHardCodedProvider();
		
		int index = 0;
		for(Sentence sentence: sentences){
			
			Sentence ngramsProcessedSentence = ngramsProcessor.process(sentence,ngramsProvider.getNGrams());
			List<WordToken> modifiedTokens = annotator.annotate(ngramsProcessedSentence.getModifiedWordList(), provider.getSemanticTypes());
			if(index==1){
				System.out.println("STOP");
			}
			List<TokenRelationship> relationships =  processor.process(modifiedTokens, input);
			List<TokenRelationship> expectedValues = expectedMap.get(index);
			
			System.out.println(sentence.getFullSentence());
			for(TokenRelationship tr :  relationships){
				System.out.println("From " + tr.getFromToken().getToken());
				System.out.println("To " + tr.getToToken().getToken());
				
				System.out.println(tr.getEdgeName());
				System.out.println("**********************************************");
			}
			
			
			assertRelations(index, relationships,expectedMap.get(index));
			index+=1;
		}
	}
	
	private void assertRelations(int index,List<TokenRelationship> actual, List<TokenRelationship> expected){
		int sentenceNumber = index + 1;
		String failMessage = "Failed at Sentence # " + sentenceNumber;
		assertEquals(failMessage,expected.size(),actual.size());
	}

	private void WriteInput(NounRelationshipInput input ){
		for(NounRelationship r : input.getNounRelationships()){
			System.out.println(r.getFromToken());
			System.out.println(r.getIsFromSemanticType());
		
			System.out.println(r.getToToken());
			System.out.println(r.getIsToSemanticType());

			System.out.println(r.getMaxDistance());
			System.out.println(r.getEdgeName());
			System.out.println("**************************************");
		}
	}
	
}
