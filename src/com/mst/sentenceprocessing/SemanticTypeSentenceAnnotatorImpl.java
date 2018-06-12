package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.DateParseValidator;


public class SemanticTypeSentenceAnnotatorImpl implements SemanticTypeSentenceAnnotator{

	public List<WordToken> annotate(List<WordToken> tokens,Map<String,String> semanticTypes) {
		
		for(WordToken token : tokens){	
			if(!semanticTypes.containsKey(token.getToken().toLowerCase())) 
				{
					if(checkForNumeric(token))
						continue;
					checkForDate(token);
					continue;
				}
			token.setSemanticType(semanticTypes.get(token.getToken().toLowerCase()));
		}
		return tokens;
	}
	
	private boolean checkForNumeric(WordToken wordToken){
		if(!wordToken.getToken().matches("[-+]?\\d*\\.?\\d+")) return false;
		wordToken.setSemanticType(SemanticTypes.CARDINAL_NUMBER);
		return true;
	}
	
	private boolean checkForDate(WordToken token){
		boolean isValid = DateParseValidator.isDate(token.getToken());
		if(isValid)
			token.setSemanticType(SemanticTypes.DATE);
		return isValid;
	}
		
	
}
