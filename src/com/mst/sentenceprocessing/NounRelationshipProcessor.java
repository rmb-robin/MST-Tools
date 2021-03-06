package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;

import com.mst.interfaces.sentenceprocessing.RelationshipProcessor;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;


public class NounRelationshipProcessor extends RelationshipProcessorBase implements RelationshipProcessor {
    private HashSet<String> posTypes;

    public NounRelationshipProcessor() {
        posTypes = new HashSet<>();
        posTypes.add(PartOfSpeachTypes.CC);
        posTypes.add(PartOfSpeachTypes.NEG);
        posTypes.add(PartOfSpeachTypes.IN);
        posTypes.add(PartOfSpeachTypes.DET);
    }

    public List<TokenRelationship> process(List<WordToken> tokens, RelationshipInput input) {
        List<TokenRelationship> result = new ArrayList<>();
        this.wordTokens = tokens;
        setRelationshipMaps(input.getRelationshipMappings());
        for (WordToken wordToken : wordTokens) {
            List<TokenRelationship> singleTokenResult = processSingleToken(wordToken);
            if (!singleTokenResult.isEmpty()) {
                result.addAll(singleTokenResult);
            }
        }
        assignNounPhraseAnnotations(result);
        return result;
    }

    private void assignNounPhraseAnnotations(List<TokenRelationship> relationships) {
        if (relationships.size() == 0)
            return;
        List<Integer> highestIndexes = new ArrayList<>();
        highestIndexes.add(0);
        int lowestIndex = wordTokens.size();
        for (TokenRelationship tokenRelationship : relationships) {
            WordToken token = tokenRelationship.getToToken();
            int index = wordTokens.indexOf(token);
            if (index > highestIndexes.get(0))
                highestIndexes.set(0, index);
            token = tokenRelationship.getFromToken();
            index = wordTokens.indexOf(token);
            if (index < lowestIndex)
                lowestIndex = index;
        }
        int highestIndex = highestIndexes.get(0);
        for (int i = lowestIndex + 1; i < highestIndex; i++) {
            WordToken token = wordTokens.get(i);
            if (token.isVerb()) {
                highestIndexes.add(i - 1);
                continue;
            }
            if (token.getPos() != null && this.posTypes.contains(token.getPos())) {
                highestIndexes.add(i - 1);
            }
        }
        WordToken token = wordTokens.get(lowestIndex);
        token.setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
        for (int index : highestIndexes) {
            token = wordTokens.get(index);
            if (token.isVerb())
                continue;
            if (token.getPos() != null && this.posTypes.contains(token.getPos()))
                continue;
            token.setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
        }
    }

    private boolean shouldGetRelationshipsForFromToken(WordToken FromToken) {
        if (FromToken.getSemanticType() != null) {
            if (semanticTypeRelationshipMap.containsKey(FromToken.getSemanticType())) {
                return true;
            }
        }
        return relationshipMap.containsKey(FromToken.getToken().toLowerCase());
    }

    private List<TokenRelationship> processSingleToken(WordToken wordToken) {
        List<TokenRelationship> tokenRelationships = new ArrayList<>();
        int index = wordTokens.indexOf(wordToken);
        if (index > 0) {
            RelationshipMapping nounRelationship = findRelationWildcardFrom(wordToken);
            if (nounRelationship != null)
                tokenRelationships.add(createRelationshipAndAnnotateWordTokens(nounRelationship.getEdgeName(), wordTokens.get(index - 1), wordToken));
        }
        if (shouldGetRelationshipsForFromToken(wordToken))
            tokenRelationships.addAll(getRelationshipsForToken(wordToken));
        return tokenRelationships;
    }

    private List<TokenRelationship> getRelationshipsForToken(WordToken wordToken) {
        String key = wordToken.getToken().toLowerCase();
        Map<String, List<RelationshipMapping>> map = relationshipMap;
        List<TokenRelationship> result = new ArrayList<>(iterateMap(map, key, wordToken));
        if (wordToken.getSemanticType() != null) {
            map = semanticTypeRelationshipMap;
            key = wordToken.getSemanticType();
            result.addAll(iterateMap(map, key, wordToken));
        }
        return result;
    }

    private List<TokenRelationship> iterateMap(Map<String, List<RelationshipMapping>> map, String key, WordToken wordToken) {
        List<TokenRelationship> result = new ArrayList<>();
        if (!map.containsKey(key))
            return result;
        int startIndex = wordTokens.indexOf(wordToken);
        for (RelationshipMapping relationship : map.get(key)) {
            List<TokenRelationship> collection = processSingleNounRelationship(relationship, startIndex);
            result.addAll(collection);
        }
        return result;
    }

    private List<TokenRelationship> processSingleNounRelationship(RelationshipMapping relationshipMapping, int startIndex) {
        List<TokenRelationship> result = new ArrayList<>();
        if (relationshipMapping.getIsToWildcard()) {
            result.add(createRelationshipAndAnnotateWordTokens(relationshipMapping.getEdgeName(), wordTokens.get(startIndex), wordTokens.get(startIndex + 1)));
            return result;
        }
        int maxDistance = 7;
        int endIndex = getEndIndex(startIndex, maxDistance);
        for (int i = startIndex + 1; i <= endIndex; i++) {
            WordToken toToken = wordTokens.get(i);
            if (isWordTokenMatchToRelationship(relationshipMapping.getIsToSemanticType(), false, relationshipMapping.getToToken(), toToken))
                result.add(createRelationshipAndAnnotateWordTokens(relationshipMapping.getEdgeName(), wordTokens.get(startIndex), wordTokens.get(i)));
        }
        return result;
    }

    private TokenRelationship createRelationshipAndAnnotateWordTokens(String edgeName, WordToken fromToken, WordToken toToken) {
        return tokenRelationshipFactory.create(edgeName, null, EdgeTypes.related, fromToken, toToken, this.getClass().getName());
    }

    private RelationshipMapping findRelationWildcardFrom(WordToken wordToken) {
        List<RelationshipMapping> nounRelationships = relationshipMap.get(wildcard);
        if (nounRelationships == null)
            return null;
        for (RelationshipMapping relationship : nounRelationships) {
            if (relationship.getToToken().equals(wordToken.getToken()))
                return relationship;
        }
        return null;
    }
}
