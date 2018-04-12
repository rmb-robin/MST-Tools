package com.mst.filter;

import com.mst.interfaces.filter.QueryBusinessRuleFilter;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.DiscreteDataType;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.CreateSynonymQueryBusinessRuleType;
import com.mst.model.sentenceProcessing.SentenceDb;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CreateSynonymQueryBusinessRuleFilterImpl implements QueryBusinessRuleFilter {
    private static final Logger logger = LogManager.getLogger(CreateSynonymQueryBusinessRuleFilterImpl.class);

    @Override
    public List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, QueryBusinessRule rule) {
        return null;
    }

    @Override
    public CreateSynonymQueryBusinessRuleType filterByBusinessRule(SentenceQueryInput input, QueryBusinessRule businessRule) {
        logger.setLevel(Level.WARN);
        logger.warn("\n\n\n" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        logger.warn("Entering filterByBusinessRule with " + input.getSentenceQueryInstances().size() + " SentenceQueryInstances, and " + businessRule.getRules().size() + " business rules.");

        List<QueryBusinessRule.Rule> rulesApplied = new ArrayList<>();
        try {

            List<QueryBusinessRule.Rule> rules = businessRule.getRules();
            List<SentenceQueryInstance> queryInstances = input.getSentenceQueryInstances().stream().collect(Collectors.toList());

            for (QueryBusinessRule.Rule rule : rules) {
                logger.warn("Processing business rule - " + rule.getRuleName());
                List<DiscreteDataType> discreteDataToMatch = rule.getDiscreteDataToMatch();

                List<String> tokens = rule.getQueryTokens();
                Map<String, List<String>> edgesToMatch = rule.getEdgeValuesToMatch();
                String edgeName = rule.getEdgeName();
                List<String> values = rule.getEdgeValues();
                String synonymousEdge = rule.getSynonymousEdge();
                String synonymousValue = rule.getSynonymousValue();
                boolean edgeExists = rule.isEdgeNameExists();
                int index = 0;

/*
                ListIterator<SentenceQueryInstance> iter = queryInstances.listIterator();
                while (iter.hasNext()) {
                    SentenceQueryInstance queryInstance = iter.next();
*/

                    for (SentenceQueryInstance queryInstance : queryInstances) {
                    List<EdgeQuery> edgeQueries = queryInstance.getEdges();
                    boolean edgeNotFound = false;
                    if (queryInstance.getTokens() == null || queryInstance.getTokens().isEmpty())
                        continue;

                    for (String token : queryInstance.getTokens()) {
                        if (tokens.contains(token) && areEdgesToMatchesFound(edgeQueries, edgesToMatch)) {
                            if (edgeExists && isEdgeToMatchFound(edgeQueries, edgeName, values) && !isEdgeToMatchFound(edgeQueries, synonymousEdge, new ArrayList<>(Collections.singleton(synonymousValue)))) {
                                SentenceQueryInstance newQueryInstance = new SentenceQueryInstance();
                                newQueryInstance.setTokens(queryInstance.getTokens());
                                List<EdgeQuery> newEdgeQueries = new ArrayList<>(edgeQueries);
                                EdgeQuery synonymousEdgeQuery = new EdgeQuery();
                                synonymousEdgeQuery.setName(synonymousEdge);
                                synonymousEdgeQuery.setValues(new HashSet<>(Collections.singleton(synonymousValue)));
                                synonymousEdgeQuery.setIsNumeric(true);
                                synonymousEdgeQuery.setIncludeValues(false);
                                synonymousEdgeQuery.setIsNamedEdge(false);
                                newQueryInstance.setEdges(replaceEdgeWithSynonym(newEdgeQueries, edgeName, synonymousEdgeQuery));
                                newQueryInstance.setAppender("or");
                                input.getSentenceQueryInstances().add(index + 1, newQueryInstance);
                                rulesApplied.add(rule);
                                logger.warn("New query instance added for " + synonymousEdgeQuery.getName());
                                break;
                            } else if (!edgeExists)
                                edgeNotFound = isEdgeToMatchNotFound(edgeQueries, edgeName);
                        }
                    }

                    index++;
                    if (edgeNotFound) {
                        rulesApplied.add(rule);
                        logger.warn("Rule applied: " + rule.getRuleName());
                    }
                }
            }
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            logger.warn("EXCEPTION - " + writer.toString());
        }


        logger.warn("filterByBusinessRule returning: " + input.getSentenceQueryInstances().size() + " SentenceQueryInstances. " + rulesApplied.size() + " rules were applied.");
        return new CreateSynonymQueryBusinessRuleType(input, rulesApplied);
    }

    @Override
    public List<SentenceQueryResult> modifyByBusinessRule(List<SentenceQueryResult> sentenceQueryResults, List<QueryBusinessRule.Rule> rulesApplied) {
        logger.warn("Entering modifyByBusinessRule with " + sentenceQueryResults.size() + " SentenceQueryResults, and " + rulesApplied.size() + " applied business rules.");
        int count = 0;
        try {

            for (SentenceQueryResult sentenceQueryResult : sentenceQueryResults) {
                DiscreteData resultDiscreteData = sentenceQueryResult.getDiscreteData();
                List<SentenceQueryEdgeResult> edgeResults = sentenceQueryResult.getSentenceQueryEdgeResults();

                for (QueryBusinessRule.Rule rule : rulesApplied) {
                    List<DiscreteDataType> discreteDataToMatch = rule.getDiscreteDataToMatch();
                    boolean ruleHasDiscreteData = (discreteDataToMatch != null && !discreteDataToMatch.isEmpty());
                    //TODO the only DiscreteData in the rules is patientAge, this can be make generic if that changes
                    int minRangeValue = 999;
                    int maxRangeValue = -999;

                    if (ruleHasDiscreteData && resultDiscreteData != null) {
                        logger.warn("Matching discrete data");
                        for (DiscreteDataType discreteData : discreteDataToMatch) {
                            minRangeValue = Integer.parseInt(discreteData.getMinRangeValue());
                            maxRangeValue = Integer.parseInt(discreteData.getMaxRangeValue());
                        }
                        int patientAge = resultDiscreteData.getPatientAge();
                        if (patientAge < minRangeValue || patientAge > maxRangeValue)
                            continue;
                    }

                    boolean edgeNameExists = rule.isEdgeNameExists();
                    String edgeName = rule.getEdgeName();
                    if ((edgeNameExists && isEdgeToMatchFoundInResults(edgeResults, edgeName, rule.getEdgeValues()) || (!edgeNameExists && isEdgeToMatchNotFoundInResults(edgeResults, edgeName)))) {
                        SentenceQueryEdgeResult newEdgeResult = new SentenceQueryEdgeResult();
                        newEdgeResult.setEdgeName(rule.getSynonymousEdge());
                        newEdgeResult.setMatchedValue(rule.getSynonymousValue());
                        edgeResults.add(newEdgeResult);
                        logger.warn("Added " + newEdgeResult.getEdgeName() + " edge with value " + newEdgeResult.getMatchedValue());
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            logger.warn("EXCEPTION - " + writer.toString());
        }

        logger.warn("modifyByBusinessRule returning: " + sentenceQueryResults.size() + " SentenceQueryResults with " + count + " added edges");
        return sentenceQueryResults;
    }

    private List<EdgeQuery> replaceEdgeWithSynonym(List<EdgeQuery> edgeQueries, String edgeName, EdgeQuery synonym) {
        int index = 0;
        for (EdgeQuery edgeQuery : edgeQueries) {
            if (edgeQuery != null && edgeQuery.getName() != null && edgeQuery.getName().equals(edgeName))
                break;
            else
                index++;
        }
        edgeQueries.set(index, synonym);
        return edgeQueries;
    }

    private boolean isEdgeToMatchNotFound(List<EdgeQuery> edgeQuery, String edgeToMatch) {
        if (edgeQuery != null && !edgeQuery.isEmpty())
            for (EdgeQuery edge : edgeQuery) {
                if (edge != null && edge.getName() != null && edge.getName().equals(edgeToMatch))
                    return false;
            }
        return true;
    }

    private boolean isEdgeToMatchFound(List<EdgeQuery> edgeQuery, String edgeToMatch, List<String> valuesToMatch) {
        if (edgeQuery != null && !edgeQuery.isEmpty())
            for (EdgeQuery edge : edgeQuery) {
                if (edge != null && edge.getName() != null && edge.getName().equals(edgeToMatch)) {
                    if (valuesToMatch == null || valuesToMatch.isEmpty())
                        return true;
                    for (String value : valuesToMatch) {
                        if (edge.getValues() != null && edge.getValues().contains(value))
                            return true;
                    }
                }
            }
        return false;
    }

    private boolean areEdgesToMatchesFound(List<EdgeQuery> edgeQuery, Map<String, List<String>> edgesToMatch) {
        if (edgeQuery == null || edgeQuery.isEmpty())
            return false;

        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            if (!isEdgeToMatchFound(edgeQuery, entry.getKey(), entry.getValue()))
                return false;
        }
        return true;
    }

    private boolean isEdgeToMatchNotFoundInResults(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch) {
        String log = "Edge to not match " + edgeToMatch;

        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult result : edgeResults) {
                if (result != null && result.getEdgeName() != null && result.getEdgeName().equals(edgeToMatch)) {
                    logger.warn(log + " was found in results");
                    return false;
                }
            }

        logger.warn(log + " was not found in results");
        return true;
    }

    private boolean isEdgeToMatchFoundInResults(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch, List<String> valuesToMatch) {
        String log = "Edge to match " + edgeToMatch;
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult result : edgeResults) {
                if (result != null && result.getEdgeName() != null && result.getEdgeName().equals(edgeToMatch)) {
                    if (valuesToMatch != null && !valuesToMatch.isEmpty())
                        log += " with value " + valuesToMatch.get(0);

                    if (valuesToMatch == null || valuesToMatch.isEmpty() || valuesToMatch.contains(result.getMatchedValue())) {
                        logger.warn(log += " was found in results");
                        return true;
                    }
                }
            }

        logger.warn(log += " was not found in results");
        return false;
    }

    private boolean areEdgesToMatchesFoundInResults(List<SentenceQueryEdgeResult> edgeResults, Map<String, List<String>> edgesToMatch) {
        if (edgeResults == null || edgeResults.isEmpty())
            return false;

        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            if (!isEdgeToMatchFoundInResults(edgeResults, entry.getKey(), entry.getValue()))
                return false;
        }
        return true;
    }

    //TODO if the ages in the sentences do not all fall within the age range specified in the rules, this will fail
    private boolean doesDiscreteDataMatch(List<SentenceDb> sentences, List<DiscreteDataType> discreteDataToMatch) {
        if (discreteDataToMatch == null || discreteDataToMatch.isEmpty())
            return true;

        int minRangeValue = 999;
        int maxRangeValue = -999;
        for (DiscreteDataType discreteData : discreteDataToMatch) {
            //String name = discreteData.getName();
            DiscreteDataType.DataType dataType = discreteData.getDataType();

            if (dataType.equals(DiscreteDataType.DataType.NUMERIC_RANGE)) {
                minRangeValue = Integer.parseInt(discreteData.getMinRangeValue());
                maxRangeValue = Integer.parseInt(discreteData.getMaxRangeValue());
            }
        }

        //TODO DiscreteData in the SentenceDb needs to be modified for generic approach e.g., sentenceDb.getDiscreteData().getValue(name)
        for (SentenceDb sentence : sentences) {
            DiscreteData discreteData = sentence.getDiscreteData();
            int patientAge = discreteData.getPatientAge();
            if (patientAge >= minRangeValue && patientAge <= maxRangeValue)
                return true;
        }
        return false;
    }
}
