package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.sentenceprocessing.TokenRelationshipFactoryImpl;

public class AdditionalExistenceEdgeProcesserImpl implements AdditionalExistenceEdgeProcesser {

	private TokenRelationshipFactory tokenRelationshipFactory;
	
	public AdditionalExistenceEdgeProcesserImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	public TokenRelationship process(Sentence sentence) {
		if(sentence.getTokenRelationships()==null || sentence.getTokenRelationships().isEmpty())
			return null;
		
		WordToken dysnToken = sentence.getTokenBySemanticType(SemanticTypes.dysn);
		if(dysnToken==null) return null;
		
		WordToken oppositeEdge = null;
		for(TokenRelationship relationship: sentence.getTokenRelationships()){
			String edgeName = relationship.getEdgeName();
			if(doesEdgeEqualExistance(edgeName)){
				if(isDsynTokenOnEdge(dysnToken.getToken(),relationship)) return null;
			}
			
			if(oppositeEdge==null && doesEdgeEqualModifier(edgeName)){
				WordToken opposite = getOppositeToken(dysnToken.getToken(), relationship);
				if(opposite!=null) oppositeEdge = opposite;
			}
		}
		if(oppositeEdge!=null)
			return tokenRelationshipFactory.create(EdgeNames.existence,EdgeTypes.related, dysnToken,oppositeEdge);
		return null;
	}
	
	private boolean doesEdgeEqualExistance(String edgeName){
		return edgeName.equals(EdgeNames.existence) || edgeName.equals(EdgeNames.existenceMaybe) 
				|| edgeName.equals(EdgeNames.existenceNo);
	}
	
	private boolean doesEdgeEqualModifier(String edgeName){
		return edgeName.equals(EdgeNames.simpleCystModifier) 
				||edgeName.equals(EdgeNames.simpleCystModifiers)
				|| edgeName.equals(EdgeNames.diseaseModifier) 
				|| edgeName.equals(EdgeNames.diseaseLocation);
	}
	
	
	private WordToken getOppositeToken(String dysnToken, TokenRelationship relationship){
		if(dysnToken.equals(relationship.getToToken().getToken())) return relationship.getFromToken();
		if(dysnToken.equals(relationship.getFromToken().getToken())) return relationship.getToToken();
		return null;
	}
	
	private boolean isDsynTokenOnEdge(String dysnToken, TokenRelationship relationship){
		if(relationship.getToToken().getToken().equals(dysnToken)) return true;
		if(relationship.getFromToken().getToken().equals(dysnToken)) return true; 
		return false;
	}

}
