package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;

import com.mst.interfaces.SemanticTypeSentenceAnnotator;
import com.mst.model.SemanticType;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;

import edu.stanford.nlp.ling.Word;
 

public class SemanticTypeSentenceAnnotatorImpl implements SemanticTypeSentenceAnnotator{

	public List<WordToken> annotate(List<WordToken> tokens,Map<String,String> semanticTypes) {
		
		for(WordToken token : tokens){	
			if(!semanticTypes.containsKey(token.getToken().toLowerCase())) 
				{
				checkForNumeric(token);
				continue;
				
				}
			token.setSemanticType(semanticTypes.get(token.getToken().toLowerCase()));
		}
		return tokens;
	}
	
	private void checkForNumeric(WordToken wordToken){
		if(!wordToken.getToken().matches("[-+]?\\d*\\.?\\d+")) return;
		wordToken.setSemanticType(SemanticTypes.cardinalNumber);
	}

}
