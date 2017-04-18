package com.mst.sentenceprocessing;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mst.interfaces.NgramsSentenceProcessor;
import com.mst.model.Sentence;
import com.mst.model.gentwo.NGramsModifierEntity;

public class NGramsSentenceProcessorImpl implements NgramsSentenceProcessor  {
	
	Tokenizer tokenizer = new Tokenizer();
	SentenceCleaner cleaner = new SentenceCleaner();
	
	public Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities) {
		String modifiedSentence = sentence.getOrigSentence();
		ngramsModifierEntities = sort(ngramsModifierEntities);
		for(NGramsModifierEntity entity : ngramsModifierEntities){
			modifiedSentence = modifiedSentence.toLowerCase().replaceAll(entity.getOriginalStatement(), entity.getModifiedStatement());
		}
		modifiedSentence = cleaner.cleanSentence(modifiedSentence);
		sentence.setModifiedWordList(tokenizer.splitWords(modifiedSentence));
		sentence.setFullSentence(modifiedSentence);
		return sentence;
	}
	
	//to do.. move to a more generic location..
	private  List<NGramsModifierEntity> sort( List<NGramsModifierEntity> ngramsModifierEntities){
		Comparator<NGramsModifierEntity> byValueSize = (e1, e2) -> Integer.compare(
	            e1.getOriginalStatement().length(), e2.getOriginalStatement().length());
		return ngramsModifierEntities.stream().sorted(byValueSize.reversed()).collect(Collectors.toList());					
	}
}
