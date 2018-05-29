package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class DistinctTokenRelationshipDeterminerImpl implements DistinctTokenRelationshipDeterminer {

    @Override
    public List<TokenRelationship> getDistinctTokenRelationships(Sentence sentence) {
        List<TokenRelationship> result = new ArrayList<>();
        Map<String, List<TokenRelationship>> tokenRelationsByEdgeName = sentence.getTokenRelationsByNameMap();
        for (Entry<String, List<TokenRelationship>> entry : tokenRelationsByEdgeName.entrySet()) {
            result.addAll(getDistinctRelationships(entry.getValue()));
        }
        return result;
    }

    public List<RecommendedTokenRelationship> getDistinctRecommendedRelationships(List<RecommendedTokenRelationship> relationships) {
        Map<String, List<RecommendedTokenRelationship>> map = RecommandedTokenRelationshipUtil.getRelationshipsByEdgeName(relationships);
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        for (Entry<String, List<RecommendedTokenRelationship>> entry : map.entrySet()) {
            result.addAll(getDistinctRecommendRelationships(entry.getValue()));
        }
        return result;
    }

    private List<RecommendedTokenRelationship> getDistinctRecommendRelationships(List<RecommendedTokenRelationship> tokenRelationships) {
        Map<String, RecommendedTokenRelationship> distinctFromTo = new HashMap<>();
        for (RecommendedTokenRelationship tokenRelationship : tokenRelationships) {
            String key = getToken(tokenRelationship.getTokenRelationship().getFromToken()) + getToken(tokenRelationship.getTokenRelationship().getToToken());
            if (distinctFromTo.containsKey(key)) continue;
            distinctFromTo.put(key, tokenRelationship);
        }
        return new ArrayList<>(distinctFromTo.values());
    }

    private List<TokenRelationship> getDistinctRelationships(List<TokenRelationship> tokenRelationships) {
        Map<String, TokenRelationship> distinctFromTo = new HashMap<>();
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            String key = getToken(tokenRelationship.getFromToken()) + getToken(tokenRelationship.getToToken());
            if (distinctFromTo.containsKey(key)) continue;
            distinctFromTo.put(key, tokenRelationship);
        }
        return new ArrayList<>(distinctFromTo.values());
    }

    private String getToken(WordToken wordtoken) {
        if (wordtoken == null) return "";
        return wordtoken.getToken().trim();
    }
}
