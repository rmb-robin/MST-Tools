package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.NegationTokenRelationshipProcessor;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class NegationTokenRelationshipProcessorImpl implements NegationTokenRelationshipProcessor {

	
	private class SubjectSearchResult{	
		public int index;
		public WordToken subject;
		public WordToken from;
	}
	
	private TokenRelationshipFactory tokenRelationshipFactory;
	
	
	public NegationTokenRelationshipProcessorImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	public List<TokenRelationship> process(List<WordToken> wordTokens){
		return getNegationEdges(wordTokens);
	}

	private boolean shouldContinueLooping(WordToken token){
		if(!token.isVerb()){
			if(token.getPos()==null) return true;
			if(!token.getPos().equals(PartOfSpeachTypes.NEG))return true;
			return false;
		}
		
		if(token.getVerb()==null) return true;
		return !token.getVerb().getIsNegation();
	}
	
	private List<TokenRelationship> getNegationEdges(List<WordToken> wordTokens){
		List<TokenRelationship> result = new ArrayList<>();
		WordToken verb= null;
		List<WordToken> negationTokens = new ArrayList<>();
		
		for(int i = 0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);

			if(wordToken.isVerb()){
				verb = wordToken;
				SubjectSearchResult subjectSearchResult = findSubjectForConjunctionVerbs(verb,wordTokens,i);
				if(subjectSearchResult!=null){
					result.add(createNegationEdge(subjectSearchResult.from, subjectSearchResult.subject));
					i = subjectSearchResult.index;
					continue;
				}
			}
			
			if(shouldContinueLooping(wordToken)) continue;
			negationTokens.add(wordToken);
			
			if(i+1>= wordTokens.size()) return result;
			
			WordToken next = wordTokens.get(i+1);
			
			//A
			if(next.getPropertyValueType()!=null && 
					(next.getPropertyValueType().equals(PropertyValueTypes.Subject) 
							|| next.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement))){
				if(verb!=null){
					result.add(createNegationEdge(verb,next));
				}
				continue;
			}
			
			if(next.getPropertyValueType()!=null && next.getPropertyValueType().equals(PropertyValueTypes.NounPhraseBegin)){
				SubjectSearchResult searchResult = findSubjectInPhrase(i+1, wordTokens,verb,wordToken);
				if(searchResult.subject!=null){
					result.add(createNegationEdge(searchResult.from,searchResult.subject));
				}
			
				i = searchResult.index;
				continue;
			}
		}
		if(result.size()==0)
			result = getAllNegationTokenEdges(negationTokens, wordTokens);
		return result;
	}
	
	
	private List<TokenRelationship> getAllNegationTokenEdges(List<WordToken> negationTokens, List<WordToken> wordTokens){
		List<TokenRelationship> result = new ArrayList<>();
		for(WordToken negationToken:negationTokens){
			if(wordTokens.indexOf(negationToken) < wordTokens.size()-1)
				result.add(createNegationEdge(negationToken, wordTokens.get(wordTokens.indexOf(negationToken)+1)));
		}
		return result;
		
	}
	
	//B2) If negation token precedes NounPhrase begin where NounPhraseFinal is not a subject or 
	//subject complement, then create negation edge between the NounPhraseFinal token and the negation token.
	private TokenRelationship createNegationEdge(WordToken from, WordToken to){
		return tokenRelationshipFactory.create(EdgeNames.negation, EdgeTypes.related, from, to);
	}
	
	private SubjectSearchResult findSubjectInPhrase(int index, List<WordToken> wordTokens, WordToken verb, WordToken negation){
		WordToken token=null;
		SubjectSearchResult result = new SubjectSearchResult();
		
		int i; 
		for(i=index+1;i<wordTokens.size();i++){
			WordToken current = wordTokens.get(i);
			
			if(current.getPropertyValueType()==null) continue;
			if(current.getPropertyValueType().equals(PropertyValueTypes.Subject) ||
					current.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement) & verb!=null){
				token = current;
				result.from = verb;
			}
			if(current.getPropertyValueType().equals(PropertyValueTypes.NounPhraseEnd) ||
			   current.getPropertyValueType().equals(PropertyValueTypes.PrepPhraseEnd))
			{
				if(token==null){
					token = current;
					result.from = negation;
				}
			}
		}
		result.index = i;
		result.subject = token;
		return result;
	}

	
	private SubjectSearchResult findSubjectForConjunctionVerbs(WordToken  firstVerb,List<WordToken> wordTokens, int currentIndex){
		if(currentIndex+2>= wordTokens.size()) return null;
		
		if(firstVerb.getVerb().getVerbType()!= VerbType.MV && firstVerb.getVerb().getVerbType()!= VerbType.LV)return null;
		WordToken nextToken = wordTokens.get(currentIndex+1);
		if(nextToken.getPos()==null) return null;
		if(!nextToken.getPos().equals(PartOfSpeachTypes.NEG))return null;
		WordToken secondVerb = wordTokens.get(currentIndex+2);
		if(!secondVerb.isVerb()) return null;
		if(secondVerb.getVerb().getVerbType()!= VerbType.AV && secondVerb.getVerb().getVerbType()!= VerbType.LV)return null;
		
		for(int i =currentIndex+1; i<wordTokens.size();i++){
			WordToken current = wordTokens.get(i);
			if(current.getPropertyValueType()==null) continue;
			if(current.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement))
			{
				SubjectSearchResult result = new SubjectSearchResult();
				result.index = i;
				result.subject = current;
				result.from = secondVerb;
				return result;
			}
		}
		return null;
	}
}
