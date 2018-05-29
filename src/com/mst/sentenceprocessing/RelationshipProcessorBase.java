package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.WordToken;

public abstract class RelationshipProcessorBase {
    TokenRelationshipFactory tokenRelationshipFactory;
    final String wildcard = "*";
    protected List<WordToken> wordTokens;
    Map<String, List<RelationshipMapping>> relationshipMap;
    Map<String, List<RelationshipMapping>> semanticTypeRelationshipMap;

    RelationshipProcessorBase() {
        tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
    }

    int getEndIndex(int index, int distance) {
        return Math.min(index + distance, wordTokens.size() - 1);
    }

    boolean isWordTokenMatchToRelationship(boolean isSemanticType, boolean isPosType, String relationshipToToken, WordToken toToken) {
        String tokenCompareValue = toToken.getToken();
        if (isSemanticType)
            tokenCompareValue = toToken.getSemanticType();
        else if (isPosType)
            tokenCompareValue = toToken.getPos();
        if (tokenCompareValue == null)
            return false;
        return tokenCompareValue.equals(relationshipToToken);
    }

    void setRelationshipMaps(List<RelationshipMapping> relationshipMappings) {
        relationshipMap = new HashMap<>();
        semanticTypeRelationshipMap = new HashMap<>();
        for (RelationshipMapping nounRelationship : relationshipMappings) {
            if (nounRelationship.getIsFromSemanticType())
                setRelationshipMap(semanticTypeRelationshipMap, nounRelationship);
            else
                setRelationshipMap(relationshipMap, nounRelationship);
        }
    }

    private void setRelationshipMap(Map<String, List<RelationshipMapping>> map, RelationshipMapping nounRelationship) {
        if (!map.containsKey(nounRelationship.getFromToken()))
            map.put(nounRelationship.getFromToken().toLowerCase(), new ArrayList<>());
        map.get(nounRelationship.getFromToken()).add(nounRelationship);
    }
}
