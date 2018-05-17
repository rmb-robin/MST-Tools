package com.mst.sentenceprocessing;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;

import static com.mst.model.metadataTypes.Descriptor.*;

public class SentenceMeasureNormalizerImpl implements SentenceMeasureNormalizer {

    public List<WordToken> Normalize(List<WordToken> wordTokens, boolean convertMeasurements) {
        try {
            tokenizeMeasurements(wordTokens);
            if (convertMeasurements)
                convertMMtoCM(wordTokens);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wordTokens;
    }

    private void tokenizeMeasurements(List<WordToken> words) {
        int uomIndex = 0;
        boolean measurementsTokenized = false;
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext()) {
            WordToken word = itr.next();
            if (Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                String[] dimensions = word.getToken().split("x");
                uomIndex = word.getPosition() + dimensions.length;
                if (dimensions.length > 1) {
                    itr.remove();
                    for (String dimension : dimensions) {
                        int index = Arrays.asList(dimensions).indexOf(dimension);
                        WordToken newWord = new WordToken();
                        newWord.setToken(dimension);
                        newWord.setSemanticType(SemanticTypes.cardinalNumber);
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
            } else if (measurementsTokenized && Constants.UNIT_OF_MEASURE_REGEX.matcher(word.getToken()).matches()) {
                word.setPosition(uomIndex);
            } else if (Constants.MEASUREMENT_1_DIMENSION_REGEX.matcher(word.getToken()).matches()) {
                String[] measurement = word.getToken().split("(mm|cm)+");
                String[] unit = word.getToken().split("\\.?\\d+");
                itr.remove();
                WordToken newWord = new WordToken();
                newWord.setToken(measurement[0]);
                newWord.setSemanticType(SemanticTypes.cardinalNumber);
                newWord.setPosition(word.getPosition());
                newWord.setDescriptor(X_AXIS);
                itr.add(newWord);
                newWord = new WordToken();
                newWord.setToken(unit[1]);
                newWord.setSemanticType("uom");
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

            if ((word.getSemanticType() != null && word.getSemanticType().equalsIgnoreCase(SemanticTypes.cardinalNumber)) || Constants.CARDINAL_NUMBER_REGEX.matcher(word.getToken()).matches()) {
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
