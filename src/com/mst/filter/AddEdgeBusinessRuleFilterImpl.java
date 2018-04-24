package com.mst.filter;

import com.mst.interfaces.filter.BusinessRuleFilter;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.businessRule.AddEdgeToQueryResults;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.discrete.DiscreteData;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static com.mst.model.businessRule.AddEdgeToQueryResults.Edge.LogicalOperator.OR;

public class AddEdgeBusinessRuleFilterImpl implements BusinessRuleFilter {
    @Override
    public List<SentenceQueryResult> filterByBusinessRule(List<SentenceQueryResult> nonFilteredResults, BusinessRule rule) {
        return null;
    }

    @Override
    public SentenceQueryInput modifySentenceQueryInput(SentenceQueryInput input, BusinessRule rule) {
        return null;
    }

    @Override
    public List<SentenceQueryResult> modifySentenceQueryResults(List<SentenceQueryResult> sentenceQueryResults, BusinessRule businessRule) {
        try {
            List<BusinessRule> rules = businessRule.getRules();

            for (SentenceQueryResult sentenceQueryResult : sentenceQueryResults) {
                DiscreteData discreteData = sentenceQueryResult.getDiscreteData();
                List<SentenceQueryEdgeResult> edgeResults = sentenceQueryResult.getSentenceQueryEdgeResults();
                String sentence = sentenceQueryResult.getSentence().toLowerCase();
                boolean newEdgeAdded = false;

                for (BusinessRule baseRule : rules) {
                    if (newEdgeAdded)
                        break;
                    AddEdgeToQueryResults rule = (AddEdgeToQueryResults) baseRule;
                    if (!areEdgesToMatchFound(edgeResults, rule.getEdgesToMatch()))
                        continue;
                    if (!isEdgeToAddNotFound(edgeResults, rule.getEdgeToAdd(), rule.getEdgeToAddValues()))
                        continue;
                    List<AddEdgeToQueryResults.Edge> specialEdges = rule.getSpecialEdges();
                    boolean nonexistentEdgeFound = false;
                    boolean containsNonexistentEdge = false;

                    if (specialEdges != null && !specialEdges.isEmpty()) {
                        for (AddEdgeToQueryResults.Edge specialEdge : specialEdges) {
                            if (!containsNonexistentEdge && !specialEdge.isEdgeExists())
                                containsNonexistentEdge = true;

                            if (specialEdge.isEdgeExists() && rule.isSearchSentenceForSpecialEdges()) {
                                //TODO logical AND operator is not currently used and is not implemented
                                if (sentence.contains(specialEdge.getEdgeName().toLowerCase()) && specialEdge.getLogicalOperator() == OR) {
                                    addEdge(edgeResults, rule, discreteData);
                                    newEdgeAdded = true;
                                }
                            } else if (specialEdge.isEdgeExists() && isEdgeToMatchFound(edgeResults, specialEdge.getEdgeName(), specialEdge.getEdgeValue()) && !newEdgeAdded) {
                                addEdge(edgeResults, rule, discreteData);
                                newEdgeAdded = true;
                            } else if ((!specialEdge.isEdgeExists() && !isSpecialEdgeNotFound(edgeResults, specialEdge.getEdgeName()))) {
                                AddEdgeToQueryResults.Edge.LogicalOperator logicalOperator = specialEdge.getLogicalOperator();
                                if ((logicalOperator == null || logicalOperator == OR) && !newEdgeAdded) {
                                    addEdge(edgeResults, rule, discreteData);
                                    newEdgeAdded = true;
                                } else nonexistentEdgeFound = true;
                            }
                        }
                        if (containsNonexistentEdge && !nonexistentEdgeFound && !newEdgeAdded) {
                            addEdge(edgeResults, rule, discreteData);
                            newEdgeAdded = true;
                        }
                    }
                    else {
                        addEdge(edgeResults, rule, discreteData);
                        newEdgeAdded = true;
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return sentenceQueryResults;
    }

    private void addEdge(List<SentenceQueryEdgeResult> edgeResults, AddEdgeToQueryResults rule, DiscreteData discreteData) {
        SentenceQueryEdgeResult newEdgeResult = new SentenceQueryEdgeResult();
        newEdgeResult.setEdgeName(rule.getEdgeToAdd());
        List<AddEdgeToQueryResults.EdgeToAddValue> edgeToAddValues = rule.getEdgeToAddValues();
        for (AddEdgeToQueryResults.EdgeToAddValue value : edgeToAddValues) {
            if (!value.isHasMinRangeValue() && !value.isHasMaxRangeValue())
                newEdgeResult.setMatchedValue(value.getValue());
            else if (discreteData != null) {
                int patientAge = discreteData.getPatientAge();
                if (value.isHasMinRangeValue() && value.isHasMaxRangeValue() && patientAge >= value.getMinRangeValue() && patientAge <= value.getMaxRangeValue())
                    newEdgeResult.setMatchedValue(value.getValue());
                else if (value.isHasMinRangeValue() && !value.isHasMaxRangeValue() && patientAge >= value.getMinRangeValue())
                    newEdgeResult.setMatchedValue(value.getValue());
            }
        }
        edgeResults.add(newEdgeResult);
    }

    private boolean isSpecialEdgeNotFound(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch) {
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult result : edgeResults) {
                if (result != null && result.getEdgeName() != null && result.getEdgeName().equals(edgeToMatch))
                    return false;
            }
        return true;
    }

    private boolean isEdgeToMatchFound(List<SentenceQueryEdgeResult> edgeResults, String edgeToMatch, String valueToMatch) {
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult edge : edgeResults) {
                if (edge != null && edge.getEdgeName() != null && edge.getEdgeName().equals(edgeToMatch)) {
                    if (valueToMatch == null || (edge.getMatchedValue() != null && edge.getMatchedValue().equals(valueToMatch))) {
                        return true;
                    }
                }
            }
        return false;
    }

    private boolean areEdgesToMatchFound(List<SentenceQueryEdgeResult> edgeResults, Map<String, List<String>> edgesToMatch) {
        if (edgeResults == null || edgeResults.isEmpty())
            return false;
        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                boolean noValuesMatch = true;
                for (String value : values)
                    if (isEdgeToMatchFound(edgeResults, entry.getKey(), value))
                        noValuesMatch = false;
                if (noValuesMatch)
                    return false;
            } else if (!isEdgeToMatchFound(edgeResults, entry.getKey(), null))
                return false;
        }
        return true;
    }

    private boolean isEdgeToAddNotFound(List<SentenceQueryEdgeResult> edgeResults, String edgeToAdd, List<AddEdgeToQueryResults.EdgeToAddValue> values) {
        if (edgeResults != null && !edgeResults.isEmpty())
            for (SentenceQueryEdgeResult edge : edgeResults) {
                if (edge != null && edge.getEdgeName() != null && edge.getEdgeName().equals(edgeToAdd)) {
                    if (values != null && (edge.getMatchedValue() != null))
                        for (AddEdgeToQueryResults.EdgeToAddValue edgeToAddValue : values)
                            if (edge.getMatchedValue().equals(edgeToAddValue.getValue()))
                                return false;
                }
            }
        return true;
    }

    private void printException(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        System.out.print(writer.toString());
    }
}
