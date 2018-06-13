package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.interfaces.sentenceprocessing.RecommendedNounPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class RecommendedNounPhraseProcessorImpl extends RelationshipProcessorBase implements RecommendedNounPhraseProcessor {
    private RelationshipInput input;
    private TokenRelationshipFactory factory;

    public RecommendedNounPhraseProcessorImpl() {
        factory = new TokenRelationshipFactoryImpl();
    }

    public List<RecommendedTokenRelationship> setNamedEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input) {
        setRelationshipMaps(input.getRelationshipMappings());
        this.input = input;
        List<RecommendedTokenRelationship> additionalEdges = new ArrayList<>();
        for (RecommendedTokenRelationship recommendedTokenRelationship : edges) {
            additionalEdges.addAll(processSingleEdge(recommendedTokenRelationship));
        }
        edges.addAll(additionalEdges);
        return edges;
    }

    public RecommandedNounPhraseResult process(List<RecommendedTokenRelationship> embeddedwords) {
        Map<Integer, RecommendedTokenRelationship> wordEmbeddingsByIndex = getFilteredWordEmbeddings(embeddedwords);
        int iterator = 0;
        int prevIndex = 0;
        int beginNounPhraseIndex = 0;
        int endNounPhraseIndex;
        Map<Integer, Integer> nounPhraseIndexes = new HashMap<>();
        for (Entry<Integer, RecommendedTokenRelationship> entry : wordEmbeddingsByIndex.entrySet()) {
            TokenRelationship tokenRelationship = entry.getValue().getTokenRelationship();
            if (iterator == 0 || entry.getKey() - prevIndex > 1) {
                tokenRelationship.getFromToken().setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
                beginNounPhraseIndex = entry.getKey();
            }
            if (!wordEmbeddingsByIndex.containsKey(entry.getKey() + 1)) {
                tokenRelationship.getToToken().setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
                endNounPhraseIndex = entry.getKey();
                if (!nounPhraseIndexes.containsKey(beginNounPhraseIndex))
                    nounPhraseIndexes.put(beginNounPhraseIndex, endNounPhraseIndex);
                //setVerifiedStatus(beginNounPhraseIndex, endNounPhraseIndex, embeddedwords);
            }
            if (iterator == wordEmbeddingsByIndex.size() - 1)
                break;
            prevIndex = entry.getKey();
            iterator += 1;
        }
        RecommandedNounPhraseResult nounPhraseResult = new RecommandedNounPhraseResult();
        nounPhraseResult.setRecommandedTokenRelationships(embeddedwords);
        nounPhraseResult.setNounPhraseIndexes(nounPhraseIndexes);
        return nounPhraseResult;
    }

    private RecommendedTokenRelationship create(RecommendedTokenRelationship from, RecommendedTokenRelationship to) {
        return factory.createRecommendedRelationship(WordEmbeddingTypes.tokenToken, EdgeTypes.related, from.getTokenRelationship().getFromToken(), to.getTokenRelationship().getToToken(), this.getClass().getName());
    }

    public List<RecommendedTokenRelationship> addEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input) {
        Map<String, RecommendedTokenRelationship> edgesByKey = RecommandedTokenRelationshipUtil.getByUniqueKey(edges);
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        this.input = input;
        for (int i = 0; i < edges.size(); i++) {
            RecommendedTokenRelationship recommandedTokenRelationship = edges.get(i);
            if (!recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.tokenToken))
                continue;
            RelationshipMapping mapping = findMapping(recommandedTokenRelationship);
            if (mapping != null)
                continue;
            result.addAll(findPermitation(i, recommandedTokenRelationship, edges, edgesByKey));
        }
        return result;
    }

    private List<RecommendedTokenRelationship> findPermitation(int fromIndex, RecommendedTokenRelationship recommandedTokenRelationship, List<RecommendedTokenRelationship> edges, Map<String, RecommendedTokenRelationship> edgesByKey) {
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        for (int i = fromIndex + 1; i < edges.size(); i++) {
            RecommendedTokenRelationship iterating = edges.get(i);
            if (!iterating.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.tokenToken))
                return result;
            RecommendedTokenRelationship newRelationship = create(recommandedTokenRelationship, iterating);
            RelationshipMapping mapping = findMapping(newRelationship);
            if (mapping == null)
                continue;
            if (edgesByKey.containsKey(newRelationship.getKey()))
                continue;
            newRelationship.getTokenRelationship().setNamedEdge(mapping.getEdgeName());
            result.add(newRelationship);
        }
        return result;
    }

    private Map<Integer, RecommendedTokenRelationship> getFilteredWordEmbeddings(List<RecommendedTokenRelationship> wordEmbeddings) {
        Map<Integer, RecommendedTokenRelationship> result = new HashMap<>();
        for (int i = 0; i < wordEmbeddings.size(); i++) {
            RecommendedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
            TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
            String type = tokenRelationship.getEdgeName();
            if (type.equals(WordEmbeddingTypes.tokenToken) || type.equals(WordEmbeddingTypes.prepMinus) || type.equals(WordEmbeddingTypes.verbMinus))
                result.put(i, recommandedTokenRelationship);
        }
        return result;
    }

    private RelationshipMapping findMapping(RecommendedTokenRelationship recommandedTokenRelationship) {
        for (RelationshipMapping mapping : input.getRelationshipMappings()) {
            if (!isWordTokenMatchToRelationship(mapping.getIsFromSemanticType(), false, mapping.getFromToken(), recommandedTokenRelationship.getTokenRelationship().getFromToken()))
                continue;
            if (isWordTokenMatchToRelationship(mapping.getIsToSemanticType(), false, mapping.getToToken(), recommandedTokenRelationship.getTokenRelationship().getToToken())) {
                return mapping;
            }
        }
        return null;
    }

    private List<RelationshipMapping> findMappings(RecommendedTokenRelationship recommandedTokenRelationship) {
        List<RelationshipMapping> mappings = new ArrayList<>();
        for (RelationshipMapping mapping : input.getRelationshipMappings()) {
            if (!isWordTokenMatchToRelationship(mapping.getIsFromSemanticType(), false, mapping.getFromToken(), recommandedTokenRelationship.getTokenRelationship().getFromToken()))
                continue;
            if (isWordTokenMatchToRelationship(mapping.getIsToSemanticType(), false, mapping.getToToken(), recommandedTokenRelationship.getTokenRelationship().getToToken())) {
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    private List<RecommendedTokenRelationship> processSingleEdge(RecommendedTokenRelationship recommandedTokenRelationship) {
        List<RelationshipMapping> mappings = findMappings(recommandedTokenRelationship);
        if (mappings.isEmpty())
            return new ArrayList<>();
        if (mappings.size() == 1) {
            RelationshipMapping mapping = mappings.get(0);
            recommandedTokenRelationship.getTokenRelationship().setNamedEdge(mapping.getNamedEdgeName());
            return new ArrayList<>();
        }
        List<RecommendedTokenRelationship> additionalEdges = addEdgesForMatchedNames(mappings.size() - 1, recommandedTokenRelationship);
        recommandedTokenRelationship.getTokenRelationship().setNamedEdge(mappings.get(0).getNamedEdgeName());
        for (int i = 1; i < mappings.size(); i++) {
            additionalEdges.get(i - 1).getTokenRelationship().setNamedEdge(mappings.get(i).getNamedEdgeName());
        }
        return additionalEdges;
    }

    private List<RecommendedTokenRelationship> addEdgesForMatchedNames(int numberOfAdd, RecommendedTokenRelationship originalRelationship) {
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        for (int i = 0; i < numberOfAdd; i++) {
            result.add(factory.deepCopy(originalRelationship));
        }
        return result;
    }
}
