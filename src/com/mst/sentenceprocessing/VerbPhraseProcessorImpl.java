package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.VerbPhraseProcessor;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.VerbPhraseInput;
import com.mst.model.sentenceProcessing.WordToken;

public class VerbPhraseProcessorImpl implements VerbPhraseProcessor {
 
	private final int maxIterationsForSubjectComplement = 3;
	private final int stOffset = 2;
	
	private List<WordToken> wordTokens;
	VerbPhraseInput verbPhraseInput; 
	public List<WordToken>  process(List<WordToken> tokens,VerbPhraseInput input) {
		this.wordTokens = tokens;
		this.verbPhraseInput = input;
		createAnnotations();
		return this.wordTokens;
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
	
	private void annotateSubjectComplement(int verbIndex){
		int twoTokensAwayIndex = verbIndex+2;
		if(twoTokensAwayIndex >=wordTokens.size()) return;
		WordToken twoTokensAway = wordTokens.get(twoTokensAwayIndex);
		if(twoTokensAway.getToken().equals("."))
		{
			annotateWordToken(wordTokens.get(verbIndex+1), PropertyValueTypes.SubjectComplement);
			return;
		}
		
		int nextIndex = verbIndex + 1;
		if(nextIndex>= wordTokens.size()) return;
		WordToken nextToken = wordTokens.get(nextIndex);
		if(nextToken.getToken().equals(".")) return;
		if(nextToken.getPos() == PartOfSpeachTypes.IN) return;
		int subjectComplementIndex = findSubjectComplement(nextIndex);
		if(subjectComplementIndex==-1) return;
		if(doesSubjectComplementCompoundExist(verbIndex,subjectComplementIndex))
			findSubjectComplement(subjectComplementIndex+1);
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
			if(isSemanticTypeMatch(wordToken) && wordToken.getPropertyValueType()!= PropertyValueTypes.PrepPhraseEnd) {
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
		WordToken firstToken = wordTokens.get(0);
		if(verbPhraseInput.getFirstWordSubjects().contains(firstToken.getToken().toLowerCase()))
		{
			annotateWordToken(firstToken, PropertyValueTypes.Subject);
			return true;
		}
		if(isSemanticTypeMatch(firstToken))
		{
			annotateWordToken(firstToken, PropertyValueTypes.Subject);
			return true;
		}
		
		annotateWordToken(firstToken, PropertyValueTypes.Subject);
		firstToken.setSubjectSetFromWildCard(true);
		return true;
	}
	

	private void findAndAnnotateCompoundSubjectFromSemanticType(int stIndex){
		for(int i = stIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(isSemanticTypeMatch(wordToken)){
				annotateWordToken(wordToken, PropertyValueTypes.Subject);
				if(i>1){
					if(wordTokens.get(i-1).getToken().equals(",")) 
						continue;
				}
				return;
			}
			stIndex = i;
		}
	//	WordToken subjectCompound = wordTokens.get(stIndex+stOffset);
	//	annotateWordToken(subjectCompound, PropertyValueTypes.Subject);
	}

	private void findAndAnnotateCompoundSubjectFromNounPhrase(int nounPhraseEndIndex){
		for(int i = nounPhraseEndIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPropertyValueType()== PropertyValueTypes.NounPhraseBegin) 
			{
				//if(i<2) return;
				WordToken compoundSubject =  wordTokens.get(i);
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
	
	

	
	private int findSubjectComplement(int startIndex){
		int endIndex = Math.min(startIndex + maxIterationsForSubjectComplement+1, wordTokens.size()-1);
		
		for(int i = startIndex;i<=endIndex;i++){
			if(processSubjectComplementSTandNounPhrase(i)) 
				return i;
		}
		
		for(int i = startIndex;i<=endIndex;i++){
			if(processSubjectComplementForPosIn(i)) 
				return i;
		}
		return -1;
	}
	
	private boolean doesSubjectComplementCompoundExist(int verbIndex, int subjectComplementIndex){
		if(verbIndex+1 >= wordTokens.size())return false;
		WordToken nextToken = wordTokens.get(verbIndex+1);
		if(nextToken.getPos()!=null){
			if(nextToken.getPos().equals(PartOfSpeachTypes.IN) || nextToken.getPos().equals(PartOfSpeachTypes.PUNCTUATION)) 
				return false;
		}
		if(subjectComplementIndex>= wordTokens.size())return false;
		nextToken = wordTokens.get(subjectComplementIndex+1);
		if(nextToken.getPos()!=null){
			if(nextToken.getPos().equals(PartOfSpeachTypes.CC) || nextToken.getToken().equals(","))
			return true; 
		}
		return false;
		
	}
	
	private boolean processSubjectComplementSTandNounPhrase(int currentIndex){
		WordToken nextToken = wordTokens.get(currentIndex);
		if(nextToken.getToken().equals(".")) return false;
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
	 
	
	private boolean processSubjectComplementForPosIn(int currentIndex){
		
		int nextindex = currentIndex + 1;
		if(nextindex >= wordTokens.size()) return false;
		
		WordToken nextToken = wordTokens.get(nextindex);
		if(nextToken.getPos()==PartOfSpeachTypes.IN)
		{
			annotateWordToken(wordTokens.get(currentIndex), PropertyValueTypes.SubjectComplement);
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
