package com.mst.filter;

import java.util.*;


import com.mst.interfaces.filter.SentenceFilter;
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.SecondLargestMeasurementProcessing;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.TokenRelationshipUtil;

import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType.MEASUREMENT_ANNOTATION;
import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType.MEASUREMENT_CLASSIFICATION;
import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static com.mst.model.metadataTypes.MeasurementAnnotations.*;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType;

public class SentenceFilterImpl implements SentenceFilter {
    private WordToken fromToken;
    private WordToken toToken;
    private String measurementClassification;

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

    public EdgeMatchOnQueryResult matchEdgesOnQuery(List<TokenRelationship> existingTokenRelationships, List<EdgeQuery> edgeQueries, String searchToken, String measurementClassification, List<BusinessRule> businessRules) {
        this.measurementClassification = measurementClassification;
        SecondLargestMeasurementProcessing secondLargestMeasurementProcessingRule = null;
        if (businessRules != null)
            for (BusinessRule businessRule : businessRules) {
                if (businessRule instanceof SecondLargestMeasurementProcessing)
                    secondLargestMeasurementProcessingRule = (SecondLargestMeasurementProcessing) businessRule;
            }
        EdgeMatchOnQueryResult result = new EdgeMatchOnQueryResult();
        Map<String, List<TokenRelationship>> namedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingTokenRelationships, true);
        Map<String, List<TokenRelationship>> notNamedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingTokenRelationships, false);
        for (EdgeQuery edgeQuery : edgeQueries) {
            if (edgeQuery.getName().equals(WordEmbeddingTypes.tokenToken) && edgeQuery.getValues().isEmpty()) {
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
            if (isEdgeMeasurement && !isRangeMeasurementInRange(tokenRelationships, edgeValuesList, measurementClassification, secondLargestMeasurementProcessingRule)) {
                result.setMatch(false);
                return result;
            } else {
                replaceMeasurementTokenRelationshipsWithMeasurementClassificationTokenRelationship(existingTokenRelationships, this.measurementClassification);
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
                } else if (isEdgeNumeric && !isEdgeInRange) {
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
                } else if (!isEdgeNumeric) {
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

    private boolean isRangeMeasurementInRange(List<TokenRelationship> tokenRelationships, List<String> rangeValues, String measurementClassification, SecondLargestMeasurementProcessing secondLargestMeasurementProcessing) {
        if (!measurementClassification.equals(LARGEST) && !measurementClassification.equals(SMALLEST) && !measurementClassification.equals(MEDIAN) && !measurementClassification.equals(MEAN))
            return false;
        if (rangeValues.size() != 2)
            return false;
        List<TokenRelationship> measurements = new ArrayList<>();
        double total = 0;
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            if (tokenRelationship.getEdgeName().equals(measurement)) {
                measurements.add(tokenRelationship);
                total += Double.parseDouble(tokenRelationship.getFromToken().getToken());
            }
        }
        Collections.sort(measurements);
        if (measurements.size() < 1 || measurements.size() > 3)
            return false;
        double rangeMeasurement = 0;
        TokenRelationship measurementTokenRelationship;
        if (measurements.size() == 1) {
            measurementTokenRelationship = measurements.get(0);
            fromToken = measurementTokenRelationship.getFromToken();
            toToken = measurementTokenRelationship.getToToken();
            rangeMeasurement = Double.parseDouble(measurementTokenRelationship.getFromToken().getToken());
        } else {
            if (measurementClassification.equals(MEAN)) {
                rangeMeasurement = total / measurements.size();
                fromToken = new WordToken();
                fromToken.setToken(String.valueOf(rangeMeasurement));
                toToken = new WordToken();
            } else if (secondLargestMeasurementProcessing == null && measurements.size() == 2 && measurementClassification.equals(MEDIAN)) {
                rangeMeasurement = total / measurements.size();
                fromToken = new WordToken();
                fromToken.setToken(String.valueOf(rangeMeasurement));
                toToken = new WordToken();
            } else {
                switch (measurementClassification) {
                    case LARGEST:
                        measurementTokenRelationship = measurements.get(measurements.size() - 1);
                        fromToken = measurementTokenRelationship.getFromToken();
                        toToken = measurementTokenRelationship.getToToken();
                        rangeMeasurement = Double.parseDouble(measurementTokenRelationship.getFromToken().getToken());
                        break;
                    case SMALLEST:
                        measurementTokenRelationship = measurements.get(0);
                        fromToken = measurementTokenRelationship.getFromToken();
                        toToken = measurementTokenRelationship.getToToken();
                        rangeMeasurement = Double.parseDouble(measurementTokenRelationship.getFromToken().getToken());
                        break;
                    case MEDIAN:
                        if (secondLargestMeasurementProcessing != null) {
                            rangeMeasurement = getSecondLargestMeasurement(measurements, secondLargestMeasurementProcessing);
                            this.measurementClassification = SECOND_LARGEST;
                        } else {
                            measurementTokenRelationship = measurements.get(1);
                            fromToken = measurementTokenRelationship.getFromToken();
                            toToken = measurementTokenRelationship.getToToken();
                            rangeMeasurement = Double.parseDouble(measurementTokenRelationship.getFromToken().getToken());
                        }
                }
            }
        }
        double min = Math.min(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        double max = Math.max(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        return rangeMeasurement >= min && rangeMeasurement <= max;
    }

    private double getSecondLargestMeasurement(List<TokenRelationship> measurements, SecondLargestMeasurementProcessing secondLargestMeasurementProcessing) {
        double secondLargestMeasurement = 0;
        int numberDimensions = measurements.size();
        Map<String, TokenRelationship> axisAnnotations = new HashMap<>();
        for (TokenRelationship measurement : measurements) {
            String descriptor = measurement.getDescriptor();
            if (descriptor != null && (descriptor.equals(LENGTH) || descriptor.equals(TRANSVERSE) || descriptor.equals(AP)))
                axisAnnotations.put(descriptor, measurement);
        }
        List<BusinessRule> rules = secondLargestMeasurementProcessing.getRules();
        for (BusinessRule baseRule : rules) {
            SecondLargestMeasurementProcessing rule = (SecondLargestMeasurementProcessing) baseRule;
            if (rule.getNumberDimensions() != numberDimensions)
                continue;
            List<String> ruleAnnotations = rule.getAxisAnnotations();
            boolean annotationNotFound = false;
            if (ruleAnnotations != null && !ruleAnnotations.isEmpty()) {
                for (String ruleAnnotation : ruleAnnotations)
                    if (!axisAnnotations.containsKey(ruleAnnotation))
                        annotationNotFound = true;
            }
            if (annotationNotFound)
                continue;
            String secondLargestIdentifier = rule.getSecondLargestIdentifier();
            IdentifierType identifierType = rule.getIdentifierType();
            List<String> largestBetweenAnnotations = rule.getLargestBetweenAnnotations();
            if (identifierType.equals(MEASUREMENT_ANNOTATION)) {
                TokenRelationship measurement = axisAnnotations.get(secondLargestIdentifier);
                fromToken = measurement.getFromToken();
                toToken = measurement.getToToken();
                return Double.parseDouble(measurement.getFromToken().getToken());
            } else if (identifierType.equals(MEASUREMENT_CLASSIFICATION)) {
                switch (secondLargestIdentifier) {
                    case LARGEST:
                        if (largestBetweenAnnotations != null && !largestBetweenAnnotations.isEmpty()) {
                            for (String annotation : largestBetweenAnnotations) {
                                TokenRelationship measurement = axisAnnotations.get(annotation);
                                double annotationValue = Double.parseDouble(measurement.getFromToken().getToken());
                                if (annotationValue > secondLargestMeasurement) {
                                    fromToken = measurement.getFromToken();
                                    toToken = measurement.getToToken();
                                    secondLargestMeasurement = annotationValue;
                                }
                            }
                            return secondLargestMeasurement;
                        } else {
                            TokenRelationship measurement = measurements.get(measurements.size() - 1);
                            fromToken = measurement.getFromToken();
                            toToken = measurement.getToToken();
                            return Double.parseDouble(measurement.getFromToken().getToken());
                        }
                    case MEDIAN:
                        if (numberDimensions == 3) {
                            TokenRelationship measurement = measurements.get(1);
                            fromToken = measurement.getFromToken();
                            toToken = measurement.getToToken();
                            return Double.parseDouble(measurement.getFromToken().getToken());
                        }
                }
            }
        }
        return secondLargestMeasurement;
    }

    private void replaceMeasurementTokenRelationshipsWithMeasurementClassificationTokenRelationship(List<TokenRelationship> tokenRelationships, String measurementClassification) {
        ListIterator<TokenRelationship> itr = tokenRelationships.listIterator();
        while (itr.hasNext())
            if (itr.next().getEdgeName().equals(measurement)) {
                itr.remove();
            }
        TokenRelationship tokenRelationship = new TokenRelationship();
        tokenRelationship.setEdgeName(measurement);
        tokenRelationship.setDescriptor(measurementClassification);
        tokenRelationship.setSource(this.getClass().getSimpleName());
        tokenRelationship.setFromToken(fromToken);
        tokenRelationship.setToToken(toToken);
        tokenRelationships.add(tokenRelationship);
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
