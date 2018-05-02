package com.mst.metadataProviders;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.SemanticTypeProvider;

public class SemanticTypeHardCodedProvider  {

	private String getFullFilePath(String filePath){
			return System.getProperty("user.dir") + File.separator + "testData" + File.separator + filePath;
	}

	public Map<String, String> getSemanticTypes(boolean isSentence) {
		
		Map<String,String> result = new HashMap<String,String>();
		List<String> lines = TestDataProvider.readLines(getFullFilePath("semanticTypes.txt"));
		for(String line: lines){
			String [] contents = line.split(",");
			if(result.containsKey(contents[0])) continue;
			result.put(contents[0], contents[1]);
		}
		return result;
	}
}



 
