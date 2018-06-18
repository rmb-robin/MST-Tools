package com.mst.model.businessRule;

import java.util.List;

public class SecondLargestMeasurementProcessing extends BusinessRule {
    public enum IdentifierType {MEASUREMENT_ANNOTATION, MEASUREMENT_CLASSIFICATION}
    private String ruleName;
    private int numberDimensions;
    private List<String> axisAnnotations;
    private String secondLargestIdentifier;
    private IdentifierType identifierType;
    private List<String> largestBetweenAnnotations;

    public SecondLargestMeasurementProcessing() {
        super(SecondLargestMeasurementProcessing.class.getName());
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public int getNumberDimensions() {
        return numberDimensions;
    }

    public void setNumberDimensions(int numberDimensions) {
        this.numberDimensions = numberDimensions;
    }

    public List<String> getAxisAnnotations() {
        return axisAnnotations;
    }

    public void setAxisAnnotations(List<String> axisAnnotations) {
        this.axisAnnotations = axisAnnotations;
    }

    public String getSecondLargestIdentifier() {
        return secondLargestIdentifier;
    }

    public void setSecondLargestIdentifier(String secondLargestIdentifier) {
        this.secondLargestIdentifier = secondLargestIdentifier;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
    }

    public List<String> getLargestBetweenAnnotations() {
        return largestBetweenAnnotations;
    }

    public void setLargestBetweenAnnotations(List<String> largestBetweenAnnotations) {
        this.largestBetweenAnnotations = largestBetweenAnnotations;
    }
}
