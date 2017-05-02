package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryResult {

	private String sentence; 
	private String sentenceId;
	
	private List<SentenceQueryEdgeResult> sentenceQueryEdgeResults;
	
	public SentenceQueryResult(){
		sentenceQueryEdgeResults = new ArrayList<>();
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public List<SentenceQueryEdgeResult> getSentenceQueryEdgeResults() {
		return sentenceQueryEdgeResults;
	}

	public void setSentenceQueryEdgeResults(List<SentenceQueryEdgeResult> sentenceQueryEdgeResults) {
		this.sentenceQueryEdgeResults = sentenceQueryEdgeResults;
	}

	public String getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(String sentenceId) {
		this.sentenceId = sentenceId;
	}
	
}
