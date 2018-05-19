package com.mst.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.interfaces.filter.SentenceFilterController;
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;


public class SentenceFilterControllerImpl implements SentenceFilterController {
    private HashSet<String> processedSentences;
    private SentenceFilter sentenceFilter;
    private Map<String, SentenceQueryResult> queryResults;
    private Map<String, SentenceDb> cumulativeSentenceResults;
    private FriendOfFriendService friendOfFriendService;
    private Map<String, MatchInfo> matches;

    public SentenceFilterControllerImpl() {
        processedSentences = new HashSet<>();
        queryResults = new HashMap<>();
        cumulativeSentenceResults = new HashMap<>();
        sentenceFilter = new SentenceFilterImpl();
        friendOfFriendService = new FriendOfFriendServiceImpl();
    }

    public Map<String, SentenceQueryResult> getQueryResults() {
        return queryResults;
    }

    public List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, List<EdgeQuery> edgeQuery, String searchToken) {
        List<SentenceQueryResult> result = new ArrayList<>();
        for (SentenceDb sentenceDb : sentences) {
            try {
                String id = sentenceDb.getId().toString();
                if (processedSentences.contains(id) || shouldByPassResult(sentenceDb.getTokenRelationships(), edgeQuery, searchToken))
                    continue;
                String oppositeToken;
                TokenRelationship foundRelationship;
                SentenceQueryResult queryResult = null;
                HashSet<String> edgeNameHash = new HashSet<>();
                edgeQuery.forEach(a -> edgeNameHash.add(a.getName()));
                for (TokenRelationship relationship : sentenceDb.getTokenRelationships()) {
                    if (relationship.getEdgeName() == null)
                        continue;
                    boolean isEdgeInSearchQuery = edgeNameHash.contains(relationship.getEdgeName());
                    ShouldMatchOnSentenceEdgesResult edgesResult = sentenceFilter.shouldAddTokenFromRelationship(relationship, token);
                    if (edgesResult.isMatch()) {
                        queryResult = (queryResult == null) ? SentenceQueryResultFactory.createSentenceQueryResult(sentenceDb) : queryResult;
                        if (isEdgeInSearchQuery)
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship, EdgeResultTypes.primaryEdge, matches, true));
                        else if (!relationship.getEdgeName().isEmpty())
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship, EdgeResultTypes.primaryEdge, matches, false));
                        oppositeToken = relationship.getOppositeToken(token);
                        foundRelationship = relationship;
                        ShouldMatchOnSentenceEdgesResult friendResult = friendOfFriendService.findFriendOfFriendEdges(sentenceDb.getTokenRelationships(), oppositeToken, foundRelationship, edgeNameHash);
                        if (friendResult != null) {
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(friendResult.getRelationship(), EdgeResultTypes.friendOfFriend, matches, true));
                        }
                    }
                }
                if (queryResult != null) {
                    result.add(queryResult);
                    processedSentences.add(id);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void filterForAnd(SentenceQueryInstance sentenceQueryInstance) {
        Map<String, EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
        HashSet<String> matchedIds = new HashSet<>();
        for (Map.Entry<String, SentenceDb> entry : cumulativeSentenceResults.entrySet()) {
            boolean tokenMatch = false;
            String matchedToken = "";
            for (String token : sentenceQueryInstance.getTokens()) {
                if (entry.getValue().getOrigSentence().contains(token)) {
                    tokenMatch = true;
                    matchedToken = token;
                    break;
                }
            }
            if (!tokenMatch)
                continue;
            HashSet<String> sentenceUniqueEdgeNames = new HashSet<>();
            entry.getValue().getTokenRelationships().forEach(a -> sentenceUniqueEdgeNames.add(a.getEdgeName()));
            for (String edgeName : edgeQueriesByName.keySet()) {
                if (!sentenceUniqueEdgeNames.contains(edgeName)) {
                    tokenMatch = false;
                    break;
                }
            }
            if (!tokenMatch)
                continue;
            if (shouldByPassResult(entry.getValue().getTokenRelationships(), sentenceQueryInstance.getEdges(), matchedToken))
                continue;
            matchedIds.add(entry.getKey());
        }
        updateExistingResults(matchedIds);
    }

    public void filterForAndNot(SentenceQueryInstance sentenceQueryInstance) {
        HashSet<String> matchedIds = new HashSet<>();
        for (Map.Entry<String, SentenceDb> entry : cumulativeSentenceResults.entrySet()) {
            for (String token : sentenceQueryInstance.getTokens()) {  // do all tokens as this is not a db query impact
                if (!entry.getValue().getOrigSentence().contains(token)) {
                    continue;
                }
                EdgeMatchOnQueryResult result = sentenceFilter.AreEdgesMatchOnQuery(entry.getValue().getTokenRelationships(), sentenceQueryInstance.getEdges(), token);
                if (result.isMatch()) {
                    matchedIds.add(entry.getKey());
                    break;
                }
            }
        }
        for (String id : matchedIds) {  // changing since update existing does the opposite of what we want here
            queryResults.remove(id);
            this.cumulativeSentenceResults.remove(id);
        }
    }

    public void filterForAndNotAll(SentenceQueryInstance sentenceQueryInstance) {
        Map<String, EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
        HashSet<String> matchedIds = new HashSet<>();
        for (Map.Entry<String, SentenceDb> entry : cumulativeSentenceResults.entrySet()) {
            int tokenMatchCount = 0;
            HashSet<String> sentenceUniqueEdgeNames = new HashSet<>();
            entry.getValue().getTokenRelationships().forEach(a -> sentenceUniqueEdgeNames.add(a.getEdgeName()));
            for (String edgeName : edgeQueriesByName.keySet()) {
                if (sentenceUniqueEdgeNames.contains(edgeName)) {
                    tokenMatchCount += 1;
                }
            }
            if (tokenMatchCount == edgeQueriesByName.size())
                continue;
            matchedIds.add(entry.getKey());
        }
        updateExistingResults(matchedIds);
    }

    public void addSentencesToResult(SentenceQueryInstanceResult result) {
        Map<String, SentenceDb> sentencesById = new HashMap<>();
        for (SentenceDb s : result.getSentences()) {
            if (sentencesById.containsKey(s.getId().toString()))
                continue;
            sentencesById.put(s.getId().toString(), s);
        }
        for (SentenceQueryResult queryResult : result.getSentenceQueryResult()) {
            if (this.queryResults.containsKey(queryResult.getSentenceId()))
                continue;
            this.queryResults.put(queryResult.getSentenceId(), queryResult);
            SentenceDb matchedSentence = sentencesById.get(queryResult.getSentenceId());
            if (matchedSentence != null && !this.cumulativeSentenceResults.containsKey(queryResult.getSentenceId())) {
                this.cumulativeSentenceResults.put(queryResult.getSentenceId(), matchedSentence);
            }
        }
    }

    public Map<String, EdgeQuery> convertEdgeQueryToDictionary(SentenceQueryInstance input) {
        Map<String, EdgeQuery> result = new HashMap<>();
        for (EdgeQuery q : input.getEdges()) {
            if (result.containsKey(q.getName()))
                continue;
            result.put(q.getName(), q);
        }
        return result;
    }

    public Map<String, SentenceDb> getCumulativeSentenceResults() {
        return cumulativeSentenceResults;
    }

    private void updateExistingResults(HashSet<String> matchedIds) {
        List<String> idsToRemove = new ArrayList<>();
        for (String id : this.queryResults.keySet()) {
            if (matchedIds.contains(id))
                continue;
            idsToRemove.add(id);
        }
        for (String id : idsToRemove) {
            queryResults.remove(id);
            this.cumulativeSentenceResults.remove(id);
        }
    }

    private boolean shouldByPassResult(List<TokenRelationship> existingtokenRelationships, List<EdgeQuery> edgeQueries, String searchToken) {
        EdgeMatchOnQueryResult edgeMatchOnQueryResult = sentenceFilter.AreEdgesMatchOnQuery(existingtokenRelationships, edgeQueries, searchToken);
        matches = edgeMatchOnQueryResult.getMatches();
        return !edgeMatchOnQueryResult.isMatch();
    }
}
