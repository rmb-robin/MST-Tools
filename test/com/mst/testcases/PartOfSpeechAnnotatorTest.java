package com.mst.testcases;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;
import com.mst.models.test.PartOfSpeechSentenceExpectedResult;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.PartOfSpeechAnnotatorImpl;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.PartOfSpeechExpectedResultsProvider;
import com.mst.testHelpers.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.testHelpers.TestDataProvider;

import static org.junit.Assert.*;

public class PartOfSpeechAnnotatorTest {

	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\pos_sentenceinput.txt";
	}
	
	@Test
	public void annotate(){
		PartOfSpeechHardcodedAnnotatorEntityProvider provider = new PartOfSpeechHardcodedAnnotatorEntityProvider();
		PartOfSpeechAnnotatorEntity entity = provider.getPartOfSpeechAnnotatorEntity();
		
		Map<Integer,PartOfSpeechSentenceExpectedResult> expectedResults = new PartOfSpeechExpectedResultsProvider().get();
		
		
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		List<Sentence> sentences = TestDataProvider.getSentences(fileText);
		
		NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
		NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
		PartOfSpeechAnnotatorImpl annotatorImpl = new PartOfSpeechAnnotatorImpl();		
		
		int index = 0;
		for(Sentence sentence: sentences){
			Sentence ngramsProcessedSentence = processor.process(sentence,ngramsProvider.getNGrams());
			List<WordToken> words = annotatorImpl.annotate(ngramsProcessedSentence.getModifiedWordList(), entity);
			assertSentences(words,index, expectedResults);
			index+=1;
		}
		
	}
	
	private void assertSentences(List<WordToken> words, int index, Map<Integer,PartOfSpeechSentenceExpectedResult> expectedResults){
		PartOfSpeechSentenceExpectedResult result = expectedResults.get(index);
		for (Map.Entry<String, List<String>> entry : result.getPosValues().entrySet()) {
			assertSinglePOS(entry.getKey(),entry.getValue(),words,index);
		}
	}
	
	private void assertSinglePOS(String posSymbol, List<String> expectedannotations,List<WordToken> words,int index)
	{
		for(String word: expectedannotations){
			for(WordToken token: words){
				if(token.getToken().equals(word))
					assertEquals(posSymbol, token.getPos());				
			}
		}
	}
	
	private void PrintExpectedResult(PartOfSpeechSentenceExpectedResult result){
		for (Map.Entry<String, List<String>> entry : result.getPosValues().entrySet()) {
			System.out.println(entry.getKey());			
			for(String v: entry.getValue()){
				System.out.println(v);
			}
		}
		System.out.println("*******************************");
	}
	
}
