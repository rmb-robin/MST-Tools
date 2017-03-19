package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.interfaces.NgramsSentenceProcessor;
import com.mst.model.Sentence;
import com.mst.model.gentwo.NGramsModifierEntity;

public class NGramsSentenceProcessorImpl implements NgramsSentenceProcessor  {
	
	Tokenizer tokenizer = new Tokenizer();
	SentenceCleaner cleaner = new SentenceCleaner();
	
	public Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities) {
		String modifiedSentence = sentence.getOrigSentence();
		for(NGramsModifierEntity entity : ngramsModifierEntities){
			modifiedSentence = modifiedSentence.replaceAll(entity.getOriginalStatement(), entity.getModifiedStatement());
		}
		modifiedSentence = cleaner.cleanSentence(modifiedSentence);
		sentence.setModifiedWordList(tokenizer.splitWords(modifiedSentence));
		sentence.setFullSentence(modifiedSentence);
		return sentence;
	}
}
