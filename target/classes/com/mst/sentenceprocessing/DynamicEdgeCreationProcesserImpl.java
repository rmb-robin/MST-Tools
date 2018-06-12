package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.DynamicEdgeCondition;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.RecommandedTokenRelationshipUtil;
import com.mst.util.TokenRelationshipUtil;

public class DynamicEdgeCreationProcesserImpl implements DynamicEdgeCreationProcesser {
    private TokenRelationshipFactory tokenRelationshipFactory;

    public DynamicEdgeCreationProcesserImpl() {
        tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
    }

    public List<TokenRelationship> process(List<DynamicEdgeCreationRule> rules, Map<String, List<TokenRelationship>> map, List<WordToken> modifiedWords) {
        // TODO Auto-generated method stub
        List<TokenRelationship> results = new ArrayList<>();

        for (DynamicEdgeCreationRule rule : rules) {
            if (isRuleValid(rule, map, modifiedWords)) {
                TokenRelationship relationship = create(rule, map, modifiedWords);
                if (relationship != null) {
                    results.add(relationship);
                }
            }
        }
        return results;
    }

    public List<RecommendedTokenRelationship> processDiscovery(List<DynamicEdgeCreationRule> rules, SentenceDiscovery sentence) {
        // TODO Auto-generated method stub
        List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(sentence.getWordEmbeddings());
        Map<String, List<TokenRelationship>> map = TokenRelationshipUtil.getMapByEdgeName(relationships, true);
        List<WordToken> modifiedWords = sentence.getModifiedWordList();
        List<TokenRelationship> relationshipsToAdd = this.process(rules, map, modifiedWords);
        List<RecommendedTokenRelationship> result = new ArrayList<>();

        for (TokenRelationship r : relationshipsToAdd) {
            RecommendedTokenRelationship recommended = this.tokenRelationshipFactory.createRecommendedRelationshipFromTokenRelationship(r);
            recommended.getTokenRelationship().setNamedEdge(recommended.getTokenRelationship().getEdgeName());
            result.add(recommended);
        }
        return result;
    }

    private boolean isRuleValid(DynamicEdgeCreationRule rule, Map<String, List<TokenRelationship>> map, List<WordToken> modifiedWords) {
        for (DynamicEdgeCondition condition : rule.getConditions()) {
            if (!isConditionValid(condition, map, modifiedWords))
                return false;
        }
        return true;
    }

    private boolean isConditionValid(DynamicEdgeCondition condition, Map<String, List<TokenRelationship>> map, List<WordToken> modifiedWords) {
        boolean isValid;
        if (condition.isCondition1Token())
            isValid = isTokenConditionValid(condition, modifiedWords);
        else
            isValid = isTokenRelationshipValid(condition, map);
        if (condition.getIsEqualTo())
            return isValid;
        return !isValid;
    }

    private boolean isTokenConditionValid(DynamicEdgeCondition condition, List<WordToken> modifiedWordTokens) {
        for (WordToken wordToken : modifiedWordTokens) {
            if (isTokenAMatch(condition.getIsTokenSemanticType(), condition.getIsTokenPOSType(), wordToken, condition.getToken()))
                return true;
        }
        return false;
    }

    private boolean isTokenAMatch(boolean isSementicType, boolean isPos, WordToken token, String value) {
        if (isSementicType) {
            if (token.getSemanticType() != null && token.getSemanticType().equals(value))
                return true;
        }
        if (isPos) {
            if (token.getPos() != null && token.getPos().equals(value))
                return true;
        }
        return token.getToken().equals(value);
    }

    private boolean isTokenRelationshipValid(DynamicEdgeCondition condition, Map<String, List<TokenRelationship>> map) {
        if (condition.getEdgeNames().isEmpty())
            return true;
        for (String edgeName : condition.getEdgeNames()) {
            if (map.containsKey(edgeName)) {
                if (isTokenRelationshipmatch(map.get(edgeName), condition))
                    return true;
            }
        }
        return false;
    }

    private boolean areTokensMatch(boolean isSementicType, boolean isPos, WordToken token, List<String> values) {
        for (String value : values) {
            if (isTokenAMatch(isSementicType, isPos, token, value))
                return true;
        }
        return false;
    }

    private boolean isTokenRelationshipmatch(List<TokenRelationship> tokenRelationships, DynamicEdgeCondition condition) {
        if (condition.getToTokens().isEmpty() && condition.getFromTokens().isEmpty())
            return true;

        for (TokenRelationship tokenRelationship : tokenRelationships) {
            if (!condition.getFromTokens().isEmpty()) {
                if (areTokensMatch(condition.getIsFromTokenSemanticType(), condition.getIsFromTokenPOSType(), tokenRelationship.getFromToken(), condition.getFromTokens()))
                    return true;
            }
            if (!condition.getToTokens().isEmpty()) {
                if (areTokensMatch(condition.getIsToTokenSemanticType(), condition.getIsToTokenPOSType(), tokenRelationship.getToToken(), condition.getToTokens()))
                    return true;
            }
        }
        return false;
    }

    private TokenRelationship create(DynamicEdgeCreationRule rule, Map<String, List<TokenRelationship>> map, List<WordToken> modifiedWords) {
        WordToken from = null;
        WordToken to = null;
        if (!rule.getFromEdgeNames().isEmpty()) {
            from = getFromTokenFromEdgesNames(rule.getFromEdgeNames(), map);
            if (from == null)
                return null;
        }

        for (WordToken token : modifiedWords) {
            if (from == null && isTokenAMatch(rule.isFromTokenSementicType(), false, token, rule.getFromToken())) {
                from = token;
                continue;
            }
            if (to == null && isTokenAMatch(rule.isToTokenSementicType(), false, token, rule.getToToken()))
                to = token;
        }

        if (from == null)
            return null;
        if (to == null)
            return null;
        return tokenRelationshipFactory.create(rule.getEdgeName(), null, EdgeTypes.related, from, to, this.getClass().getName());
    }

    private WordToken getFromTokenFromEdgesNames(List<String> edgeNames, Map<String, List<TokenRelationship>> map) {
        for (String edgeName : edgeNames) {
            if (map.containsKey(edgeName)) {
                return map.get(edgeName).get(0).getFromToken();
            }
        }
        return null;
    }
}
