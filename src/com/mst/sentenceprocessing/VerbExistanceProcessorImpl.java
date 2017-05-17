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
import com.mst.tools.GraphProcessor.EdgeDirection;

public class VerbExistanceProcessorImpl {

	private class MatchedTokenResult{
		public TokenRelationship tokenRelationship;
		public WordToken oppositeToken; 
	}
	
	private WordToken verb = null;
	private WordToken subject =null;
	private WordToken subjectComplement = null;
	private List<TokenRelationship> tokenRelationships;
    private TokenRelationshipFactory tokenRelationshipFactory; 
 
    
    
    public VerbExistanceProcessorImpl(){
    	tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
    }
    
	public List<TokenRelationship> process(Sentence sentence){
		//tokenRelationships = new ArrayList<>();
		setVerbPhraseTokens(sentence.getModifiedWordList());
		String edgeName = getEdgeName(sentence);
		if(edgeName==null) return new ArrayList<TokenRelationship>();
		return createExistanceEdges(edgeName);
	}
	
	private String getEdgeName(Sentence sentence){
		List<TokenRelationship> existingRelationships = sentence.getTokenRelationships();
		if(existingRelationships==null)return null;
		if(existingRelationships.isEmpty()) return null;
		if(subject==null || subjectComplement==null) return null;
		if(verb==null) return null;
		List<TokenRelationship> matchedRelationships = getTokenRelationsForTokens(existingRelationships);
		if(matchedRelationships.isEmpty())return null;
	
		if(isExistanceEdge(matchedRelationships)) return EdgeNames.existance;
		if(isExistanceNoEdge(matchedRelationships)) return EdgeNames.existanceNo;
		if(isExistancePossibilityEdge(matchedRelationships)) return EdgeNames.existancePossibility;
		if(isExistenceMaybeNoEdge(matchedRelationships)) return EdgeNames.existanceMaybeNo;
		
		//might need to move this..

		if(!sentence.doesSentenceContainVerb())	
			sentence.addHasVerb();
		
		return EdgeNames.existance;
	}
	
	private boolean isExistenceMaybeNoEdge(List<TokenRelationship> tokenRelationships){
		Verb verbObj = verb.getVerb();
		if(verbObj!=null){
			if(verbObj.getVerbState().equals(EdgeNames.possibility) ||verbObj.getVerbState().equals(EdgeNames.negation)) 
				return true;
		}
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return true;
		}
		return false;
	}
	
	private boolean isExistancePossibilityEdge(List<TokenRelationship> tokenRelationships){
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
		}
		return false;
	}
	
	private boolean isExistanceEdge(List<TokenRelationship> tokenRelationships){
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return false;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility)) return false;
		}
		
		Verb verbObj = verb.getVerb();
		if(verbObj==null) return false;
		
		if(verb.getVerb().getVerbState()=="possibility") return false;
		if(verbObj.getVerbState().equals(EdgeNames.possibility)) return false;
		if(verbObj.getVerbState().equals(EdgeNames.negation)) return false;
		
		return true;
	}
	
	private boolean isExistanceNoEdge(List<TokenRelationship> tokenRelationships){
		Verb verbObj = verb.getVerb();
		if(verbObj==null) return false;
		
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
	
	
//	If any fromtoken or totoken has edgename “negation” or “possibility”, then create edge “existence-maybe-no” between “has” and NounPhraseEnd (in a sentence without a verb, the final token is always going to be NounPhraseEnd). Ex. Has no probable cyst.
//	If any fromtoken or totoken has edgename “negation”, then create edge “existence-no” between “has” and NounPhraseEnd. Ex. Has no cyst.
//	If any fromtoken or totoken has edgename “possibility”, then create edge “existence-possibility” between “has” and NounPhraseEnd. Ex. Has possible simple cyst.
//	Else, create edge “existence” between “has” and NounPhraseEnd. Ex. Has simple cyst.
//	Note to Self: After adding verb “has” was thinking about processing using same code as for verbs but that will not work because the sentence will not contain a subject.
//	

	
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
		
	private List<TokenRelationship> createExistanceEdges(String edgeName){
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
		if(verb==null) return false;
		if(subject==null) return false;
		if(subjectComplement==null) return false;
		Verb verbObject = verb.getVerb();
		if(verbObject==null) return false;

		if(verbObject.getVerbType()!=VerbType.AV) return false;
		return (verbObject.isExistance() && verbObject.getIsMaintainVerbNetClass());
	}
	
	private boolean shouldCreateSubjectSubjectComplementEdge(){
		if(verb==null) return false;
		if(subject==null) return false;
		if(subjectComplement==null) return false;

		Verb verbObject = verb.getVerb();
		if(verbObject==null) return false;
		
		if(verbObject.getVerbType()==VerbType.LV) return true;
		
		if(verbObject.getVerbType()==VerbType.AV){
			if(verbObject.isExistance() && !verbObject.getIsMaintainVerbNetClass()) return true;
			if(!verbObject.isExistance() && !verbObject.getIsMaintainVerbNetClass()) return true;
		}
	
		return false;
	}	
}
