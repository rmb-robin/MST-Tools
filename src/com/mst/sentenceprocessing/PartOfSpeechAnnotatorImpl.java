package com.mst.sentenceprocessing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.PartOfSpeechAnnotator;
import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;
import com.mst.model.sentenceProcessing.WordToken;

public class PartOfSpeechAnnotatorImpl implements PartOfSpeechAnnotator {

	public List<WordToken> annotate(List<WordToken> tokens, PartOfSpeechAnnotatorEntity entity) {
		
		for(WordToken token: tokens){
			updateToken(token,entity);
		}
		return tokens;
	}
	
	
	private void updateToken(WordToken token,PartOfSpeechAnnotatorEntity entity){
		
		for (Map.Entry<String, HashSet<String>> entry : entity.getAnnotators().entrySet())
		{
		   if(entry.getValue().contains(token.getToken().toLowerCase()))
		   {
			   token.setPos(entry.getKey());
			   return;
		   }
		}
	}
}
