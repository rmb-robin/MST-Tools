package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.Verb;
import com.mst.model.sentenceProcessing.WordToken;


public class VerbExistanceProcessorImpl {

	private WordToken verb = null;
	private WordToken subject =null;
	private WordToken subjectComplement = null;
    private TokenRelationshipFactory tokenRelationshipFactory; 
 
    public VerbExistanceProcessorImpl(){
    	tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
    }
    
	public List<TokenRelationship> process(Sentence sentence){
		 
		setVerbPhraseTokens(sentence.getModifiedWordList());
		if(isVerbValid()){
			String edgeName = getEdgeNameforNotHas(sentence);
			if(edgeName !=null) 
				return createNonHasEdges(edgeName);
		}
		
		TokenRelationship relationship = createHasEdge(sentence);
		List<TokenRelationship> relationships = new ArrayList<>();
		if(relationship!=null) relationships.add(relationship);
		return relationships;
	}
	
	

	private boolean isVerbValid(){
		if(verb==null) return false;
		if(verb.getVerb()==null) return false;
		return true;
	}
	
	private String getEdgeNameforNotHas(Sentence sentence){
		List<TokenRelationship> existingRelationships = sentence.getTokenRelationships();
		if(existingRelationships==null)return null;
		if(existingRelationships.isEmpty()) return null;
		if(subject==null || subjectComplement==null) return null;
		List<TokenRelationship> matchedRelationships = getTokenRelationsForTokens(existingRelationships);
		if(matchedRelationships.isEmpty())return null;
	
		if(isExistanceEdge(matchedRelationships)) return EdgeNames.existance;
		if(isExistanceNoEdge(matchedRelationships)) return EdgeNames.existanceNo;
		if(isExistancePossibilityEdge(matchedRelationships)) return EdgeNames.existancePossibility;
		if(isExistenceMaybeNoEdge(matchedRelationships)) return EdgeNames.existanceMaybeNo;
		
		return null;
	}
	

	private boolean isExistancePossibilityEdge(List<TokenRelationship> tokenRelationships){
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
		}
		return false;
	}
	
	private boolean isExistenceMaybeNoEdge(List<TokenRelationship> tokenRelationships){
		Verb verbObj = verb.getVerb();
		if(verbObj.getVerbState().equals(EdgeNames.possibility) ||verbObj.getVerbState().equals(EdgeNames.negation)) 
			return true;
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return true;
		}
		return false;
	}
	

	
	private boolean isExistanceEdge(List<TokenRelationship> tokenRelationships){
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return false;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility)) return false;
		}
		
		Verb verbObj = verb.getVerb();
		if(verbObj.getVerbState().equals(EdgeNames.possibility)) return false;
		if(verbObj.getVerbState().equals(EdgeNames.negation)) return false;
		return true;
	}
	
	private boolean isExistanceNoEdge(List<TokenRelationship> tokenRelationships){
		Verb verbObj = verb.getVerb();
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation) && verbObj.getVerbState().equals(EdgeNames.negation)) 
				return true;
		}
		return false;					
	}
	
	
	private List<TokenRelationship> getTokenRelationsForTokens(List<TokenRelationship> existingRelationships){
		List<TokenRelationship> result = new ArrayList<>();
		for(TokenRelationship tokenRelationship: existingRelationships){
			if(tokenRelationship.getToToken().equals(subject) || tokenRelationship.getToToken().equals(subjectComplement)){
				result.add(tokenRelationship);
				continue;
			}
			if(tokenRelationship.getFromToken().equals(subject) || tokenRelationship.getFromToken().equals(subjectComplement))
				result.add(tokenRelationship);
		}
		return result;
	}
		
	private List<TokenRelationship> getTokenRelationshipForSingleToken(List<TokenRelationship> existingRelationships, WordToken token){
		List<TokenRelationship> result = new ArrayList<>();
		for(TokenRelationship tokenRelationship: existingRelationships){
			if(tokenRelationship.getToToken().equals(token)){
				result.add(tokenRelationship);
				continue;
			}
			if(tokenRelationship.getFromToken().equals(token))
				result.add(tokenRelationship);
		}
		return result;
	}	
	
	
	
	private void setVerbPhraseTokens(List<WordToken> tokens){
		
		verb = null;
		subject = null;
		subjectComplement = null;
		
		for(WordToken wordToken: tokens){
			if(wordToken.isVerb()){
				verb = wordToken; 
				continue;
			}
			if(wordToken.getPropertyValueType().equals(PropertyValueTypes.Subject) && subject==null)
			{
				subject = wordToken;
				continue;
			}
			
			if(wordToken.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement) && subjectComplement == null){
				subjectComplement = wordToken;
			}
		}
	}	
		
	private List<TokenRelationship> createNonHasEdges(String edgeName){
		List<TokenRelationship> relationships = new ArrayList<>();
		if(shouldCreateSubjectSubjectComplementEdge()) 
		{ 
			relationships.add(tokenRelationshipFactory.create(edgeName, EdgeTypes.related, subject, subjectComplement));
			return relationships;
		}
		if(shouldCreateVerbNetEdge()){
			relationships.add(tokenRelationshipFactory.create(edgeName, EdgeTypes.related, subject, subjectComplement));
			relationships.add(tokenRelationshipFactory.create(verb.getVerb().getVerbNetClass(), EdgeTypes.related, subject, subjectComplement));
			return relationships;
		}
		return relationships;
	}
		
	private boolean shouldCreateVerbNetEdge(){
		if(subject==null) return false;
		if(subjectComplement==null) return false;
		Verb verbObject = verb.getVerb();

		if(verbObject.getVerbType()!=VerbType.AV) return false;
		return (verbObject.isExistance() && verbObject.getIsMaintainVerbNetClass());
	}
	
	private boolean shouldCreateSubjectSubjectComplementEdge(){
		if(subject==null) return false;
		if(subjectComplement==null) return false;
		
		Verb verbObject = verb.getVerb();		
		if(verbObject.getVerbType()==VerbType.LV) return true;
		
		if(verbObject.getVerbType()==VerbType.AV){
			if(verbObject.isExistance() && !verbObject.getIsMaintainVerbNetClass()) return true;
			if(!verbObject.isExistance() && !verbObject.getIsMaintainVerbNetClass()) return true;
		}
	
		return false;
	}	
	
	
	//E
	private TokenRelationship createHasEdge(Sentence sentence){
		WordToken nounPhraseEnd = getNounPhraseEnd(sentence.getModifiedWordList());
		if(nounPhraseEnd==null) return null;
		
		if(!sentence.doesSentenceContainVerb())	{
			sentence.addHasVerb();
			return processEdgesForHasVerb(sentence,null);
		}
		return null;
		
		
	}
	
	private TokenRelationship processEdgesForHasVerb(Sentence sentence, WordToken nounPhrase){
		List<TokenRelationship> matchedTokenRelationship = getTokenRelationshipForSingleToken(sentence.getTokenRelationships(),nounPhrase);
		String edgeName = getEdgeNameforNHas(matchedTokenRelationship);
		if(edgeName==null) return null;
		WordToken hasVerb = sentence.getModifiedWordList().get(0);
		return tokenRelationshipFactory.create(edgeName, EdgeTypes.related, hasVerb, nounPhrase);
	}
	
	private String getEdgeNameforNHas(List<TokenRelationship> matchedTokenRelationship){
		if(isExistanceEdgeForHas(matchedTokenRelationship)) return EdgeNames.existance;
		if(isExistanceNoEdgeForHas(matchedTokenRelationship)) return EdgeNames.existanceNo;
		if(isExistancePossibilityEdge(matchedTokenRelationship)) return EdgeNames.existancePossibility;
		if(tryCreatExistanceMaybeNo(matchedTokenRelationship)) return EdgeNames.existanceMaybeNo;		
		return null;
	}
	
	private boolean tryCreatExistanceMaybeNo(List<TokenRelationship> matchedTokenRelationship){
		for(TokenRelationship tokenRelationship: matchedTokenRelationship){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility) || tokenRelationship.getEdgeName().equals(EdgeNames.negation) ) 
				return true;
		}
		return false;
	}

	
	private boolean isExistanceNoEdgeForHas(List<TokenRelationship> matchedTokenRelationship){
		for(TokenRelationship tokenRelationship: matchedTokenRelationship){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation)) 
				return true;
		}
		
		return false;
	}
	
	private boolean isExistanceEdgeForHas(List<TokenRelationship> matchedTokenRelationship){
		for(TokenRelationship tokenRelationship: matchedTokenRelationship){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation)) return false;
		}
		return true;
		
	}
	
	private WordToken getNounPhraseEnd(List<WordToken> tokens){
		for(WordToken token: tokens){
			if(token.getPropertyValueType().equals(PropertyValueTypes.NounPhraseEnd)) return token;
		}
		return null;
	}
	
	
}
