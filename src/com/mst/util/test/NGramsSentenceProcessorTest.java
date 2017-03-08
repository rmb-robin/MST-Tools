package com.mst.util.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import com.mst.model.Sentence;
import com.mst.model.SentenceToken;
import com.mst.tools.NGramsSentenceProcessor;
import com.mst.tools.SentenceCleaner;
import com.mst.tools.Tokenizer;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

import static org.junit.Assert.*;

public class NGramsSentenceProcessorTest {



	
	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\sentences.txt";
	}
	private String getoutputPath(){
		return System.getProperty("user.dir") + "\\testoutput\\ngramTest.txt";
	}	
	
	
	@Test
	public void process() {
		List<String> output = new ArrayList<>();
		NGramsSentenceProcessor processor = new NGramsSentenceProcessor();
		String fileText = getFileText(getTestDataPath());
		List<Sentence> sentences = getSentence(fileText);
		
		for(Sentence sentence: sentences){
		Sentence ngramsProcessedSentence = processor.process(sentence);
		assertEquals(sentence,ngramsProcessedSentence);
	    output.add(sentence.getFullSentence() + "," + ngramsProcessedSentence.getFullSentence());
		}
		writeOutput(output);
	}
	
	private void writeOutput(List<String> output){
		Path out = Paths.get(getoutputPath());
		try {
			Files.write(out,output,Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private String getFileText(String path){
		try {
			String contents = new String(Files.readAllBytes(Paths.get(path)));
			return contents;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private List<Sentence> getSentence(String text){
		Tokenizer t = new Tokenizer();
		SentenceCleaner cleaner = new SentenceCleaner();
		List<Sentence> sentences = new ArrayList<Sentence>();
		
		ArrayList<SentenceToken> sentenceTokens = t.splitSentencesNew(text);
		int position = 0;
		for(SentenceToken sentenceToken : sentenceTokens) {
			String cs = cleaner.cleanSentence(sentenceToken.getToken());
			Sentence sentence = new Sentence(null, position++, t.splitWords(cs));
			
			sentence.setPractice("pratice");
			sentence.setSource(null);
			sentence.setFullSentence(cs);
			sentence.setOrigSentence(sentenceToken.getToken());
			sentences.add(sentence);	
		}
		return sentences;
	}
	
	
}
