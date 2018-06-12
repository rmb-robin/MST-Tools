package com.mst.sentenceprocessing;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mst.interfaces.sentenceprocessing.NgramsSentenceProcessor;
import com.mst.model.sentenceProcessing.NGramsModifierEntity;
import com.mst.model.sentenceProcessing.Sentence;

public class NGramsSentenceProcessorImpl implements NgramsSentenceProcessor {
    private Tokenizer tokenizer = new Tokenizer();
    private SentenceCleaner cleaner = new SentenceCleaner();

    public Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities) {
        String modifiedSentence = sentence.getOrigSentence();
        ngramsModifierEntities = sort(ngramsModifierEntities);
        for (NGramsModifierEntity entity : ngramsModifierEntities) {
            modifiedSentence = modifiedSentence.toLowerCase().replaceAll(entity.getOriginalStatement(), entity.getModifiedStatement());
        }
        modifiedSentence = cleaner.cleanSentence(modifiedSentence);
        sentence.setModifiedWordList(tokenizer.splitWords(modifiedSentence));
        sentence.setNormalizedSentence(modifiedSentence);
        return sentence;
    }

    private List<NGramsModifierEntity> sort(List<NGramsModifierEntity> ngramsModifierEntities) {
        Comparator<NGramsModifierEntity> byValueSize = Comparator.comparingInt(e -> e.getOriginalStatement().length());
        return ngramsModifierEntities.stream().sorted(byValueSize.reversed()).collect(Collectors.toList());
    }
}
