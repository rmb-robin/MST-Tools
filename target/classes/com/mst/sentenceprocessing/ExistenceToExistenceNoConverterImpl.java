package com.mst.sentenceprocessing;

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

    public ExistenceToExistenceNoConverterImpl() {
        tokenRelationFactory = new TokenRelationshipFactoryImpl();
    }

    @Override
    public List<TokenRelationship> convertExistenceNo(List<TokenRelationship> negationRelationships, List<TokenRelationship> tokenRelationships) {
        return createExistenceNoFromNegation(negationRelationships, tokenRelationships);
    }

    private List<TokenRelationship> createExistenceNoFromNegation(List<TokenRelationship> negationRelations, List<TokenRelationship> existingRelationships) {
        Map<String, TokenRelationship> relationshipByToToken = new HashMap<>();
        boolean shouldContinueProcessing = false;
        for (TokenRelationship negationRelationship : negationRelations) {
            if (!relationshipByToToken.containsKey(negationRelationship.getToToken().getToken()))
                relationshipByToToken.put(negationRelationship.getToToken().getToken(), negationRelationship);
            String pos = negationRelationship.getFromToken().getPos();
            if (pos == null)
                continue;
            if (!pos.equals(PartOfSpeachTypes.NEG))
                continue;
            shouldContinueProcessing = true;
        }
        if (!shouldContinueProcessing) return existingRelationships;
        return createExistenceNo(existingRelationships, relationshipByToToken);
    }

    private List<TokenRelationship> createExistenceNo(List<TokenRelationship> cummaltiveRelationships, Map<String, TokenRelationship> relationshipByToToken) {
        HashSet<String> matchingEdges = new HashSet<>();
        matchingEdges.add(EdgeNames.existence);
        HashSet<WordToken> matchedRelationships = new HashSet<>();
        for (TokenRelationship relationship : cummaltiveRelationships) {
            if (relationshipByToToken.containsKey(relationship.getFromToken().getToken())) {
                if (relationship.getEdgeName() != null && matchingEdges.contains(relationship.getEdgeName())) {
                    relationship.setEdgeName(EdgeNames.existenceNo);
                    TokenRelationship matched = relationshipByToToken.get(relationship.getFromToken().getToken());
                    matchedRelationships.add(matched.getToToken());
                }
            }
        }
        for (Map.Entry<String, TokenRelationship> entry : relationshipByToToken.entrySet()) {
            if (!matchedRelationships.contains(entry.getKey()))
                cummaltiveRelationships.add(tokenRelationFactory.create(EdgeNames.existenceNo, null, EdgeTypes.related, entry.getValue().getFromToken(), entry.getValue().getToToken(), this.getClass().getName()));
        }
        return cummaltiveRelationships;
    }
}





