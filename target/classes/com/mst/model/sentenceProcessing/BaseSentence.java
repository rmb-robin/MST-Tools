package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

public class BaseSentence {

	protected List<WordToken> modifiedWordList = new ArrayList<>();

	public List<WordToken> getModifiedWordList() {
		return modifiedWordList;
	}

	public void setModifiedWordList(List<WordToken> modifiedWordList) {
		this.modifiedWordList = modifiedWordList;
	}
	
	public WordToken getTokenBySemanticType(String semanticType){
		if(this.modifiedWordList==null) return null;
		
		for(WordToken wordToken: this.modifiedWordList){
			if(wordToken.getSemanticType()==null) continue;
			if(wordToken.getSemanticType().equals(semanticType))
				return wordToken;
			
		}
		return null;
	}
}
