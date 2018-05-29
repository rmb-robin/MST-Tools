package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.mst.interfaces.sentenceprocessing.MeasurementProcessor;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

import static com.mst.model.metadataTypes.Descriptor.*;
import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.PropertyValueTypes.NA;
import static com.mst.model.metadataTypes.SemanticTypes.*;
import static com.mst.util.Constants.*;

public class MeasurementProcessorImpl implements MeasurementProcessor {

    public List<TokenRelationship> process(List<WordToken> wordTokens, boolean convertMillimeter) {
        addMeasurements(wordTokens);
        if (convertMillimeter)
            convertMMtoCM(wordTokens);
        return createTokenRelationships(wordTokens);
    }

    private List<TokenRelationship> createTokenRelationships(List<WordToken> words) {
        List<TokenRelationship> newRelationships = new ArrayList<>();
        WordToken xAxis = null;
        WordToken yAxis = null;
        WordToken zAxis = null;
        WordToken uom = null;
        for (WordToken word : words) {
            String semanticType = word.getSemanticType();
            String descriptor = word.getDescriptor();
            if (descriptor != null && descriptor.equals(X_AXIS))
                xAxis = word;
            else if (descriptor != null && descriptor.equals(Y_AXIS))
                yAxis = word;
            else if (descriptor != null && descriptor.equals(Z_AXIS))
                zAxis = word;
            else if (semanticType != null && semanticType.equals(UNIT_OF_MEASURE))
                uom = word;
        }
        if (xAxis != null && uom != null)
            addRelationships(xAxis, uom, newRelationships);
        if (yAxis != null && uom != null)
            addRelationships(yAxis, uom, newRelationships);
        if (zAxis != null && uom != null)
            addRelationships(zAxis, uom, newRelationships);
        return newRelationships;
    }

    private void addRelationships(WordToken axis, WordToken uom, List<TokenRelationship> relationships) {
        TokenRelationship relationship = new TokenRelationship();
        relationship.setEdgeName(measurement);
        relationship.setDescriptor(axis.getDescriptor());
        relationship.setSource(MeasurementProcessor.class.getName());
        relationship.setFromToken(axis);
        relationship.setToToken(uom);
        relationships.add(relationship);
        relationship = new TokenRelationship();
        relationship.setEdgeName(unitOfMeasure);
        relationship.setDescriptor(axis.getDescriptor());
        relationship.setSource(MeasurementProcessor.class.getName());
        relationship.setFromToken(axis);
        relationship.setToToken(uom);
        relationships.add(relationship);
    }

    private void addMeasurements(List<WordToken> words) {
        int uomIndex = 0;
        boolean measurementsTokenized = false;
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext()) {
            WordToken word = itr.next();
            if (CARDINAL_NUMBER_REGEX.matcher(word.getToken()).matches() && itr.nextIndex() < words.size() - 1) {
                WordToken nextWord = words.get(itr.nextIndex());
                if (UNIT_OF_MEASURE_REGEX.matcher(nextWord.getToken()).matches()) {
                    word.setSemanticType(CARDINAL_NUMBER);
                    word.setDescriptor(X_AXIS);
                    word.setPropertyValueType(NA);
                    word.setSubjectSetFromWildCard(false);
                    nextWord.setSemanticType(UNIT_OF_MEASURE);
                    nextWord.setPropertyValueType(NA);
                    nextWord.setSubjectSetFromWildCard(false);
                }
            } else if (MULTIDIMENSIONAL_MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                String[] dimensions = word.getToken().split("x");
                uomIndex = word.getPosition() + dimensions.length;
                if (dimensions.length > 1) {
                    itr.remove();
                    for (String dimension : dimensions) {
                        int index = Arrays.asList(dimensions).indexOf(dimension);
                        WordToken newWord = new WordToken();
                        newWord.setToken(dimension);
                        newWord.setSemanticType(CARDINAL_NUMBER);
                        newWord.setPosition(word.getPosition() + index);
                        switch (index) {
                            case 0:
                                newWord.setDescriptor(X_AXIS);
                                break;
                            case 1:
                                newWord.setDescriptor(Y_AXIS);
                                break;
                            case 2:
                                newWord.setDescriptor(Z_AXIS);
                        }
                        itr.add(newWord);
                    }
                }
                measurementsTokenized = true;
            } else if (measurementsTokenized && UNIT_OF_MEASURE_REGEX.matcher(word.getToken()).matches()) {
                word.setPosition(uomIndex);
            } else if (SINGLE_DIMENSION_MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                String[] measurement = word.getToken().split("(mm|cm)+");
                String[] unit = word.getToken().split("\\.?\\d+");
                itr.remove();
                WordToken newWord = new WordToken();
                newWord.setToken(measurement[0]);
                newWord.setSemanticType(CARDINAL_NUMBER);
                newWord.setPosition(word.getPosition());
                newWord.setDescriptor(X_AXIS);
                itr.add(newWord);
                newWord = new WordToken();
                newWord.setToken(unit[1]);
                newWord.setSemanticType(UNIT_OF_MEASURE);
                newWord.setPosition(word.getPosition() + 1);
                itr.add(newWord);
            }
        }
    }

    private void convertMMtoCM(List<WordToken> words) {
        //TODO check that next word after mm is NOT hg
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext() && itr.nextIndex() < words.size() - 1) {
            WordToken word = itr.next();
            if ((word.getSemanticType() != null && word.getSemanticType().equalsIgnoreCase(CARDINAL_NUMBER)) || CARDINAL_NUMBER_REGEX.matcher(word.getToken()).matches()) {
                WordToken nextWord = words.get(itr.nextIndex());
                WordToken secondWord;
                WordToken thirdWord;
                if (nextWord.getToken().matches("(?i)(mm|millimeters)+")) {
                    word.setToken(String.valueOf(Float.parseFloat(word.getToken()) / 10));
                    nextWord.setToken("cm");
                } else if (itr.nextIndex() + 1 < words.size()) {
                    secondWord = words.get(itr.nextIndex() + 1);
                    if (secondWord.getToken().matches("(?i)(mm|millimeters)+")) {
                        word.setToken(String.valueOf(Float.parseFloat(word.getToken()) / 10));
                        nextWord.setToken(String.valueOf(Float.parseFloat(nextWord.getToken()) / 10));
                        secondWord.setToken("cm");
                    } else if (itr.nextIndex() + 2 < words.size()) {
                        secondWord = words.get(itr.nextIndex() + 1);
                        thirdWord = words.get(itr.nextIndex() + 2);
                        if (thirdWord.getToken().matches("(?i)(mm|millimeters)+")) {
                            word.setToken(String.valueOf(Float.parseFloat(word.getToken()) / 10));
                            nextWord.setToken(String.valueOf(Float.parseFloat(nextWord.getToken()) / 10));
                            secondWord.setToken(String.valueOf(Float.parseFloat(secondWord.getToken()) / 10));
                            thirdWord.setToken("cm");
                        }
                    }
                }
            }
        }
    }
}
