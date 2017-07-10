package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

public class SentenceProcessingResult {

	private List<Sentence> sentences; 
	private SentenceProcessingFailures failures; 
	
	public SentenceProcessingResult(){
		sentences = new ArrayList<>();
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public SentenceProcessingFailures getFailures() {
		return failures;
	}

	public void setFailures(SentenceProcessingFailures failures) {
		this.failures = failures;
	}
	
}
