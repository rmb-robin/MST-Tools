package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.common.escape.Escaper;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.interfaces.sentenceprocessing.VerbExistanceProcessor;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.Verb;
import com.mst.model.sentenceProcessing.WordToken;


public class VerbExistanceProcessorImpl implements VerbExistanceProcessor{

	private WordToken verb = null;
	private HashSet<WordToken> subjects =null;
	private HashSet<WordToken> subjectComplements = null;
    private WordToken modalVerb = null;
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
		if(subjects.isEmpty() && subjectComplements.isEmpty()) return null;
		List<TokenRelationship> matchedRelationships = getTokenRelationsForTokens(existingRelationships);

		if(isExistanceEdge(matchedRelationships)) return EdgeNames.existence;
		if(isExistanceNoEdge(matchedRelationships)) return EdgeNames.existenceNo;
		if(isExistancePossibilityEdge(matchedRelationships)) return EdgeNames.existencePossibility;
		if(isExistenceMaybeNoEdge(matchedRelationships)) return EdgeNames.existenceMaybe;
		
		return null;
	}
	

	private boolean isExistancePossibilityEdge(List<TokenRelationship> tokenRelationships){
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
		}
		return false;
	}
	
	private boolean isExistenceMaybeNoEdge(List<TokenRelationship> tokenRelationships){
		if(verb.getVerb().getVerbState().equals(EdgeNames.negation)) return true;
		if(modalVerb!=null){
			if(modalVerb.getVerb().getVerbState().equals(EdgeNames.possibility))return true;
		}

		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility))return true;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return true;
		}
		return false;
	}
	

	
	private boolean isExistanceEdge(List<TokenRelationship> tokenRelationships){
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName()==null) continue;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation))return false;
			if(tokenRelationship.getEdgeName().equals(EdgeNames.possibility)) return false;
		}
		if(modalVerb!=null){
			if(modalVerb.getVerb().getVerbState().equals(EdgeNames.possibility)) return false; 
		}
		Verb verbObj = verb.getVerb();
		if(verbObj.getVerbState().equals(EdgeNames.negation)) return false;
		return true;
	}
	
	private boolean isExistanceNoEdge(List<TokenRelationship> tokenRelationships){
		Verb verbObj = verb.getVerb();
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(EdgeNames.negation)){
				if(verbObj.getVerbState().equals(EdgeNames.negation)) return true; 
				if(verbObj.getVerbType()==VerbType.AV && verbObj.isExistance()) return true;
			}
				return true;
		}
		return false;					
	}
	
	
	private List<TokenRelationship> getTokenRelationsForTokens(List<TokenRelationship> existingRelationships){
		List<TokenRelationship> result = new ArrayList<>();
		for(TokenRelationship tokenRelationship: existingRelationships){
			if(subjects.contains(tokenRelationship.getToToken()) || subjectComplements.contains(tokenRelationship.getToToken())){
				result.add(tokenRelationship);
				continue;
			}
			if(subjects.contains(tokenRelationship.getFromToken())|| subjectComplements.contains(tokenRelationship.getFromToken()))
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
		subjects = new HashSet<>();
		subjectComplements = new HashSet<>();
		modalVerb = null;
		
		for(WordToken wordToken: tokens){
			if(wordToken.isVerb()){
				if(wordToken.getVerb().getVerbType()==VerbType.MV)
					modalVerb = wordToken;
				else 
					verb = wordToken; 
				continue;
			}
			if(wordToken.getPropertyValueType()!=null && wordToken.getPropertyValueType().equals(PropertyValueTypes.Subject))
			{
				subjects.add(wordToken);
				continue;
			}
			
			if(wordToken.getPropertyValueType()!=null && wordToken.getPropertyValueType().equals(PropertyValueTypes.SubjectComplement)){
				subjectComplements.add(wordToken);
			}
		}
	}	
		
	private List<TokenRelationship> createRelationshipsForAll(String edgeName){
		List<TokenRelationship> result = new ArrayList<>();
		
		for(WordToken subject: subjects){
			for(WordToken  subjectComplement: subjectComplements){
				result.add(tokenRelationshipFactory.create(edgeName, EdgeTypes.related, subject, subjectComplement));
			}
		}
		return result;
	}

	private List<TokenRelationship> createNonHasEdges(String edgeName){
		
		if(shouldCreateSubjectSubjectComplementEdge()) 
		{ 
			return createRelationshipsForAll(edgeName);
		}
		List<TokenRelationship> relationships = new ArrayList<>();
		if(shouldCreateVerbNetEdge()){
			
			relationships.addAll(createRelationshipsForAll(edgeName));
			relationships.addAll(createRelationshipsForAll(verb.getVerb().getVerbNetClass()));
			return relationships;
		}
		return relationships;
	}
		
	private boolean shouldCreateVerbNetEdge(){
		if(subjects.isEmpty()) return false;
		if(subjectComplements.isEmpty()) return false;
		Verb verbObject = verb.getVerb();

		if(verbObject.getVerbType()!=VerbType.AV) return false;
		return (verbObject.isExistance() && verbObject.getIsMaintainVerbNetClass());
	}
	
	private boolean shouldCreateSubjectSubjectComplementEdge(){
		if(subjects.isEmpty()) return false;
		if(subjectComplements.isEmpty()) return false;
		
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
			return processEdgesForHasVerb(sentence,nounPhraseEnd);
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
		if(isExistanceEdgeForHas(matchedTokenRelationship)) return EdgeNames.existence;
		if(isExistanceNoEdgeForHas(matchedTokenRelationship)) return EdgeNames.existenceNo;
		if(isExistancePossibilityEdge(matchedTokenRelationship)) return EdgeNames.existencePossibility;
		if(tryCreatExistanceMaybeNo(matchedTokenRelationship)) return EdgeNames.existenceMaybe;		
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
