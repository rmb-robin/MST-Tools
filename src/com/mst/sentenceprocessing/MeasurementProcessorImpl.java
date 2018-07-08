package com.mst.sentenceprocessing;

import java.util.*;

import com.mst.interfaces.sentenceprocessing.MeasurementProcessor;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

import static com.mst.model.metadataTypes.Descriptor.*;
import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.MeasurementAnnotations.*;
import static com.mst.model.metadataTypes.PropertyValueTypes.NA;
import static com.mst.model.metadataTypes.SemanticTypes.*;
import static com.mst.util.Constants.*;

public class MeasurementProcessorImpl implements MeasurementProcessor {
    private Map<String, Annotation> annotationsFound;

    public List<TokenRelationship> process(List<WordToken> wordTokens, boolean convertMillimeter) {
        processAnnotations(wordTokens);
        addMeasurements(wordTokens);
        if (convertMillimeter)
            convertMMtoCM(wordTokens);
        return createTokenRelationships(wordTokens);
    }

    private class Annotation {
        public String value;
        public int position;

        Annotation(String value, int position) {
            this.value = value;
            this.position = position;
        }
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
            if (descriptor != null && (descriptor.equals(X_AXIS) || descriptor.equals(LENGTH) || descriptor.equals(SHORT_AXIS) || descriptor.equals(LONG_AXIS)))
                xAxis = word;
            else if (descriptor != null && (descriptor.equals(Y_AXIS) || descriptor.equals(TRANSVERSE) || descriptor.equals(SHORT_AXIS) || descriptor.equals(LONG_AXIS)))
                yAxis = word;
            else if (descriptor != null && (descriptor.equals(Z_AXIS) || descriptor.equals(AP) || descriptor.equals(SHORT_AXIS) || descriptor.equals(LONG_AXIS)))
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

    private void processAnnotations(List<WordToken> words) {
        annotationsFound = new LinkedHashMap<>();
        Map<String, List<String>> entries = getAnnotations();
        int index = 0;
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext()) {
            WordToken word = itr.next();
            while (index > 0) {
                word = itr.next();
                index--;
            }
            String token = word.getToken().toLowerCase();
            boolean match = false;
            for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
                if (match)
                    break;
                List<String> annotations = entry.getValue();
                for (String annotation : annotations) {
                    if (match)
                        break;
                    String[] annotationWords = annotation.split("\\s");
                    if (annotationWords.length > 1 && annotationWords[0].equals(token)) {
                        match = true;
                        while (index < annotationWords.length) {
                            if (itr.nextIndex() + index < words.size()) {
                                token = words.get(itr.nextIndex() + index).getToken();
                            }
                            index++;
                            if (!annotationWords[index].equals(token)) {
                                match = false;
                                break;
                            } else if (index == annotationWords.length - 1) {
                                annotationsFound.put(entry.getKey(), new Annotation(annotation, word.getPosition()));
                                break;
                            }
                        }
                    } else if (annotation.equals(token)) {
                        annotationsFound.put(entry.getKey(), new Annotation(annotation, word.getPosition()));
                        match = true;
                        break;
                    }
                }
            }
        }
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
        int measurementIndex = 0;
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext()) {
            WordToken word = itr.next();
            String descriptor;
            if (CARDINAL_NUMBER_REGEX.matcher(word.getToken()).matches() && itr.nextIndex() < words.size()) {
                WordToken nextWord = words.get(itr.nextIndex());
                if (UNIT_OF_MEASURE_REGEX.matcher(nextWord.getToken()).matches() || MEASUREMENT_DELIMITER_REGEX.matcher(nextWord.getToken()).matches()) {
                    word.setSemanticType(CARDINAL_NUMBER);
                    descriptor = getDescriptor(measurementIndex, word.getPosition());
                    word.setDescriptor(descriptor);
                    word.setPropertyValueType(NA);
                    word.setSubjectSetFromWildCard(false);
                    nextWord.setSemanticType(UNIT_OF_MEASURE);
                    nextWord.setPropertyValueType(NA);
                    nextWord.setSubjectSetFromWildCard(false);
                    measurementIndex++;
                }
            } else if (MULTIDIMENSIONAL_MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                String[] dimensions = word.getToken().split("x");
                uomIndex = word.getPosition() + dimensions.length;
                if (dimensions.length > 1) {
                    itr.remove();
                    int startingIndex = measurementIndex;
                    for (String dimension : dimensions) {
                        int index = startingIndex + Arrays.asList(dimensions).indexOf(dimension);
                        WordToken newWord = new WordToken();
                        newWord.setToken(dimension);
                        newWord.setSemanticType(CARDINAL_NUMBER);
                        newWord.setPosition(word.getPosition() + index);
                        descriptor = getDescriptor(index, word.getPosition() + index);
                        newWord.setDescriptor(descriptor);
                        itr.add(newWord);
                        measurementIndex++;
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
                descriptor = getDescriptor(measurementIndex, word.getPosition());
                newWord.setDescriptor(descriptor);
                itr.add(newWord);
                newWord = new WordToken();
                newWord.setToken(unit[1]);
                newWord.setSemanticType(UNIT_OF_MEASURE);
                newWord.setPosition(word.getPosition() + 1);
                itr.add(newWord);
                measurementIndex++;
            }
        }
    }

    private String getDescriptor(int measurementIndex, int measurementPosition) {
        if (annotationsFound.size() == 0)
            switch (measurementIndex) {
                case 0:
                    return X_AXIS;
                case 1:
                    return Y_AXIS;
                case 2:
                    return Z_AXIS;
            }
        else {
            Set<Map.Entry<String, Annotation>> mapSet = annotationsFound.entrySet();
            @SuppressWarnings(value = "unchecked")
            Map.Entry<String, Annotation> element = (Map.Entry<String, Annotation>) mapSet.toArray()[measurementIndex];
            int annotationPosition = element.getValue().position;
            if (annotationPosition < measurementPosition || annotationPosition - measurementPosition <= 2) {
                return element.getKey();
            } else if (annotationsFound.size() == 3)
                return element.getKey();
        }
        return null;
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
