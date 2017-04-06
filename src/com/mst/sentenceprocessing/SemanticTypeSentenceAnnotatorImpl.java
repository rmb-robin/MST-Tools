package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;

import com.mst.interfaces.SemanticTypeSentenceAnnotator;
import com.mst.model.WordToken;
 

public class SemanticTypeSentenceAnnotatorImpl implements SemanticTypeSentenceAnnotator{

	public List<WordToken> annotate(List<WordToken> tokens,Map<String,String> semanticTypes) {
		
		for(WordToken token : tokens){
			if(!semanticTypes.containsKey(token.getToken().toLowerCase())) continue;
			token.setSemanticType(semanticTypes.get(token.getToken().toLowerCase()));
		}
		return tokens;
	}

}
