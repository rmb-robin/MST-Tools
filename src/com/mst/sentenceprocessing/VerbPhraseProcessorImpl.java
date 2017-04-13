package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.VerbPhraseProcessor;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeachTypes;
import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.TokenRelationship;
import com.mst.model.gentwo.VerbPhraseInput;
import com.mst.model.gentwo.VerbType;

import edu.stanford.nlp.ling.Word;

public class VerbPhraseProcessorImpl implements VerbPhraseProcessor {
 
	private final int maxIterationsForSubjectComplement = 3;
	private final int stOffset = 2;
	
	private List<WordToken> wordTokens;
	VerbPhraseInput verbPhraseInput; 
	public List<TokenRelationship>  process(List<WordToken> tokens,VerbPhraseInput input) {
		this.wordTokens = tokens;
		this.verbPhraseInput = input;
		createAnnotations();
		return null;
	}
	
	private void createAnnotations(){
		for(int i =0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(isVerb(wordToken))
			{
				List<Integer> verbIndexes = findVerbIndexs(wordToken,i,7);
				annotateSubjects(verbIndexes.get(0));
				annotateSubjectComplement(verbIndexes.get(verbIndexes.size()-1));
			}
		}
	}

	private void annotateSubjects(int verbIndex){
		if(verbIndex==0) return;
		if(verbIndex==1){
			if(annotateSubjectsFirstWord()) return;
		}
	
		boolean lookForPos = false;
		for(int i = verbIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			
			if(lookForPos && wordToken.getPos()==PartOfSpeachTypes.IN){
				WordToken subject = findSubjectAfterPosIn(i, wordToken);
				if(subject!=null) 
				{
					annotateWordToken(subject, PropertyValueTypes.Subject);
					return;
				}
				lookForPos = false;
			}
			
			if(wordToken.getPropertyValueType()==PropertyValueTypes.NounPhraseEnd)
			{
					findAndAnnotateCompoundSubjectFromNounPhrase(i);
					annotateWordToken(wordToken, PropertyValueTypes.Subject);
					return;
			}
			if(isSemanticTypeMatch(wordToken)) {
				findAndAnnotateCompoundSubjectFromSemanticType(i);
				annotateWordToken(wordToken, PropertyValueTypes.Subject);
				return;
			}
		
			if(wordToken.getPropertyValueType()==PropertyValueTypes.PrepPhraseEnd)
				lookForPos = true;
		}
		return;
	}
	
	
	private boolean annotateSubjectsFirstWord(){
		WordToken firstWord = wordTokens.get(0);
		if(verbPhraseInput.getFirstWordSubjects().contains(firstWord.getToken()))
		{
			annotateWordToken(firstWord, PropertyValueTypes.Subject);
			return true;
		}
		if(isSemanticTypeMatch(firstWord))
		{
			annotateWordToken(firstWord, PropertyValueTypes.Subject);
			return true;
		}
		return false;
	}
	

	
	
	private void findAndAnnotateCompoundSubjectFromSemanticType(int stIndex){
		for(int i = stIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(!isSemanticTypeMatch(wordToken))
			{
				WordToken subjectCompound = wordTokens.get(stIndex+stOffset);
				annotateWordToken(subjectCompound, PropertyValueTypes.Subject);
				return;
			}
			stIndex = i;
		}
		WordToken subjectCompound = wordTokens.get(stIndex+stOffset);
		annotateWordToken(subjectCompound, PropertyValueTypes.Subject);
	}

	private void findAndAnnotateCompoundSubjectFromNounPhrase(int nounPhraseEndIndex){
		for(int i = nounPhraseEndIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPropertyValueType()== PropertyValueTypes.NounPhraseBegin) 
			{
				if(i<2) return;
				WordToken compoundSubject =  wordTokens.get(i+2);
				annotateWordToken(compoundSubject, PropertyValueTypes.Subject);
				return;
			}
		}
	}


	private WordToken findSubjectAfterPosIn(int index, WordToken posInToken){
		if(index==0) return null;  //change that to nto return.
		WordToken previous = wordTokens.get(index-1);
		if(previous.getPropertyValueType() ==PropertyValueTypes.PrepPhraseEnd)
			return null;
		return previous;
	}
	
	private void annotateWordToken(WordToken wordToken, PropertyValueTypes propertyValueType){
		wordToken.setPropertyValueType(propertyValueType);
	}
	
	
	private boolean isSemanticTypeMatch(WordToken wordToken){
		return wordToken.getSemanticType()!=null && verbPhraseInput.getStTypes().contains(wordToken.getSemanticType());
	}
	
	
	private void annotateSubjectComplement(int verbIndex){
		int nextIndex = verbIndex + 1;
		WordToken nextToken = wordTokens.get(nextIndex);
		if(nextToken.getToken().equals(".")) return;
		if(nextToken.getPos() == PartOfSpeachTypes.IN) return;
		if(processSubjectComplementSTandNounPhrase(nextIndex)) 
			return;
		
		int startIndex = nextIndex + 1;
		int endIndex = startIndex + maxIterationsForSubjectComplement;
		
		for(int i = startIndex;i<=endIndex;i++){
			if(processSubjectComplementSTandNounPhrase(i)) 
				return;
		}
	}
	
	
	private boolean processSubjectComplementSTandNounPhrase(int currentIndex){
		WordToken nextToken = wordTokens.get(currentIndex);
		if(nextToken.getPropertyValueType() == PropertyValueTypes.NounPhraseBegin){
			WordToken lastNounToken = getLastTokenInNounPhrase(currentIndex);
			if(lastNounToken!=null)
				annotateWordToken(lastNounToken, PropertyValueTypes.SubjectComplement);
				return true;
		}
			
		if(isSemanticTypeMatch(nextToken)){
			WordToken lastStTypeToken = getLastSemanticType(currentIndex);
			annotateWordToken(lastStTypeToken, PropertyValueTypes.SubjectComplement);
			return true;
		}
		return false;
	}
	 
	
	
	private WordToken getLastTokenInNounPhrase(int index){
		for(int i=index+1;i<wordTokens.size();i++){
			WordToken nextToken = wordTokens.get(i);
			if(nextToken.getPropertyValueType()==PropertyValueTypes.NounPhraseEnd)
				return nextToken;
		}
		return null;
	}
	
	private WordToken getLastSemanticType(int index){
		WordToken lastKnownSt = wordTokens.get(index);
		for(int i=index+1;i<wordTokens.size();i++){
			WordToken nextToken = wordTokens.get(i);
			if(!isSemanticTypeMatch(nextToken))
				return lastKnownSt;
			lastKnownSt = nextToken;
		}
		return lastKnownSt;
	}
	
	private boolean isVerb(WordToken wordToken){
		if(!wordToken.isVerb()) return false;
		if(wordToken.getVerb().getVerbType()==VerbType.IV) return false;
		return true;
	}
	
	private List<Integer> findVerbIndexs(WordToken verbWordToken, int firstVerbIndex, int distance){
		List<Integer> result = new ArrayList<>();
		result.add(firstVerbIndex);
		if(verbWordToken.getVerb().getVerbType() == VerbType.AV) 
			return result;
		
		int endIndex = Math.min(firstVerbIndex+distance, wordTokens.size());
		for(int i =firstVerbIndex+1;i<endIndex;i++){
			WordToken tokenInstance = wordTokens.get(i);
			if(!isVerb(tokenInstance))continue;
			if(tokenInstance.getVerb().getVerbType()==VerbType.AV)
			{
				result.add(i);
				return result;
			}
		}
		return result;
	}

 

}
