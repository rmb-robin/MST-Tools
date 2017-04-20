package com.mst.metadataProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.gentwo.*;

import edu.stanford.nlp.ling.Word;

public class NounrRelationshipExpectedProvider {

	private String getFullFilePath(){
		return System.getProperty("user.dir") + "\\testData\\nounrelationsexpected.txt";
	}
	
	
	private Map<Integer,List<TokenRelationship>> relationsByIndex = new HashMap<>();
	
	public Map<Integer,List<TokenRelationship>> get(){
	
		List<String> lines = TestDataProvider.readLines(getFullFilePath());
		lines.forEach((a) -> processLine(a));
		return relationsByIndex;
	}
	
	private void processLine(String line){
		String[] contents= line.split(",");
		int index = Integer.parseInt(contents[0]);
		if(!relationsByIndex.containsKey(index))
			relationsByIndex.put(index, new ArrayList<TokenRelationship>());
	
	
		TokenRelationship relationship = new TokenRelationship();
		relationship.setFromToken(getWordToken(contents[1]));
		relationship.setFromToken(getWordToken(contents[2]));
		relationship.setEdgeName(contents[3]);
		relationsByIndex.get(index).add(relationship);
	}
	
	private WordToken getWordToken(String content){
		WordToken token = new WordToken();
		token.setToken(content);
		return token;
	}
	
	
}
