package com.mst.sentenceprocessing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicTreeUI.CellEditorHandler;

import com.mst.interfaces.sentenceprocessing.SemanticTypeSentenceAnnotator;
import com.mst.model.SemanticType;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.DateParseValidator;

import edu.stanford.nlp.ling.Word;
 

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
		wordToken.setSemanticType(SemanticTypes.cardinalNumber);
		return true;
	}
	
	private boolean checkForDate(WordToken token){
		boolean isValid = DateParseValidator.isDate(token.getToken());
		if(isValid)
			token.setSemanticType(SemanticTypes.date);
		return isValid;
	}
		
	
}
