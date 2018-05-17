package com.mst.filter;

import com.mst.interfaces.filter.BusinessRuleFilter;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.AddEdgeToResult;
import com.mst.model.businessRule.AppendToInput;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.RemoveEdgeFromResult;
import com.mst.model.discrete.DiscreteData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR;
import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR_NOT;


public class BusinessRuleFilterImpl implements BusinessRuleFilter {
    private static final Logger logger = LogManager.getLogger(BusinessRuleFilterImpl.class);

    @Override
    public void modifySentenceQueryInput(SentenceQueryInput input, List<BusinessRule> businessRules) {
        for (BusinessRule businessRule : businessRules) {
            if (businessRule instanceof AppendToInput)
                processAppendToInput(input, (AppendToInput) businessRule);
        }
    }

    @Override
    public void modifySentenceQueryResult(List<SentenceQueryResult> results, List<BusinessRule> businessRules) {
        for (BusinessRule businessRule : businessRules) {
            if (businessRule instanceof AddEdgeToResult)
                processAddEdgeToResult(results, (AddEdgeToResult) businessRule);
            else if (businessRule instanceof RemoveEdgeFromResult)
                processRemoveEdgeFromResult(results, (RemoveEdgeFromResult) businessRule);
        }
    }

    private void processAppendToInput(SentenceQueryInput input, AppendToInput businessRule) {
        try {
            List<BusinessRule> rules = businessRule.getRules();
            List<SentenceQueryInstance> instances = input.getSentenceQueryInstances();
            ListIterator<SentenceQueryInstance> itr = instances.listIterator();

            while (itr.hasNext()) {
                SentenceQueryInstance instance = itr.next();
                List<EdgeQuery> edges = instance.getEdges();

                for (BusinessRule baseRule : rules) {
                    AppendToInput rule = (AppendToInput) baseRule;
                    if (!areEdgesToMatchFoundInInput(edges, rule.getEdgesToMatch()))
                        continue;
                    String edgeToAppend = rule.getEdgeToAppend();
                    SentenceQueryInstance newInstance = new SentenceQueryInstance();
                    newInstance.setTokens(instance.getTokens());
                    List<EdgeQuery> newEdges = new ArrayList<>();
                    BusinessRule.LogicalOperator logicalOperator = rule.getLogicalOperator();

                    if (logicalOperator == OR_NOT) { //TODO need to implement other logical operators
                        for (EdgeQuery edge : edges) {
                            if (!edge.getName().equals(edgeToAppend))
                                newEdges.add(edge);
                        }
                        newInstance.setAppender("or");
                        newInstance.setEdges(newEdges);
                    }

                    itr.add(newInstance);
                }
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private void processAddEdgeToResult(List<SentenceQueryResult> sentenceQueryResults, AddEdgeToResult businessRule) {
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
                    AddEdgeToResult rule = (AddEdgeToResult) baseRule;
                    if (!areEdgesToMatchFoundInResults(edgeResults, rule.getEdgesToMatch()) || !isEdgeToAddNotFoundInResults(edgeResults, rule.getEdgeToAdd()))
                        continue;
                    List<AddEdgeToResult.Edge> specialEdges = rule.getSpecialEdges();
                    boolean nonexistentEdgeFound = false;
                    boolean containsNonexistentEdge = false;

                    if (specialEdges != null && !specialEdges.isEmpty()) {
                        for (AddEdgeToResult.Edge specialEdge : specialEdges) {
                            if (!containsNonexistentEdge && !specialEdge.isEdgeExists())
                                containsNonexistentEdge = true;

                            if (specialEdge.isEdgeExists() && rule.isSearchSentenceForSpecialEdges()) {
                                //TODO logical AND operator is not currently used and is not implemented
                                if (sentence.contains(specialEdge.getEdgeName().toLowerCase()) && specialEdge.getLogicalOperator() == OR) {
                                    addEdgeToResults(edgeResults, rule, discreteData);
                                    newEdgeAdded = true;
                                }
                            } else if (specialEdge.isEdgeExists() && isEdgeToMatchFoundInResults(edgeResults, specialEdge.getEdgeName(), specialEdge.getEdgeValue()) && !newEdgeAdded) {
                                addEdgeToResults(edgeResults, rule, discreteData);
                                setDisplayEdgeTrue(edgeResults, specialEdge.getEdgeName(), specialEdge.getEdgeValue());
                                newEdgeAdded = true;
                            } else if ((!specialEdge.isEdgeExists() && !isSpecialEdgeNotFoundInResults(edgeResults, specialEdge.getEdgeName(), specialEdge.getEdgeValue()))) {
                                BusinessRule.LogicalOperator logicalOperator = specialEdge.getLogicalOperator();
                                if ((logicalOperator == null || logicalOperator == OR) && !newEdgeAdded) {
                                    addEdgeToResults(edgeResults, rule, discreteData);
                                    newEdgeAdded = true;
                                } else nonexistentEdgeFound = true;
                            }
                        }
                        if (containsNonexistentEdge && !nonexistentEdgeFound && !newEdgeAdded) {
                            addEdgeToResults(edgeResults, rule, discreteData);
                            newEdgeAdded = true;
                        }
                    } else {
                        addEdgeToResults(edgeResults, rule, discreteData);
                        newEdgeAdded = true;
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private void processRemoveEdgeFromResult(List<SentenceQueryResult> sentenceQueryResults, RemoveEdgeFromResult businessRule) {
        try {
            List<BusinessRule> rules = businessRule.getRules();
            for (SentenceQueryResult sentenceQueryResult : sentenceQueryResults) {
                List<SentenceQueryEdgeResult> edgeResults = sentenceQueryResult.getSentenceQueryEdgeResults();

                for (BusinessRule baseRule : rules) {
                    RemoveEdgeFromResult rule = (RemoveEdgeFromResult) baseRule;
                    String edgeToRemove = rule.getEdgeToRemove();
                    boolean removeIfNull = rule.isRemoveIfNull();
                    List<String> values = rule.getEdgeToRemoveValues();
                    ListIterator<SentenceQueryEdgeResult> itr = edgeResults.listIterator();

                    while (itr.hasNext()) {
                        SentenceQueryEdgeResult edgeResult = itr.next();
                        boolean nullMatch = edgeResult.getMatchedValue() == null && removeIfNull;
                        if (edgeResult.getEdgeName().equals(edgeToRemove) && (nullMatch || (values != null && values.contains(edgeResult.getMatchedValue()))))
                            itr.remove();
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private void setDisplayEdgeTrue(List<SentenceQueryEdgeResult> results, String edgeName, String value) {
        for (SentenceQueryEdgeResult result : results)
            if (result.getEdgeName().equals(edgeName) && result.getMatchedValue().equals(value)) {
                result.setDisplayEdge(true);
                return;
            }
    }

    private void addEdgeToResults(List<SentenceQueryEdgeResult> results, AddEdgeToResult rule, DiscreteData discreteData) {
        SentenceQueryEdgeResult newEdgeResult = new SentenceQueryEdgeResult();
        newEdgeResult.setEdgeName(rule.getEdgeToAdd());
        newEdgeResult.setDisplayEdge(true);
        List<AddEdgeToResult.EdgeToAddValue> edgeToAddValues = rule.getEdgeToAddValues();
        for (AddEdgeToResult.EdgeToAddValue value : edgeToAddValues) {
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
        results.add(newEdgeResult);
    }

    private boolean isSpecialEdgeNotFoundInResults(List<SentenceQueryEdgeResult> results, String specialEdge, String value) {
        if (results != null && !results.isEmpty())
            for (SentenceQueryEdgeResult edge : results) {
                if (edge != null && edge.getEdgeName() != null && edge.getEdgeName().equals(specialEdge) && edge.getMatchedValue().equals(value))
                    return false;
            }
        return true;
    }

    private boolean isEdgeToMatchFoundInResults(List<SentenceQueryEdgeResult> results, String edgeToMatch, String valueToMatch) {
        if (results != null && !results.isEmpty())
            for (SentenceQueryEdgeResult edge : results) {
                if (edge != null && edge.getEdgeName() != null && edge.getEdgeName().equals(edgeToMatch)) {
                    if (valueToMatch == null || (edge.getMatchedValue() != null && edge.getMatchedValue().equals(valueToMatch))) {
                        return true;
                    }
                }
            }
        return false;
    }

    private boolean isEdgeToMatchFoundInInput(List<EdgeQuery> input, String edgeToMatch, String valueToMatch) {
        if (input != null && !input.isEmpty())
            for (EdgeQuery edge : input) {
                if (edge != null && edge.getName() != null && edge.getName().equals(edgeToMatch)) {
                    if (valueToMatch == null || (edge.getValues() != null && !edge.getValues().isEmpty()) && edge.getValues().contains(valueToMatch)) {
                        return true;
                    }
                }
            }
        return false;
    }

    private boolean areEdgesToMatchFoundInInput(List<EdgeQuery> input, Map<String, List<String>> edgesToMatch) {
        if (input == null || input.isEmpty())
            return false;
        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                boolean noValuesMatch = true;
                for (String value : values)
                    if (isEdgeToMatchFoundInInput(input, entry.getKey(), value))
                        noValuesMatch = false;
                if (noValuesMatch)
                    return false;
            } else if (!isEdgeToMatchFoundInInput(input, entry.getKey(), null))
                return false;
        }
        return true;
    }

    private boolean areEdgesToMatchFoundInResults(List<SentenceQueryEdgeResult> results, Map<String, List<String>> edgesToMatch) {
        if (results == null || results.isEmpty())
            return false;
        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                boolean noValuesMatch = true;
                for (String value : values)
                    if (isEdgeToMatchFoundInResults(results, entry.getKey(), value))
                        noValuesMatch = false;
                if (noValuesMatch)
                    return false;
            } else if (!isEdgeToMatchFoundInResults(results, entry.getKey(), null))
                return false;
        }
        return true;
    }

    private boolean isEdgeToAddNotFoundInResults(List<SentenceQueryEdgeResult> results, String edgeToAdd) {
        if (results != null && !results.isEmpty())
            for (SentenceQueryEdgeResult edge : results) {
                if (edge != null && edge.getEdgeName() != null && edge.getEdgeName().equals(edgeToAdd))
                    return false;
            }
        return true;
    }

    private void printException(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        System.out.print(writer.toString());
        logger.debug(writer.toString());
    }
}
