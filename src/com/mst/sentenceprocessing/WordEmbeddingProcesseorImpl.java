package com.mst.sentenceprocessing;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.jsonSerializers.DeepCloner;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

import java.util.ArrayList;
import java.util.List;

public class WordEmbeddingProcesseorImpl implements WordEmbeddingProcessor {

    private TokenRelationshipFactory factory;

    public WordEmbeddingProcesseorImpl() {
        factory = new TokenRelationshipFactoryImpl();
    }

    @Override
    public List<RecommendedTokenRelationship> process(List<WordToken> tokens) {
        List<RecommendedTokenRelationship> result = new ArrayList<>();
        if (tokens.size() == 1) {
            result.add(getWordEmbedding(tokens.get(0)));
        } else {
            for (int i = 0; i < tokens.size(); i++) {
                if (i + 1 < tokens.size())
                    result.add(getWordEmbedding(tokens.get(i), tokens.get(i + 1)));
            }
        }
        return result;
    }

    private RecommendedTokenRelationship getWordEmbedding(WordToken single) {
        WordToken singleCloned = (WordToken) DeepCloner.deepClone(single);
        singleCloned.setPosition(single.getPosition());
        return factory.createRecommendedRelationshipFromTokenRelationship(getTokenRelationship(single));
    }

    private RecommendedTokenRelationship getWordEmbedding(WordToken first, WordToken second) {
        WordToken firstCloned = (WordToken) DeepCloner.deepClone(first);
        firstCloned.setPosition(first.getPosition());
        WordToken secondCloned = (WordToken) DeepCloner.deepClone(second);
        secondCloned.setPosition(second.getPosition());
        return factory.createRecommendedRelationship(getEdgeName(first, second), EdgeTypes.related, firstCloned, secondCloned, this.getClass().getName());
    }

    private TokenRelationship getTokenRelationship(WordToken single) {
        TokenRelationship tokenRelationship = new TokenRelationship();
        tokenRelationship.setFromToken(single);
        tokenRelationship.setToToken(single);
        tokenRelationship.setEdgeName(WordEmbeddingTypes.tokenToken);
        return tokenRelationship;
    }

    private String getEdgeName(WordToken first, WordToken second) {

        String firstPos = "";
        if (first.getPos() != null)
            firstPos = first.getPos();

        String secondPos = "";
        if (second.getPos() != null)
            secondPos = second.getPos();

        if (first.isVerb() && secondPos.equals(PartOfSpeachTypes.IN))
            return WordEmbeddingTypes.verbPrep;
        if (first.isVerb() && second.isVerb())
            return WordEmbeddingTypes.bothVerbs;
        if (first.isVerb())
            return WordEmbeddingTypes.verbPlus;
        if (second.isVerb())
            return WordEmbeddingTypes.verbMinus;
        if (firstPos.equals(PartOfSpeachTypes.IN))
            return WordEmbeddingTypes.prepPlus;
        if (secondPos.equals(PartOfSpeachTypes.IN))
            return WordEmbeddingTypes.prepMinus;

        if (firstPos.equals(PartOfSpeachTypes.CC))
            return WordEmbeddingTypes.conjunctionPlus;
        if (secondPos.equals(PartOfSpeachTypes.CC))
            return WordEmbeddingTypes.conjunctionMinus;

        if (firstPos.equals(PartOfSpeachTypes.DP))
            return WordEmbeddingTypes.dependentSignalPlus;
        if (secondPos.equals(PartOfSpeachTypes.DP))
            return WordEmbeddingTypes.dependentSignalMinus;
        return WordEmbeddingTypes.tokenToken;
    }
}