package com.mst.sentenceprocessing;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.PrepositionPhraseProcessor;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.PrepositionPhraseProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;


public class PrepositionPhraseProcessorImpl implements PrepositionPhraseProcessor {

	private class StAnnotion{
		private WordToken wordToken; 
		private int index;
		
		public WordToken getWordToken() {
			return wordToken;
		}
		public void setWordToken(WordToken wordToken) {
			this.wordToken = wordToken;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		} 
		
	}
	
	
	private List<WordToken> tokens;
	private HashSet<String> punctuations;
	private HashSet<String> stLookups;
	private int range;
	
	public List<WordToken> process(List<WordToken> tokens,PrepositionPhraseProcessingInput input) {
		this.tokens = tokens;
		this.punctuations = input.getPunctuations();
		this.stLookups = input.getStLookups();
		this.range = input.getRange();
		processPropositionPhrases();
		return this.tokens;
	}
	
	private void processPropositionPhrases(){
		for(WordToken wordToken: tokens){
			if(wordToken.getPos()!=PartOfSpeachTypes.IN) continue;
			processInToken(wordToken);
		}
	}
	
	private void processInToken(WordToken inPosToken){
		int startTokenIndex = tokens.indexOf(inPosToken);
		int endIndex = tokens.size()-1;
		int currentIterations=0;
		boolean phraseFound = false;
		List<WordToken> nounPhraseEndTokens = new ArrayList<WordToken>();
		List<StAnnotion> sTTokens = new ArrayList<StAnnotion>();
		
		for(int i = startTokenIndex+1; i<tokens.size();i++){
			WordToken wordToken = tokens.get(i);
		
			boolean checkNext = false;
			if(shouldAnnotateWithPrepPhraseEnd(wordToken,nounPhraseEndTokens,sTTokens,i)){
				wordToken.setPropertyValueType(PropertyValueTypes.PrepPhraseEnd);
				checkNext = true;
				phraseFound = true;
			}
			if(punctuations.contains(wordToken.getToken()))
			{
				if(!phraseFound && i-1>=0)
					tokens.get(i-1).setPropertyValueType(PropertyValueTypes.PrepPhraseEnd);
			}
			
			else if(wordToken.getPos()==PartOfSpeachTypes.IN && !phraseFound){
				tokens.get(i-1).setPropertyValueType(PropertyValueTypes.PrepPhraseEnd);
			}
			
			if(!wordToken.getToken().equals(","))
				currentIterations+=1;
			
			if(shouldEndLoop(i, endIndex, checkNext,currentIterations)) break;
		}
		cleanTokens(nounPhraseEndTokens);
		cleanSTTokens(sTTokens);
	}
	
	private void cleanSTTokens(List<StAnnotion> stAnnotations){
		if(stAnnotations.size()<2) return;
		for(int i =1; i<stAnnotations.size();i++){
			
			StAnnotion current = stAnnotations.get(i);
			StAnnotion previous = stAnnotations.get(i-1);
			
			if(current.index-previous.index == 1)
				previous.getWordToken().setPropertyValueType(null);
		}
	}
	
	private void cleanTokens(List<WordToken> wordTokens){
		for(int i = 0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(i!=wordTokens.size()-1)
				wordToken.setPropertyValueType(null);
		}
	}
	
	private boolean shouldEndLoop(int index, int endIndex, boolean checkNext,int currentIterations){
		
		if(checkNext && index+1>=endIndex)return true;
		if(checkNext){
			WordToken nextToken = tokens.get(index+1);
			return punctuations.contains(nextToken.getToken());
		}
		
		WordToken currentToken = tokens.get(index);
		if(currentToken.isVerb())return true;
		if(currentIterations==this.range) return true;
		return false;			
	 }
	
	private boolean shouldAnnotateWithPrepPhraseEnd(WordToken wordToken, List<WordToken> nounPhraseEndTokens, List<StAnnotion> stTokens, int index){
		if(wordToken.getPropertyValueType() == PropertyValueTypes.NounPhraseEnd) 
		{
			nounPhraseEndTokens.add(wordToken);
			return true;
		}
		
		if(stLookups.contains(wordToken.getSemanticType()))
		{
			StAnnotion annotion = new StAnnotion();
			annotion.setIndex(index);
			annotion.setWordToken(wordToken);
			stTokens.add(annotion);
			return true;
		}
		return false;
	}
	
	
}
