package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.ExistenceToExistenceNoConverter;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class ExistenceToExistenceNoConverterImpl implements ExistenceToExistenceNoConverter {

	private TokenRelationshipFactory tokenRelationFactory; 
	
	public ExistenceToExistenceNoConverterImpl(){
		tokenRelationFactory = new TokenRelationshipFactoryImpl();
	}
	@Override
	public List<TokenRelationship> convertExistenceNo(List<TokenRelationship> negationRelationships, List<TokenRelationship> existingRelationships){
		
		return createExistenceNoFromNegation(negationRelationships, existingRelationships);
	}


	private List<TokenRelationship> createExistenceNoFromNegation(List<TokenRelationship> negationRelations, List<TokenRelationship> existingRelationships){
	
		Map<WordToken,TokenRelationship> relationshipByToToken = new HashMap<>();

		List<TokenRelationship> cummaltiveRelationships = new ArrayList<>();
		cummaltiveRelationships.addAll(existingRelationships);
		cummaltiveRelationships.addAll(negationRelations);

		boolean shouldContinueProcessing = false;
		for(TokenRelationship negationRelationship : negationRelations ){
			if(!relationshipByToToken.containsKey(negationRelationship.getToToken()))
			relationshipByToToken.put(negationRelationship.getToToken(),negationRelationship);
			String pos = negationRelationship.getFromToken().getPos();
			if(pos==null) continue;
			if(pos!= PartOfSpeachTypes.NEG) continue;
			shouldContinueProcessing = true;
		}
		
		if(!shouldContinueProcessing) return cummaltiveRelationships;
		return createExistenceNo(cummaltiveRelationships, relationshipByToToken);
	}

	private List<TokenRelationship> createExistenceNo(List<TokenRelationship> cummaltiveRelationships,Map<WordToken,TokenRelationship> relationshipByToToken){
		
		HashSet<WordToken> matchedRelationships = new HashSet<>();
		for(TokenRelationship relationship: cummaltiveRelationships){
			if(relationshipByToToken.containsKey(relationship.getFromToken())){		
				if(relationship.getEdgeName().equals(EdgeNames.existence)){
					relationship.setEdgeName(EdgeNames.existenceNo);
				}
			matchedRelationships.add(relationshipByToToken.get(relationship.getFromToken()).getToToken());
			}	
		}
		
		
		for(Map.Entry<WordToken, TokenRelationship> entry : relationshipByToToken.entrySet()){
			
			if(!matchedRelationships.contains(entry.getKey()))
			  cummaltiveRelationships.add(tokenRelationFactory.
					  create(EdgeNames.existenceNo, EdgeTypes.related, entry.getValue().getFromToken(), entry.getValue().getToToken()));
		}
		return cummaltiveRelationships;
	}
}





