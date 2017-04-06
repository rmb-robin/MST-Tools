package com.mst.sentenceprocessing;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import com.mst.interfaces.PrepositionPhraseProcessor;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeachTypes;
import com.mst.model.gentwo.PrepositionPhraseProcessingInput;
import com.mst.model.gentwo.PropertyValueTypes;


public class PrepositionPhraseProcessorImpl implements PrepositionPhraseProcessor {

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
		for(int i = startTokenIndex+1; i<tokens.size();i++){
			WordToken wordToken = tokens.get(i);
			if(wordToken.getPropertyValueType() == PropertyValueTypes.NounPhraseEnd)
				nounPhraseEndTokens.add(wordToken);
			
			boolean checkNext = false;
			if(shouldAnnotateWithPrepPhraseEnd(wordToken)){
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
		cleanNonLastNounPhraseEndTokens(nounPhraseEndTokens);
	}
	
	private void cleanNonLastNounPhraseEndTokens(List<WordToken> wordTokens){
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
	
	private boolean shouldAnnotateWithPrepPhraseEnd(WordToken wordToken){
		if(wordToken.getPropertyValueType() == PropertyValueTypes.NounPhraseEnd) return true;
		if(stLookups.contains(wordToken.getSemanticType())) return true;
		return false;
	}
	
	
}
