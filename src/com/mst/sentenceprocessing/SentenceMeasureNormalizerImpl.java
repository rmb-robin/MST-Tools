package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;

public class SentenceMeasureNormalizerImpl implements SentenceMeasureNormalizer {

	public List<WordToken> Normalize(List<WordToken> wordTokens, boolean convertMeasurements, boolean convertLargest) {
		if(!convertMeasurements && !convertLargest) return wordTokens;
		
		try {
			if(convertMeasurements) {
				convertMMtoCM(wordTokens);
			}
			
			if(convertLargest) {
				convertToLargestMeasurement(wordTokens);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return wordTokens;
	}

	private void convertMMtoCM(List<WordToken> words) {
		// https://medicalsearchtechnologies.atlassian.net/browse/UI-140
		
		for(WordToken word : words) {
			try {
				if((word.getSemanticType() != null && 
					word.getSemanticType().equalsIgnoreCase(SemanticTypes.cardinalNumber)) ||
				    Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
					
					// getPosition is 0-based so don't add one to get the next token
					WordToken nextWord = Constants.getToken(words, word.getPosition());
					
					if(nextWord.getToken().matches("(?i)mm|millimeters?")) {
						WordToken nextWordPlusOne = Constants.getToken(words, word.getPosition()+1);
						
						if(!nextWordPlusOne.getToken().equalsIgnoreCase("hg")) {
							String[] values = word.getToken().split("x");
							StringBuilder newValue = new StringBuilder();
							
							for(String v : values) {
								newValue.append(Float.parseFloat(v) / 10);
								newValue.append("x");
							}
							newValue.deleteCharAt(newValue.length()-1);
							
							word.setToken(newValue.toString());
							nextWord.setToken("cm");
						}
					}
				}
			} catch(Exception e) {
				System.out.println(e.toString());
			}
		}
	}
	
	private void convertToLargestMeasurement(List<WordToken> words) {
		// https://medicalsearchtechnologies.atlassian.net/browse/UI-179
		
		for(WordToken word : words) {
			if(Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
				// getPosition is 0-based so don't add one to get the next token
				WordToken nextWord = Constants.getToken(words, word.getPosition());
				
				if(nextWord.getToken().matches("(?i)mm|millimeters?|cm|centimeters?")) {
					String[] values = word.getToken().split("x");
					float newValue = Float.parseFloat(values[0]);
					
					for(String v : values) {
						if(Float.parseFloat(v) > newValue) {
							newValue = Float.parseFloat(v);
						}									
					}
					
					word.setToken(String.valueOf(newValue));
				}
			}
		}
	}
}
