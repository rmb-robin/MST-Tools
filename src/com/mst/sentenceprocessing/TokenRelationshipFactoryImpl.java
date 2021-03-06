package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.jsonSerializers.DeepCloner;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class TokenRelationshipFactoryImpl implements TokenRelationshipFactory {

    @Override
    public TokenRelationship create(String edgeName, String descriptor, String frameName, WordToken fromToken, WordToken toToken, String source) {
        TokenRelationship tokenRelationship = new TokenRelationship();
        tokenRelationship.setUniqueIdentifier(UUID.randomUUID().toString());
        tokenRelationship.setEdgeName(edgeName);
        tokenRelationship.setDescriptor(descriptor);
        tokenRelationship.setFrameName(frameName);
        tokenRelationship.setSource(source);
        if (edgeName != null && edgeName.equals(EdgeNames.hasICD)) {
            tokenRelationship.setFromToken(fromToken);
            tokenRelationship.setToToken(toToken);
        } else {
            if (fromToken.getPosition() < toToken.getPosition()) {
                tokenRelationship.setFromToken(fromToken);
                tokenRelationship.setToToken(toToken);
            } else {
                tokenRelationship.setFromToken(toToken);
                tokenRelationship.setToToken(fromToken);
            }
        }
        return tokenRelationship;
    }

    public RecommendedTokenRelationship createRecommendedRelationship(String edgeName, String frameName, WordToken fromToken, WordToken toToken, String source) {
        RecommendedTokenRelationship recommendedTokenRelationship = new RecommendedTokenRelationship();
        recommendedTokenRelationship.setTokenRelationship(create(edgeName, null, frameName, fromToken, toToken, source));
        String key = recommendedTokenRelationship.getTokenRelationship().getFromTokenToTokenString();
        recommendedTokenRelationship.setKey(key);
        return recommendedTokenRelationship;
    }

    public RecommendedTokenRelationship createRecommendedRelationshipFromTokenRelationship(TokenRelationship tokenRelationship) {
        RecommendedTokenRelationship recommandedTokenRelationship = new RecommendedTokenRelationship();
        recommandedTokenRelationship.setTokenRelationship(tokenRelationship);
        String key = recommandedTokenRelationship.getTokenRelationship().getFromTokenToTokenString();
        recommandedTokenRelationship.setKey(key);
        return recommandedTokenRelationship;
    }

    public List<RecommendedTokenRelationship> createRecommendedRelationshipsFromTokenRelationships(List<TokenRelationship> tokenRelationships) {
        if (tokenRelationships == null)
            return null;
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            result.add(createRecommendedRelationshipFromTokenRelationship(tokenRelationship));
        }
        return result;
    }

    public RecommendedTokenRelationship deepCopy(RecommendedTokenRelationship original) {
        WordToken firstCloned = (WordToken) DeepCloner.deepClone(original.getTokenRelationship().getFromToken());
        WordToken secondCloned = (WordToken) DeepCloner.deepClone(original.getTokenRelationship().getToToken());
        return this.createRecommendedRelationship(original.getTokenRelationship().getEdgeName(), EdgeTypes.related, firstCloned, secondCloned, original.getTokenRelationship().getSource());
    }
}
