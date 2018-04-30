package com.mst.model.sentenceProcessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.AdditionalExistenceEdgeProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.sentenceprocessing.TokenRelationshipFactoryImpl;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class AdditionalExistenceEdgeProcesserImpl implements AdditionalExistenceEdgeProcesser {

	private TokenRelationshipFactory tokenRelationshipFactory;
	
	public AdditionalExistenceEdgeProcesserImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	public TokenRelationship process(Sentence sentence) {
		if(sentence.getTokenRelationships()==null || sentence.getTokenRelationships().isEmpty())
			return null;
		
		WordToken dysnToken = sentence.getTokenBySemanticType(SemanticTypes.dysn);
		return createRelationship(sentence.getTokenRelationships(), dysnToken,false);
	}
	
	
	
	
	@Override
	public RecommendedTokenRelationship processDiscovery(SentenceDiscovery discovery) {
		WordToken dysnToken = discovery.getTokenBySemanticType(SemanticTypes.dysn);
		List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(discovery.getWordEmbeddings());
		TokenRelationship relationship = createRelationship(relationships, dysnToken,true);
		if(relationship==null)return null;
		
		return tokenRelationshipFactory.createRecommendedRelationshipFromTokenRelationship(relationship);

	}
	
	private TokenRelationship createRelationship(List<TokenRelationship> relationships, WordToken dysnToken, boolean isNamed){
		if(dysnToken==null) return null;
		
		WordToken oppositeEdge = null;
		for(TokenRelationship relationship: relationships){
			String edgeName = relationship.getEdgeName();
			if(isNamed)
				edgeName = relationship.getNamedEdge();
			if(edgeName==null) continue;
			
			
			if(doesEdgeEqualExistance(edgeName)){
				if(relationship.isToFromTokenMatch(dysnToken.getToken())) return null;
			}
			
			if(oppositeEdge==null && doesEdgeEqualModifier(edgeName)){
				WordToken opposite = getOppositeToken(dysnToken.getToken(), relationship);
				if(opposite!=null) oppositeEdge = opposite;
			}
		}
		if(oppositeEdge!=null)
			return tokenRelationshipFactory.create(EdgeNames.existence,EdgeTypes.related, dysnToken,oppositeEdge,this.getClass().getName());
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

	
	
}
