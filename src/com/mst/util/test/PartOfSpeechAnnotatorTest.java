package com.mst.util.test;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;

public class PartOfSpeechAnnotatorTest {

	
	@Test
	public void annotate(){
		PartOfSpeechHardcodedAnnotatorEntityProvider provider = new PartOfSpeechHardcodedAnnotatorEntityProvider();
		PartOfSpeechAnnotatorEntity entity = provider.getPartOfSpeechAnnotatorEntity();
		
		Map<Integer,PartOfSpeechSentenceExpectedResult> expectedResults = new PartOfSpeechExpectedResultsProvider().get();
		
		for (Map.Entry<Integer, PartOfSpeechSentenceExpectedResult> entry : expectedResults.entrySet()) {
			PrintExpectedResult(entry.getValue());
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
