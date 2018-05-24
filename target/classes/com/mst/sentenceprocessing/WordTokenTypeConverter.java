package com.mst.sentenceprocessing;

import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;

public class WordTokenTypeConverter {

	public static Double tryConvertToDouble(WordToken wordToken){
		
		if(!wordToken.getSemanticType().equals(SemanticTypes.cardinalNumber)) return null;
		 try {  
			 return Double.parseDouble(wordToken.getToken());  
			 
	      } catch (NumberFormatException e) {  
	         return null;  
	      }
	}
}
