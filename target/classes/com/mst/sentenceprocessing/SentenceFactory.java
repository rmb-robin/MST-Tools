package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.SentenceToken;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;

public class SentenceFactory {
	
	private Tokenizer tokenizer;
	private SentenceCleaner cleaner;
	
	public SentenceFactory(){
		tokenizer = new Tokenizer();
		cleaner  = new SentenceCleaner();
	}
	
	public Sentence getSentence(String sentenceText,String study, String practice, String source){
		SentenceToken sentenceToken = tokenizer.splitSentencesNew(sentenceText).get(0);
		return createSentence(sentenceToken,study,practice,source,0);
	}
	
	public List<Sentence> getSentences(String text,String study,String practice, String source){
		List<SentenceToken> sentenceTokens = tokenizer.splitSentencesNew(text);
		List<Sentence> result = new ArrayList<Sentence>();
		 
		int position = 1;
		for(SentenceToken sentenceToken: sentenceTokens){
			Sentence sentence = createSentence(sentenceToken, study,practice, source, position);
			result.add(sentence);
			position +=1;
		}
		return result;
	}

	private Sentence createSentence(SentenceToken sentenceToken, String study, String practice, String source,int position){
		String cs = cleaner.cleanSentence(sentenceToken.getToken());
		Sentence sentence = new Sentence(null, position);
		List<String> words =  tokenizer.splitWordsInStrings(cs);
		sentence.setOriginalWords(words);
		
		sentence.setPractice(practice);
		sentence.setSource(source);
		sentence.setStudy(study);
		sentence.setNormalizedSentence(cs);
		sentence.setOrigSentence(sentenceToken.getToken());
		return sentence;
	}
}
