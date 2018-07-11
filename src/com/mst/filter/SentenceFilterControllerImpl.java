package com.mst.filter;

import java.util.*;

import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.interfaces.filter.SentenceFilterController;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.Compliance;
import com.mst.model.discrete.ComplianceResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.FollowupRecommendation;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.sentenceprocessing.ComplianceProcessor;
import com.mst.sentenceprocessing.DiscreteDataMenopausalStatus;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static com.mst.model.metadataTypes.EdgeNames.measurement;


public class SentenceFilterControllerImpl implements SentenceFilterController {
    private static final Logger LOGGER = LogManager.getLogger(SentenceFilterControllerImpl.class);
    private HashSet<String> processedSentences;
    private SentenceFilter sentenceFilter;
    private Map<String, SentenceQueryResult> queryResults;
    private Map<String, SentenceDb> cumulativeSentenceResults;
    private FriendOfFriendService friendOfFriendService;
    private Map<String, MatchInfo> matches;
    private ComplianceProcessor complianceProcessor;
    private Map<DiscreteData, ComplianceResult> complianceResults;

    public SentenceFilterControllerImpl() {
        processedSentences = new HashSet<>();
        queryResults = new HashMap<>();
        cumulativeSentenceResults = new HashMap<>();
        sentenceFilter = new SentenceFilterImpl();
        friendOfFriendService = new FriendOfFriendServiceImpl();
        complianceProcessor = new ComplianceProcessor();
    }

    public Map<String, SentenceQueryResult> getQueryResults() {
        return queryResults;
    }

    public List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, List<EdgeQuery> edgeQuery, String measurementClassification, List<BusinessRule> businessRules) {
        List<SentenceQueryResult> results = new ArrayList<>();
        for (SentenceDb sentenceDb : sentences) {
            try {
                String id = sentenceDb.getId().toString();
                if (processedSentences.contains(id) || shouldByPassResult(sentenceDb.getTokenRelationships(), edgeQuery, token, measurementClassification, businessRules))
                    continue;
                String oppositeToken;
                TokenRelationship foundRelationship;
                SentenceQueryResult queryResult = null;
                HashSet<String> edgeNameHash = new HashSet<>();
                edgeQuery.forEach(a -> edgeNameHash.add(a.getName()));
                for (TokenRelationship relationship : sentenceDb.getTokenRelationships()) {
                    if (relationship.getEdgeName() == null)
                        continue;
                    ShouldMatchOnSentenceEdgesResult edgesResult = sentenceFilter.shouldAddTokenFromRelationship(relationship, token);
                    if (edgesResult.isMatch() || relationship.getEdgeName().equals(measurement)) {
                        queryResult = (queryResult == null) ? SentenceQueryResultFactory.createSentenceQueryResult(sentenceDb) : queryResult;
                        boolean isEdgeInSearchQuery = edgeNameHash.contains(relationship.getEdgeName());
                        if (isEdgeInSearchQuery)
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship, EdgeResultTypes.primaryEdge, matches, true));
                        else if (!relationship.getEdgeName().isEmpty())
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship, EdgeResultTypes.primaryEdge, matches, false));
                        oppositeToken = relationship.getOppositeToken(token);
                        foundRelationship = relationship;
                        ShouldMatchOnSentenceEdgesResult friendResult = friendOfFriendService.findFriendOfFriendEdges(sentenceDb.getTokenRelationships(), oppositeToken, foundRelationship, edgeNameHash);
                        if (friendResult != null)
                            queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(friendResult.getRelationship(), EdgeResultTypes.friendOfFriend, matches, true));
                    }
                }
                if (queryResult != null) {
                    results.add(queryResult);
                    processedSentences.add(id);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
            }
        }
        return results;
    }

    public void filterForAnd(SentenceQueryInstance sentenceQueryInstance, List<BusinessRule> businessRules) {
        Map<String, EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
        HashSet<String> matchedIds = new HashSet<>();
        String measurementClassification = sentenceQueryInstance.getMeasurementClassification();
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
            if (shouldByPassResult(entry.getValue().getTokenRelationships(), sentenceQueryInstance.getEdges(), matchedToken, measurementClassification, businessRules))
                continue;
            matchedIds.add(entry.getKey());
        }
        updateExistingResults(matchedIds);
    }

    public void filterForAndNot(SentenceQueryInstance sentenceQueryInstance, List<BusinessRule> businessRules) {
        HashSet<String> matchedIds = new HashSet<>();
        for (Map.Entry<String, SentenceDb> entry : cumulativeSentenceResults.entrySet()) {
            for (String token : sentenceQueryInstance.getTokens()) {  // do all tokens as this is not a db query impact
                if (!entry.getValue().getOrigSentence().contains(token)) {
                    continue;
                }
                EdgeMatchOnQueryResult result = sentenceFilter.matchEdgesOnQuery(entry.getValue().getTokenRelationships(), sentenceQueryInstance.getEdges(), token, sentenceQueryInstance.getMeasurementClassification(), businessRules);
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
            if (queryResults.containsKey(queryResult.getSentenceId()))
                continue;
            queryResults.put(queryResult.getSentenceId(), queryResult);
            SentenceDb matchedSentence = sentencesById.get(queryResult.getSentenceId());
            if (matchedSentence != null && !cumulativeSentenceResults.containsKey(queryResult.getSentenceId())) {
                cumulativeSentenceResults.put(queryResult.getSentenceId(), matchedSentence);
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

    public void processCompliance(List<SentenceDb> sentences, List<BusinessRule> businessRules, boolean setFollowupRecommendation) {
        if (businessRules == null) {
            LOGGER.debug("businessRules is null");
            return;
        }
        Compliance compliance = null;
        for (BusinessRule businessRule : businessRules) {
            if (businessRule instanceof Compliance) {
                compliance = (Compliance) businessRule;
                break;
            }
        }
        if (compliance == null) {
            LOGGER.debug("instanceof Compliance not found in businessRules");
            return;
        }
        Map<DiscreteData, List<SentenceDb>> sentencesByDiscreteData = new HashMap<>();
        for (SentenceDb sentence : sentences) {
            DiscreteData discreteData = sentence.getDiscreteData();
            if (sentencesByDiscreteData.containsKey(discreteData)) {
                sentencesByDiscreteData.get(discreteData).add(sentence);
            } else {
                DiscreteDataMenopausalStatus.setStatus(sentence.getDiscreteData());
                sentencesByDiscreteData.put(discreteData, new ArrayList<>(Collections.singletonList(sentence)));
            }
        }
        complianceResults = new HashMap<>();
        for (Map.Entry<DiscreteData, List<SentenceDb>> entry : sentencesByDiscreteData.entrySet()) {
            DiscreteData discreteData = entry.getKey();
            ComplianceResult complianceResult = complianceProcessor.process(discreteData, entry.getValue(), compliance, setFollowupRecommendation);
            complianceResults.put(discreteData, complianceResult);
            Map<String, Boolean> bucketCompliance = complianceResult.getBucketCompliance();
            StringBuilder bucketName = new StringBuilder();
            String isCompliant = "";
            Iterator<Map.Entry<String, Boolean>> itr = bucketCompliance.entrySet().iterator();
            while(itr.hasNext()) {
                Map.Entry<String, Boolean> mapEntry = itr.next();
                bucketName.append(mapEntry.getKey());
                isCompliant += String.valueOf(mapEntry.getValue());
                if (itr.hasNext()) {
                    bucketName.append(", ");
                    isCompliant += ", ";
                }
            }
            if (discreteData != null) {
                discreteData.setBucketName(bucketName.toString());
                discreteData.setIsCompliant(isCompliant);
            }
            FollowupRecommendation followup = complianceResult.getFollowupRecommendation();
            if (discreteData != null && followup != null)
                discreteData.setFollowupRecommendation(followup);
        }
    }

    public Map<DiscreteData, ComplianceResult> getComplianceResults() {
        return complianceResults;
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

    private boolean shouldByPassResult(List<TokenRelationship> existingTokenRelationships, List<EdgeQuery> edgeQueries, String token, String measurementClassification, List<BusinessRule> businessRules) {
        EdgeMatchOnQueryResult edgeMatchOnQueryResult = sentenceFilter.matchEdgesOnQuery(existingTokenRelationships, edgeQueries, token, measurementClassification, businessRules);
        matches = edgeMatchOnQueryResult.getMatches();
        return !edgeMatchOnQueryResult.isMatch();
    }
}
