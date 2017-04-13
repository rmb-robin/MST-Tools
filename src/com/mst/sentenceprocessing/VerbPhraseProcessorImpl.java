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
				WordToken subject = findSubject(verbIndexes.get(0));
				if(subject!=null) 
					annotateWordToken(subject, PropertyValueTypes.Subject);
				WordToken subjectComplement = findSubjectComplement(verbIndexes.get(verbIndexes.size()-1));
				if(subjectComplement!=null)
					annotateWordToken(subjectComplement,PropertyValueTypes.SubjectComplement);
			}
		}
	}
	
	
	private void annotateWordToken(WordToken wordToken, PropertyValueTypes propertyValueType){
		wordToken.setPropertyValueType(propertyValueType);
	}
	
	private WordToken findSubject(int verbIndex){
		if(verbIndex==0) return null;
		if(verbIndex==1){
			WordToken firstWord = wordTokens.get(0);
			if(verbPhraseInput.getFirstWordSubjects().contains(firstWord.getToken()))return firstWord;
			if(isSemanticTypeMatch(firstWord)) return firstWord;
			return null;
		}
	
		boolean lookForPos = false;
		for(int i = verbIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			
			if(lookForPos && wordToken.getPos()==PartOfSpeachTypes.IN){
				WordToken subject = findSubjectAfterPosIn(i, wordToken);
				WordToken subjectPrevious = findSubjectCompound(i-2);
				if(subject!=null) return subject;
				lookForPos = false;
			}
			
			if(wordToken.getPropertyValueType()==PropertyValueTypes.NounPhraseEnd)
			{
					int compoundSubjectIndex = findCompoundSubjectFromNounPhrase(i);
					return wordToken;
			}
			if(isSemanticTypeMatch(wordToken)) {
				int compoundSubjectIndex = findCompoundSubjectFromSemanticType(i) + 2;
				return wordToken;
			}
		
			if(wordToken.getPropertyValueType()==PropertyValueTypes.PrepPhraseEnd)
				lookForPos = true;
		}
		return null;
	}
	
	
	
	
	private boolean isSemanticTypeMatch(WordToken wordToken){
		return wordToken.getSemanticType()!=null && verbPhraseInput.getStTypes().contains(wordToken.getSemanticType());
	}
	
	
	private int findCompoundSubjectFromSemanticType(int stIndex){
		for(int i = stIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(!isSemanticTypeMatch(wordToken))
				return stIndex;
			stIndex = i;
		}
		return stIndex;
	}
	

	
	private int findCompoundSubjectFromNounPhrase(int nounPhraseEndIndex){
		for(int i = nounPhraseEndIndex-1; i>= 0;i--){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPropertyValueType()== PropertyValueTypes.NounPhraseBegin) return i+2;
		}
		return -1;
	}

	
	
//	C2. If the preceding token is POS=coordinating conjunction, then loop through the tokens using the B3 logic above.
//			Keep looping through the tokens until the condition in C1 is no longer met. In other words, once another subject which 
//			is at the beginning of the sentence is identified, we can stop looking for another subject.

	private WordToken findSubjectCompound(int index){
		if(index<=0)return null;
		WordToken wordToken = wordTokens.get(index);
		if(wordToken.getPos()== PartOfSpeachTypes.CC)
		{
			//do soemthign.
		}
		return null;
	}
	
	private WordToken findSubjectAfterPosIn(int index, WordToken posInToken){
		if(index==0) return null;  //change that to nto return.
		WordToken previous = wordTokens.get(index-1);
		if(previous.getPropertyValueType() ==PropertyValueTypes.PrepPhraseEnd)
			return null;
		return previous;
	}
	
	
	
	private WordToken findSubjectComplement(int verbIndex){
		for(int i=verbIndex+1;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(i-verbIndex==1){
				if(wordToken.getPos() == PartOfSpeachTypes.IN)
					return wordToken;
			}
			
			if(wordToken.getToken().equals(".")) return null;
			
			
			
			if(wordToken.getPropertyValueType()==PropertyValueTypes.NounPhraseEnd)
				return wordToken;
		}
		return null;	
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
