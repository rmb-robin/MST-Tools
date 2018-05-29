package com.mst.filter;

import java.util.*;


import com.mst.interfaces.filter.SentenceFilter;
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.TokenRelationshipUtil;

import static com.mst.model.metadataTypes.Descriptor.X_AXIS;
import static com.mst.model.metadataTypes.Descriptor.Y_AXIS;
import static com.mst.model.metadataTypes.Descriptor.Z_AXIS;
import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static com.mst.model.metadataTypes.MeasurementClassification.*;

public class SentenceFilterImpl implements SentenceFilter {

    public ShouldMatchOnSentenceEdgesResult shouldAddTokenFromRelationship(TokenRelationship relation, String token) {
        ShouldMatchOnSentenceEdgesResult result = new ShouldMatchOnSentenceEdgesResult();
        result.setMatch(false);
        if (token.split(" ").length > 1) {
            token = token.replace(" ", "-");
        }
        if (relation.getFromToken() == null)
            return result;
        else if (relation.getToToken() == null)
            return result;
        else if (relation.getEdgeName().equals(measurement))
            return result;
        else if (relation.getFromToken().getToken().equals(token)) {
            result.setMatch(true);
            return result;
        } else if (relation.getToToken().getToken().equals(token)) {
            result.setMatch(true);
            return result;
        }
        return result;
    }

    public EdgeMatchOnQueryResult matchEdgesOnQuery(List<TokenRelationship> existingTokenRelationships, List<EdgeQuery> edgeQueries, String searchToken, String measurementClassification) {
        EdgeMatchOnQueryResult result = new EdgeMatchOnQueryResult();
        Map<String, List<TokenRelationship>> namedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingTokenRelationships, true);
        Map<String, List<TokenRelationship>> notNamedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingTokenRelationships, false);
        for (EdgeQuery edgeQuery : edgeQueries) {
            if (edgeQuery.getName().equals(WordEmbeddingTypes.defaultEdge) && edgeQuery.getValues().isEmpty()) {
                edgeQuery.getValues().add(searchToken);
            }
            HashSet<String> edgeValues = edgeQuery.getValuesLower();
            if (edgeQuery.getName().equals(EdgeNames.existence)) {
                if (!isMatchOnExistence(notNamedRelationshipsByEdgeName, searchToken)) {
                    result.setMatch(false);
                    return result;
                } else {
                    MatchInfo info = new MatchInfo();
                    info.setValue(searchToken);
                    result.getMatches().put(edgeQuery.getName(), info);
                    continue;
                }
            } else {
                if (!isEdgeNotInQuery(edgeQuery, namedRelationshipsByEdgeName, notNamedRelationshipsByEdgeName))
                    continue;
                if (edgeValues == null || edgeValues.isEmpty())
                    continue;
            }
            result.setDidTokenRelationsContainAnyMatches(true);
            List<String> edgeValuesList = new ArrayList<>(edgeValues);
            List<TokenRelationship> tokenRelationships = getTokenRelationshipByQueryEdgeName(edgeQuery, namedRelationshipsByEdgeName, notNamedRelationshipsByEdgeName);
            if (edgeQuery.getIsNumeric() == null)
                edgeQuery.setIsNumeric(isEdgeQueryNumeric(edgeValuesList));
            boolean isEdgeNumeric = edgeQuery.getIsNumeric();
            boolean isEdgeMeasurement = edgeQuery.getName().equals(measurement);
            if (isEdgeMeasurement && !areMeasurementsInRange(tokenRelationships, edgeValuesList, measurementClassification)) {
                result.setMatch(false);
                return result;
            }
            boolean isEdgeInRange = false;
            int matchCount = 0;
            for (TokenRelationship relationship : tokenRelationships) {

               if (isEdgeMeasurement) {
                    MatchInfo matchInfo = createMatchInfo(true, "from", relationship.getFromToken().getToken());
                    if (matchInfo != null) {
                        result.getMatches().put(edgeQuery.getName(), matchInfo);
                        matchCount += 1;
                    }
                }
                else if (isEdgeNumeric && !isEdgeInRange) {
                    if (relationship.getFromToken().isCardinal()) {
                        isEdgeInRange = isNumericInRange(edgeValuesList, relationship.getFromToken().getToken());
                        MatchInfo matchInfo = createMatchInfo(isEdgeInRange, "from", relationship.getFromToken().getToken());
                        if (matchInfo != null) {
                            result.getMatches().put(edgeQuery.getName(), matchInfo);
                            matchCount += 1;
                            continue;
                        }
                    }
                    if (relationship.getToToken().isCardinal())
                        isEdgeInRange = isNumericInRange(edgeValuesList, relationship.getToToken().getToken());
                    MatchInfo matchInfo = createMatchInfo(isEdgeInRange, "to", relationship.getToToken().getToken());
                    if (matchInfo != null) {
                        result.getMatches().put(edgeQuery.getName(), matchInfo);
                        matchCount += 1;
                    }
                }
                else if (!isEdgeNumeric) {
                    if (edgeValues.contains(relationship.getFromToken().getToken()) || edgeValues.contains(relationship.getToToken().getToken())) {
                        MatchInfo info = new MatchInfo();
                        if (edgeValues.contains(relationship.getFromToken().getToken())) {
                            info.setTokenType("from");
                            info.setValue(relationship.getFromToken().getToken());
                            matchCount += 1;
                        } else {
                            info.setTokenType("to");
                            info.setValue(relationship.getToToken().getToken());
                            matchCount += 1;
                        }
                        result.getMatches().put(edgeQuery.getName(), info);
                    }
                }
            }
            if (matchCount == 0) {
                result.setMatch(false);
                return result;
            }
        }
        result.setMatch(true);
        return result;
    }

    private boolean areMeasurementsInRange(List<TokenRelationship> tokenRelationships, List<String> rangeValues, String measurementClassification) {
        if (!measurementClassification.equals(LARGEST) && !measurementClassification.equals(SMALLEST) && !measurementClassification.equals(MEDIAN) && !measurementClassification.equals(MEAN))
            return false;
        if (rangeValues.size() != 2)
            return false;
        List<TokenRelationship> measurements = new ArrayList<>();
        double total = 0;
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            String descriptor = tokenRelationship.getDescriptor();
            if (tokenRelationship.getEdgeName().equals(measurement) && (descriptor.equals(X_AXIS) || descriptor.equals(Y_AXIS) || descriptor.equals(Z_AXIS))) {
                measurements.add(tokenRelationship);
                total += Double.parseDouble(tokenRelationship.getFromToken().getToken());
            }
        }
        Collections.sort(measurements);
        if (measurements.size() < 1 || measurements.size() > 3)
            return false;
        double min = Math.min(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        double max = Math.max(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        if (measurements.size() == 1) {
            double measurement = Double.parseDouble(measurements.get(0).getFromToken().getToken());
            return measurement >= min && measurement <= max;
        } else {
            if (measurementClassification.equals(MEAN) || (measurements.size() == 2 && measurementClassification.equals(MEDIAN))) {
                double measurement = total / measurements.size();
                return measurement >= min && measurement <= max;
            }
            else {
                switch (measurementClassification) {
                    case LARGEST:
                        double largest = Double.parseDouble(measurements.get(measurements.size() - 1).getFromToken().getToken());
                        return largest >= min && largest <= max;
                    case SMALLEST:
                        double smallest = Double.parseDouble(measurements.get(0).getFromToken().getToken());
                        return smallest >= min && smallest <= max;
                    case MEDIAN:
                        double median = Double.parseDouble(measurements.get(1).getFromToken().getToken());
                        return median >= min && median <= max;
                }
            }
        }
        return false;
    }

    private boolean isMatchOnExistence(Map<String, List<TokenRelationship>> relationshipsByEdgeName, String searchToken) {
        if (!isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existence))
            return false;
        if (isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existenceMaybe))
            return false;
        if (isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existenceNo))
            return false;
        return !isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existencePossibility);
    }

    private boolean isMatchOnEdgeName(Map<String, List<TokenRelationship>> relationshipsByEdgeName, String searchToken, String edgeName) {
        if (!relationshipsByEdgeName.containsKey(edgeName))
            return false;
        for (TokenRelationship relationship : relationshipsByEdgeName.get(edgeName)) {
            if (relationship.isToFromTokenMatch(searchToken))
                return true;
        }
        return false;
    }

    private MatchInfo createMatchInfo(boolean isRange, String tokenType, String value) {
        if (!isRange)
            return null;
        MatchInfo info = new MatchInfo();
        info.setTokenType(tokenType);
        info.setValue(value);
        return info;
    }

    private boolean isNumericInRange(List<String> edgeValues, String relationShipValue) {
        if (!isNumericValue(relationShipValue))
            return false;
        double value = Double.parseDouble(relationShipValue);
        double valueOne = Double.parseDouble(edgeValues.get(0));
        double valueTwo = Double.parseDouble(edgeValues.get(1));
        double min = Math.min(valueOne, valueTwo);
        double max = Math.max(valueOne, valueTwo);
        return value >= min && value <= max;
    }

    private boolean isEdgeQueryNumeric(List<String> edgeValues) {
        if (edgeValues.size() > 2)
            return false;
        if (!isNumericValue(edgeValues.get(0)))
            return false;
        return isNumericValue(edgeValues.get(1));
    }

    private boolean isNumericValue(String value) {
        return value.matches("[-+]?\\d*\\.?\\d+");
    }

    private boolean isEdgeNotInQuery(EdgeQuery edgeQuery, Map<String, List<TokenRelationship>> namedRelationshipsByEdgeName, Map<String, List<TokenRelationship>> notNamedRelationshipsByEdgeName) {
        if (edgeQuery.getIsNamedEdge()) {
            return namedRelationshipsByEdgeName.containsKey(edgeQuery.getName());
        }
        return notNamedRelationshipsByEdgeName.containsKey(edgeQuery.getName());
    }

    private List<TokenRelationship> getTokenRelationshipByQueryEdgeName(EdgeQuery edgeQuery, Map<String, List<TokenRelationship>> namedRelationshipsByEdgeName, Map<String, List<TokenRelationship>> notNamedRelationshipsByEdgeName) {
        if (edgeQuery.getIsNamedEdge()) {
            return namedRelationshipsByEdgeName.get(edgeQuery.getName());
        }
        return notNamedRelationshipsByEdgeName.get(edgeQuery.getName());
    }
}
