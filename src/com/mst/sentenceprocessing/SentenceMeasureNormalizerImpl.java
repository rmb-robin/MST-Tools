package com.mst.sentenceprocessing;

import java.util.List;
import java.util.ListIterator;

import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;

public class SentenceMeasureNormalizerImpl implements SentenceMeasureNormalizer {

    public List<WordToken> Normalize(List<WordToken> wordTokens, boolean convertMeasurements, boolean convertLargest) { //TODO remove last parameter
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
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext()) {
            WordToken word = itr.next();
            if (Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                String[] dimensions = word.getToken().split("x");
                if (dimensions.length > 1) {
                    itr.remove();
                    for (String dimension : dimensions) {
                        WordToken newWord = new WordToken();
                        newWord.setToken(dimension);
                        newWord.setSemanticType(word.getSemanticType());
                        itr.add(newWord);
                    }
                }
            }
            else if (Constants.NUMERIC_REGEX.matcher(word.getToken()).matches()) {
                String[] measurement = word.getToken().split("(mm|cm)+");
                String[] unit = word.getToken().split("\\.?\\d+");
                itr.remove();
                WordToken newWord = new WordToken();
                newWord.setToken(measurement[0]);
                itr.add(newWord);
                newWord = new WordToken();
                newWord.setToken(unit[1]);
                newWord.setSemanticType("uom");
                itr.add(newWord);
            }
        }
    }

    private void convertMMtoCM(List<WordToken> words) {
        //TODO check that next word after mm is NOT hg
        ListIterator<WordToken> itr = words.listIterator();
        while (itr.hasNext() && itr.nextIndex() < words.size() - 1) {
            WordToken word = itr.next();
            if ((word.getSemanticType() != null && word.getSemanticType().equalsIgnoreCase(SemanticTypes.cardinalNumber)) || Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches()) {
                WordToken nextWord = words.get(itr.nextIndex());
                WordToken secondWord;
                WordToken thirdWord;
                if (!nextWord.getToken().matches("(?i)(cm|centimeters)+")) {
                    if (nextWord.getToken().matches("(?i)(mm|millimeters)+")) {
                        word.setToken(String.valueOf(Float.parseFloat(word.getToken()) / 10));
                        nextWord.setToken("cm");
                    } else if (itr.nextIndex() + 1 < words.size()) {
                        secondWord = words.get(itr.nextIndex() + 1);
                        if (secondWord.getToken().matches("(?i)(mm|millimeters)+")) {
                            word.setToken(String.valueOf(Float.parseFloat(word.getToken()) / 10));
                            nextWord.setToken(String.valueOf(Float.parseFloat(nextWord.getToken()) / 10));
                            secondWord.setToken("cm");
                        }
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
