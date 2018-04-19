package com.mst.filter;

import com.mst.interfaces.filter.QueryBusinessRuleFilter;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.CreateSynonymQueryBusinessRuleType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mst.model.businessRule.QueryBusinessRule.Rule.Edge.LogicalOperator.*;

public class CreateSynonymQueryBusinessRuleFilterImpl implements QueryBusinessRuleFilter {
    @Override
    public List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, QueryBusinessRule rule) {
        return null;
    }

    @Override
    public CreateSynonymQueryBusinessRuleType filterByBusinessRule(SentenceQueryInput input, QueryBusinessRule businessRule) {
        List<QueryBusinessRule.Rule> rulesApplied = new ArrayList<>();
        try {
            List<QueryBusinessRule.Rule> rules = businessRule.getRules();
            List<SentenceQueryInstance> queryInstances = new ArrayList<>(input.getSentenceQueryInstances());

            for (QueryBusinessRule.Rule rule : rules) {
                if (!rulesApplied.isEmpty())
                    break;
                boolean addEdgesToQuery = rule.isAddEdgesToQuery();
                List<String> tokens = rule.getQueryTokens();
                Map<String, List<String>> edgesToMatch = rule.getEdgeValuesToMatch();
                boolean containsNonexistentEdge = false;
                boolean nonexistentEdgeNotFoundAndSynonymousEdgeNotFound = true;
                boolean tokensAndEdgesToMatchFound = false;

                for (SentenceQueryInstance queryInstance : queryInstances) {
                    if (!rulesApplied.isEmpty())
                        break;
                    List<EdgeQuery> edgeQueries = queryInstance.getEdges();
                    if (queryInstance.getTokens() == null || queryInstance.getTokens().isEmpty())
                        continue;

                    for (String token : queryInstance.getTokens()) {
                        if (!rulesApplied.isEmpty())
                            break;
                        if (tokens.contains(token) && areEdgesToMatchFound(edgeQueries, edgesToMatch)) {
                            tokensAndEdgesToMatchFound = true;
                            List<QueryBusinessRule.Rule.Edge> edges = rule.getEdges();

                            for (QueryBusinessRule.Rule.Edge edge : edges) {
                                boolean edgeExists = edge.isEdgeNameExists();
                                if (!containsNonexistentEdge && !edgeExists)
                                    containsNonexistentEdge = true;
                                boolean edgeFound = isEdgeMatchFound(edgeQueries, edge.getEdgeName(), edge.getEdgeValue());
                                boolean synonymousEdgeNotFound = isSynonymousEdgeNotFound(edgeQueries, rule.getSynonymousEdge(), rule.getSynonymousEdgeValues());
                                if (edgeExists && edgeFound && synonymousEdgeNotFound) {
                                    rulesApplied.add(rule);
                                    break;
                                }
                                else if ((!edgeExists && edgeFound) || !synonymousEdgeNotFound)
                                    nonexistentEdgeNotFoundAndSynonymousEdgeNotFound = false;
                            }
                        }
                    }
                }
                if (tokensAndEdgesToMatchFound && containsNonexistentEdge && nonexistentEdgeNotFoundAndSynonymousEdgeNotFound && addEdgesToQuery) {
                    List<QueryBusinessRule.Rule.Edge> edges = rule.getEdges();
                    for (QueryBusinessRule.Rule.Edge edge : edges) {
                        SentenceQueryInstance newQueryInstance = new SentenceQueryInstance();
                        newQueryInstance.setTokens(rule.getQueryTokens());
                        EdgeQuery newEdgeQuery = new EdgeQuery();
                        newEdgeQuery.setName(edge.getEdgeName());
                        newEdgeQuery.setValues(new HashSet<>(Collections.singleton(edge.getEdgeValue())));
                        newEdgeQuery.setIsNumeric(edge.isEdgeNumeric());
                        newEdgeQuery.setIncludeValues(false);
                        newEdgeQuery.setIsNamedEdge(false);
                        newQueryInstance.setEdges(new ArrayList<>(Collections.singleton(newEdgeQuery)));
                        newQueryInstance.setAppender(edge.getLogicalOperator().toString().toLowerCase());
                        input.getSentenceQueryInstances().add(newQueryInstance);
                    }
                    rulesApplied.add(rule);
                    break;
                }
                else if (tokensAndEdgesToMatchFound && containsNonexistentEdge && nonexistentEdgeNotFoundAndSynonymousEdgeNotFound) {
                    rulesApplied.add(rule);
                    break;
                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return new CreateSynonymQueryBusinessRuleType(input, rulesApplied);
    }

    @Override
    public List<SentenceQueryResult> modifyByBusinessRule(List<SentenceQueryResult> sentenceQueryResults, List<QueryBusinessRule.Rule> rulesApplied) {
        try {
            for (SentenceQueryResult sentenceQueryResult : sentenceQueryResults) {
                DiscreteData discreteData = sentenceQueryResult.getDiscreteData();
                List<SentenceQueryEdgeResult> edgeResults = sentenceQueryResult.getSentenceQueryEdgeResults();
                boolean newEdgeAdded = false;

                for (QueryBusinessRule.Rule rule : rulesApplied) {
                    List<QueryBusinessRule.Rule.Edge> edges = rule.getEdges();
                    boolean nonexistentEdgeFound = false;
                    boolean containsNonexistentEdge = false;

                    for (QueryBusinessRule.Rule.Edge edge : edges) {
                        boolean edgeExists = edge.isEdgeNameExists();
                        String edgeName = edge.getEdgeName();
                        if (!containsNonexistentEdge && !edgeExists)
                            containsNonexistentEdge = true;

                        if (edgeExists && isEdgeToMatchFoundInResults(edgeResults, edgeName, edge.getEdgeValue()) && !newEdgeAdded) {
                            addSynonymousEdge(edgeResults, rule, discreteData);
                            newEdgeAdded = true;
                        }
                        else if ((!edgeExists && !isEdgeToMatchNotFoundInResults(edgeResults, edgeName))) {
                            QueryBusinessRule.Rule.Edge.LogicalOperator logicalOperator = edge.getLogicalOperator();
                            if ((logicalOperator == null || logicalOperator == OR) && !newEdgeAdded) {
                                addSynonymousEdge(edgeResults, rule, discreteData);
                                newEdgeAdded = true;
                            }
                            else nonexistentEdgeFound = true;
                        }
                    }
                    if (containsNonexistentEdge && !nonexistentEdgeFound && !newEdgeAdded) {
                        addSynonymousEdge(edgeResults, rule, discreteData);
                        newEdgeAdded = true;
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return sentenceQueryResults;
    }

    private void addSynonymousEdge(List<SentenceQueryEdgeResult> edgeResults, QueryBusinessRule.Rule rule, DiscreteData discreteData) {
        SentenceQueryEdgeResult newEdgeResult = new SentenceQueryEdgeResult();
        newEdgeResult.setEdgeName(rule.getSynonymousEdge());
        List<QueryBusinessRule.Rule.SynonymousEdgeValue> synonymousEdgeValues = rule.getSynonymousEdgeValues();
        for (QueryBusinessRule.Rule.SynonymousEdgeValue value : synonymousEdgeValues) {
            if (!value.isHasMinRangeValue() && !value.isHasMaxRangeValue())
                newEdgeResult.setMatchedValue(value.getSynonymousValue());
            else if (discreteData != null) {
                int patientAge = discreteData.getPatientAge();
                if (value.isHasMinRangeValue() && value.isHasMaxRangeValue() && patientAge >= value.getMinRangeValue() && patientAge <= value.getMaxRangeValue())
                    newEdgeResult.setMatchedValue(value.getSynonymousValue());
                else if (value.isHasMinRangeValue() && !value.isHasMaxRangeValue() && patientAge >= value.getMinRangeValue())
                    newEdgeResult.setMatchedValue(value.getSynonymousValue());
            }
        }
        edgeResults.add(newEdgeResult);
    }

    private boolean isSynonymousEdgeNotFound(List<EdgeQuery> edgeQuery, String synonymousEdge, List<QueryBusinessRule.Rule.SynonymousEdgeValue> synonymousValues) {
        if (edgeQuery != null && !edgeQuery.isEmpty())
            for (EdgeQuery edge : edgeQuery) {
                if (edge != null && edge.getName() != null && edge.getName().equals(synonymousEdge)) {
                    if (synonymousValues != null && (edge.getValues() != null))
                        for (QueryBusinessRule.Rule.SynonymousEdgeValue synonymousEdgeValue : synonymousValues)
                            if (edge.getValues().contains(synonymousEdgeValue.getSynonymousValue()))
                                return false;
                }
            }
        return true;
    }

    private boolean isEdgeMatchFound(List<EdgeQuery> edgeQuery, String edgeToMatch, String valueToMatch) {
        if (edgeQuery != null && !edgeQuery.isEmpty())
            for (EdgeQuery edge : edgeQuery) {
                if (edge != null && edge.getName() != null && edge.getName().equals(edgeToMatch)) {
                    if (valueToMatch == null || (edge.getValues() != null && edge.getValues().contains(valueToMatch)))
                        return true;
                }
            }
        return false;
    }

    private boolean areEdgesToMatchFound(List<EdgeQuery> edgeQuery, Map<String, List<String>> edgesToMatch) {
        if (edgeQuery == null || edgeQuery.isEmpty())
            return false;
        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                for (String value : values)
                    if (!isEdgeMatchFound(edgeQuery, entry.getKey(), value))
                        return false;
            }
            else if (!isEdgeMatchFound(edgeQuery, entry.getKey(), null))
                return false;
        }
        return true;
    }

    private boolean isEdgeToMatchNotFoundInResults(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch) {
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult result : edgeResults) {
                if (result != null && result.getEdgeName() != null && result.getEdgeName().equals(edgeToMatch))
                    return false;
            }
        return true;
    }

    private boolean isEdgeToMatchFoundInResults(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch, String valueToMatch) {
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult result : edgeResults) {
                if (result != null && result.getEdgeName() != null && result.getEdgeName().equals(edgeToMatch))
                    if (valueToMatch == null || valueToMatch.equals(result.getMatchedValue()))
                        return true;
            }
        return false;
    }

    private void printException(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        System.out.print(writer.toString());
    }
}
