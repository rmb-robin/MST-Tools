package com.mst.filter;

import java.util.Map;

import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;

import static com.mst.model.metadataTypes.EdgeNames.measurement;

class SentenceQueryResultFactory {
    static SentenceQueryResult createSentenceQueryResult(SentenceDb sentenceDb) {
        SentenceQueryResult result = new SentenceQueryResult();
        result.setSentence(sentenceDb.getNormalizedSentence());
        result.setSentenceId(sentenceDb.getId().toString());
        result.setDiscreteData(sentenceDb.getDiscreteData());
        return result;
    }

    static SentenceQueryEdgeResult createSentenceQueryEdgeResult(TokenRelationship relationship, String edgeType, Map<String, MatchInfo> matches, boolean displayEdge) {
        SentenceQueryEdgeResult queryEdgeResult = new SentenceQueryEdgeResult();
        queryEdgeResult.setEdgeName(relationship.getEdgeName());
        queryEdgeResult.setDescriptor(relationship.getDescriptor());
        queryEdgeResult.setFromToken(relationship.getFromToken().getToken());
        queryEdgeResult.setToToken(relationship.getToToken().getToken());
        if (relationship.getEdgeName().equals(measurement)) {
            queryEdgeResult.setTokenType("from");
            queryEdgeResult.setMatchedValue(relationship.getFromToken().getToken());
        } else if (matches.containsKey(relationship.getEdgeName())) {
            MatchInfo info = matches.get(relationship.getEdgeName());
            queryEdgeResult.setTokenType(info.getTokenType());
            queryEdgeResult.setMatchedValue(info.getValue());
        } else if (!displayEdge) {
            queryEdgeResult.setTokenType("from");
            queryEdgeResult.setMatchedValue(relationship.getFromToken().getToken());
        }
        queryEdgeResult.setEdgeResultType(edgeType);
        queryEdgeResult.setDisplayEdge(displayEdge);
        return queryEdgeResult;
    }
}
