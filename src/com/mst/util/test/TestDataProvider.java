package com.mst.util.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.mst.model.Sentence;
import com.mst.model.SentenceToken;
import com.mst.tools.SentenceCleaner;
import com.mst.tools.Tokenizer;

public class TestDataProvider {

	public static String getFileText(String path){
		try {
			String contents = new String(Files.readAllBytes(Paths.get(path)));
			return contents;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	} 
	
	public static List<Sentence> getSentence(String text){
		Tokenizer t = new Tokenizer();
		SentenceCleaner cleaner = new SentenceCleaner();
		List<Sentence> sentences = new ArrayList<Sentence>();
		
		ArrayList<SentenceToken> sentenceTokens = t.splitSentencesNew(text);
		int position = 0;
		for(SentenceToken sentenceToken : sentenceTokens) {
			String cs = cleaner.cleanSentence(sentenceToken.getToken());
			Sentence sentence = new Sentence(null, position++);
			List<String> words =  t.splitWordsInStrings(cs);
			sentence.setOriginalWords(words);
			
			sentence.setPractice("pratice");
			sentence.setSource(null);
			sentence.setFullSentence(cs);
			sentence.setOrigSentence(sentenceToken.getToken());
			sentences.add(sentence);	
		}
		return sentences;
	}
	
}
