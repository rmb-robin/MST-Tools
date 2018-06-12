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
            if (isEdgeMeasurement && !areMeasurementsInRange(tokenRelationships, edgeValuesList, measurementClassification, secondLargestMeasurementProcessingRule)) {
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

    private class SecondLargestProcessingResult {
        boolean measurementsInRange;
        double measurementValue;
        String identifier;
        IdentifierType identifierType;

        SecondLargestProcessingResult(boolean measurementsInRange) {
            this.measurementsInRange = measurementsInRange;
        }
    }

    private boolean areMeasurementsInRange(List<TokenRelationship> tokenRelationships, List<String> rangeValues, String measurementClassification, SecondLargestMeasurementProcessing secondLargestMeasurementProcessing) {
        final String SECOND_LARGEST = "2nd largest";
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
        double min = Math.min(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        double max = Math.max(Double.parseDouble(rangeValues.get(0)), Double.parseDouble(rangeValues.get(1)));
        if (measurements.size() == 1) {
            double measurement = Double.parseDouble(measurements.get(0).getFromToken().getToken());
            return measurement >= min && measurement <= max;
        } else {
            if (measurementClassification.equals(MEAN)) {
                double measurement = total / measurements.size();
                return measurement >= min && measurement <= max;
            } else if (secondLargestMeasurementProcessing == null && measurements.size() == 2 && measurementClassification.equals(MEDIAN)) {
                double measurement = total / measurements.size();
                return measurement >= min && measurement <= max;
            } else {
                switch (measurementClassification) {
                    case LARGEST:
                        double largest = Double.parseDouble(measurements.get(measurements.size() - 1).getFromToken().getToken());
                        return largest >= min && largest <= max;
                    case SMALLEST:
                        double smallest = Double.parseDouble(measurements.get(0).getFromToken().getToken());
                        return smallest >= min && smallest <= max;
                    case MEDIAN:
                        if (secondLargestMeasurementProcessing == null) {
                            double median = Double.parseDouble(measurements.get(1).getFromToken().getToken());
                            return median >= min && median <= max;
                        } else {
                            SecondLargestProcessingResult result = processSecondLargestMeasurement(min, max, measurements, secondLargestMeasurementProcessing);
                            if (result.measurementsInRange) {
                                String identifier = result.identifier;
                                IdentifierType identifierType = result.identifierType;
                                if (identifierType.equals(MEASUREMENT_ANNOTATION)) {
                                    for (TokenRelationship measurement : measurements) {
                                        String descriptor = measurement.getDescriptor();
                                        if (measurement.getEdgeName().equals(EdgeNames.measurement) && descriptor.equals(identifier)) {
                                            measurement.setDescriptor(SECOND_LARGEST);
                                            WordToken secondLargest = measurement.getFromToken();
                                            secondLargest.setToken(String.valueOf(result.measurementValue));
                                            secondLargest.setDescriptor(SECOND_LARGEST);
                                            measurement.setFromToken(secondLargest);
                                        }
                                    }
                                } else if (identifierType.equals(MEASUREMENT_CLASSIFICATION)) {
                                    TokenRelationship measurement;
                                    switch (identifier) {
                                        case LARGEST:
                                            measurement = measurements.get(measurements.size() - 1);
                                            measurement.setDescriptor(SECOND_LARGEST);
                                            WordToken secondLargest = measurement.getFromToken();
                                            secondLargest.setToken(String.valueOf(result.measurementValue));
                                            secondLargest.setDescriptor(SECOND_LARGEST);
                                            measurement.setFromToken(secondLargest);
                                            break;
                                        case MEDIAN:
                                            if (measurements.size() == 3) {
                                                measurement = measurements.get(1);
                                                measurement.setDescriptor(SECOND_LARGEST);
                                                secondLargest = measurement.getFromToken();
                                                secondLargest.setToken(String.valueOf(result.measurementValue));
                                                secondLargest.setDescriptor(SECOND_LARGEST);
                                                measurement.setFromToken(secondLargest);
                                            }
                                    }
                                }
                            }
                            return result.measurementsInRange;
                        }
                }
            }
        }
        return false;
    }

    private SecondLargestProcessingResult processSecondLargestMeasurement(double min, double max, List<TokenRelationship> measurements, SecondLargestMeasurementProcessing secondLargestMeasurementProcessing) {
        int numberDimensions = measurements.size();
        Map<String, TokenRelationship> axisAnnotations = new HashMap<>();
        for (TokenRelationship measurement : measurements) {
            String descriptor = measurement.getDescriptor();
            if (descriptor.equals(LENGTH) || descriptor.equals(TRANSVERSE) || descriptor.equals(AP))
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
            SecondLargestProcessingResult result = new SecondLargestProcessingResult(true);
            result.identifier = secondLargestIdentifier;
            result.identifierType = identifierType;
            List<String> largestBetweenAnnotations = rule.getLargestBetweenAnnotations();
            if (identifierType.equals(MEASUREMENT_ANNOTATION)) {
                TokenRelationship measurement = axisAnnotations.get(secondLargestIdentifier);
                result.measurementValue = Double.parseDouble(measurement.getFromToken().getToken());
                result.measurementsInRange = result.measurementValue >= min && result.measurementValue <= max;
                return result;
            } else if (identifierType.equals(MEASUREMENT_CLASSIFICATION)) {
                switch (secondLargestIdentifier) {
                    case LARGEST:
                        if (largestBetweenAnnotations != null && !largestBetweenAnnotations.isEmpty()) {
                            double largest = 0;
                            for (String annotation : largestBetweenAnnotations) {
                                TokenRelationship measurement = axisAnnotations.get(annotation);
                                double annotationValue = Double.parseDouble(measurement.getFromToken().getToken());
                                if (annotationValue > largest)
                                    largest = annotationValue;
                            }
                            result.measurementValue = largest;
                            result.measurementsInRange = largest >= min && largest <= max;
                            return result;
                        } else {
                            TokenRelationship measurement = measurements.get(measurements.size() - 1);
                            result.measurementValue = Double.parseDouble(measurement.getFromToken().getToken());
                            result.measurementsInRange = result.measurementValue >= min && result.measurementValue <= max;
                            return result;
                        }
                    case MEDIAN:
                        if (numberDimensions == 3) {
                            TokenRelationship measurement = measurements.get(1);
                            result.measurementValue = Double.parseDouble(measurement.getFromToken().getToken());
                            result.measurementsInRange = result.measurementValue >= min && result.measurementValue <= max;
                            return result;
                        }
                }
            }
        }
        return new SecondLargestProcessingResult(false);
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
