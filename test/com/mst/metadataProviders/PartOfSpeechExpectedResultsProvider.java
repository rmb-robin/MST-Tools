package com.mst.metadataProviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.models.test.PartOfSpeechSentenceExpectedResult;

public class PartOfSpeechExpectedResultsProvider {

	private String getFullFilePath(String fileName){
		return System.getProperty("user.dir") + "\\testData\\" + fileName;
	}

	public Map<Integer, PartOfSpeechSentenceExpectedResult> get(){
		String filePath = getFullFilePath("pos_test_expected.txt");
		List<String> lines = TestDataProvider.readLines(filePath);
		Map<Integer, PartOfSpeechSentenceExpectedResult> result = new HashMap<Integer, PartOfSpeechSentenceExpectedResult>();
		int i =0;
		for(String line: lines){
			result.put(i,getResult(line));
			i+=1;
		}
		return result;
	}
	
	
	private PartOfSpeechSentenceExpectedResult getResult(String line){
		PartOfSpeechSentenceExpectedResult result = new PartOfSpeechSentenceExpectedResult();
		
		List<String> components = Arrays.asList(line.split(",")); 
		for(String pos:components){
			String[] posSplit = pos.split("=");
			List<String> values = Arrays.asList(posSplit[1].split(";"));
			result.getPosValues().put(posSplit[0].trim(), values);
		}
		return result;
	}
}
