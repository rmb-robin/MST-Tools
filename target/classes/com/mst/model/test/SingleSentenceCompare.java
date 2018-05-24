package com.mst.model.test;

import java.util.ArrayList;
import java.util.List;

public class SingleSentenceCompare {

	private String sentenceA; 
	private String sentenceB; 
	
	private List<SentenceEdgeCompare> misMatchedEdges;
	private List<SentenceEdgeCompare> matchingEdges; 
	
	
	public SingleSentenceCompare(){
		misMatchedEdges = new ArrayList<>();
		matchingEdges = new ArrayList<>();
		
	}
	public String getSentenceA() {
		return sentenceA;
	}

	public void setSentenceA(String sentenceA) {
		this.sentenceA = sentenceA;
	}

	public String getSentenceB() {
		return sentenceB;
	}

	public void setSentenceB(String sentenceB) {
		this.sentenceB = sentenceB;
	}

	public List<SentenceEdgeCompare> getDismatchedEdges() {
		return misMatchedEdges;
	}

	public void setDismatchedEdges(List<SentenceEdgeCompare> dismatchedEdges) {
		this.misMatchedEdges = dismatchedEdges;
	}
	public List<SentenceEdgeCompare> getMisMatchedEdges() {
		return misMatchedEdges;
	}
	public void setMisMatchedEdges(List<SentenceEdgeCompare> misMatchedEdges) {
		this.misMatchedEdges = misMatchedEdges;
	}
	public List<SentenceEdgeCompare> getMatchingEdges() {
		return matchingEdges;
	}
	public void setMatchingEdges(List<SentenceEdgeCompare> matchingEdges) {
		this.matchingEdges = matchingEdges;
	} 
	
	
}
 