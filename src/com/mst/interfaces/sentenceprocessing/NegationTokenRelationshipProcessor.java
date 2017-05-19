package com.mst.interfaces.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.TokenRelationshipFactoryImpl;

public class NegationTokenRelationshipProcessor {

	
	private class NounPhraseResult{	
		public int index;
		public WordToken subject;
	}
	
	private WordToken subject, subjectComplement, nounPhraseEnd; 
	private TokenRelationshipFactory tokenRelationshipFactory;
	
	
	public NegationTokenRelationshipProcessor(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	public List<TokenRelationship> process(List<WordToken> wordTokens){
		setTokens(wordTokens);
		return getNegationEdges(wordTokens);
	}

	private void setTokens(List<WordToken> wordTokens){
		subject = null;
		subjectComplement = null;
		nounPhraseEnd = null;
		
		for(WordToken token: wordTokens){
			if(token.getPropertyValueType().equals(PropertyValueTypes.Subject) && subject==null){
				subject = token; 
				continue;
			}
		
			if(token.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement) && subjectComplement==null){
				subjectComplement = token; 
				continue;
			}
		
			
			if(token.getPropertyValueType().equals(PropertyValueTypes.NounPhraseEnd) && nounPhraseEnd==null){
				nounPhraseEnd = token; 
				continue;
			}
		}
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
				NounPhraseResult nounPhraseResult = findSubjectInPhrase(i+1, wordTokens);
				if(nounPhraseResult.subject!=null){
					result.add(createNegationEdge(wordToken,subject));
				}
			
				i = nounPhraseResult.index;
				continue;
			}
		}
		return result;
	}
	
	private TokenRelationship createNegationEdge(WordToken from, WordToken to){
		return tokenRelationshipFactory.create(EdgeNames.negation, EdgeTypes.related, from, to);
	}
	
	private NounPhraseResult findSubjectInPhrase(int index, List<WordToken> wordTokens){
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
		NounPhraseResult result = new NounPhraseResult();
		result.index = i;
		result.subject = token;
		return result;
	}

	/*
	 * A) If negation token immediately precedes either the subject or subject complement, then create negation edge between either the subject or subject complement
	 *  and verb. Ex. She has no cyst.

B) If negation token immediately precedes NounPhraseBegin where subject or subject complement is the final token, then create negation edge between either the subject or subject complement and verb. 
Ex. No ovarian cyst was seen on CT.

C) If negation token follows lv but precedes av, then create negation edge between av and subject complement. Ex. CT does not show a cyst.
If negation token follows lv but precedes another lv, then create negation edge between second lv and subject complement. Ex. She does not have a cyst. If negation token follows mv but precedes lv, then create negation edge between lv and subject complement. Ex. She may not have a cyst after all. If negation token follows mv but precedes av, then create negation edge between av and subject complement. Ex. CT may not show the cyst.

D) If verb isNegation=T, then create negation edge between av and subject complement.
	 * 
	 * 
	 * 
	 */
	
}
