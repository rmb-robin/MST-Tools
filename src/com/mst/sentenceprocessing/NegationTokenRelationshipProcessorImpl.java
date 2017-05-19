package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NegationTokenRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class NegationTokenRelationshipProcessorImpl implements NegationTokenRelationshipProcessor {

	
	private class SubjectSearchResult{	
		public int index;
		public WordToken subject;
	}
	
	private TokenRelationshipFactory tokenRelationshipFactory;
	
	
	public NegationTokenRelationshipProcessorImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	public List<TokenRelationship> process(List<WordToken> wordTokens){
		return getNegationEdges(wordTokens);
	}

	private List<TokenRelationship> getNegationEdges(List<WordToken> wordTokens){
		List<TokenRelationship> result = new ArrayList<>();
		for(int i = 0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(!wordToken.isVerb()) continue; 
			if(!wordToken.getVerb().getIsNegation()) continue; 
			if(i+1>= wordTokens.size()) return result;
			
			WordToken next = wordTokens.get(i+1);
			if(next.getPropertyValueType().equals(PropertyValueTypes.Subject) || next.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement)){
				result.add(createNegationEdge(wordToken,next));
				continue;
			}
			
			if(next.getPropertyValueType().equals(PropertyValueTypes.NounPhraseBegin)){
				SubjectSearchResult searchResult = findSubjectInPhrase(i+1, wordTokens);
				if(searchResult.subject!=null){
					result.add(createNegationEdge(wordToken,searchResult.subject));
				}
			
				i = searchResult.index;
				continue;
			}
			
			if(next.isVerb()){
				SubjectSearchResult searchResult = findSubjectForConjunctionVerbs(next,wordTokens,i);
			 if(result!=null) {
				 result.add(createNegationEdge(next, searchResult.subject));
				 i = searchResult.index;
				 continue;
			 }
			}
			
			//D) If verb isNegation=T, then create negation edge between av and subject complement.
		}
		return result;
	}
	
	private TokenRelationship createNegationEdge(WordToken from, WordToken to){
		return tokenRelationshipFactory.create(EdgeNames.negation, EdgeTypes.related, from, to);
	}
	
	private SubjectSearchResult findSubjectInPhrase(int index, List<WordToken> wordTokens){
		WordToken token=null;
		int i; 
		for(i=index+1;i<wordTokens.size();i++){
			WordToken current = wordTokens.get(i);
			if(current.getPropertyValueType().equals(PropertyValueTypes.Subject) ||
					current.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement))
				token = current;
			if(current.getPropertyValueType().equals(PropertyValueTypes.NounPhraseEnd) ||
			   current.getPropertyValueType().equals(PropertyValueTypes.PrepPhraseEnd))
				break;
		}
		SubjectSearchResult result = new SubjectSearchResult();
		result.index = i;
		result.subject = token;
		return result;
	}

	private SubjectSearchResult findSubjectForConjunctionVerbs(WordToken next,List<WordToken> wordTokens, int currentIndex){
		if(next.getVerb().getVerbType()!= VerbType.AV || next.getVerb().getVerbType()!= VerbType.LV) return null;
		if(currentIndex==0) return null;
		WordToken previous = wordTokens.get(currentIndex-1);
		if(!previous.isVerb()) return null;
		if(previous.getVerb().getVerbType()!=VerbType.LV) return null;

		for(int i =currentIndex+1; i<wordTokens.size();i++){
			WordToken current = wordTokens.get(i);
			if(current.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement))
			{
				SubjectSearchResult result = new SubjectSearchResult();
				result.index = i;
				result.subject = current;
				return result;
			}
		}
		return null;
	}
}
