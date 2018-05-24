package com.mst.model.test;

import java.util.List;

public class SentenceEdgeCompare {

	private String edgeName; 
	private int SentenceAOccurance;
	private int SentenceBOccurance;
	private List<String> sentenceAToFrom; 
	private List<String> sentenceBToFrom;
	
	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public int getSentenceAOccurance() {
		return SentenceAOccurance;
	}
	public void setSentenceAOccurance(int sentenceAOccurance) {
		SentenceAOccurance = sentenceAOccurance;
	}
	public int getSentenceBOccurance() {
		return SentenceBOccurance;
	}
	public void setSentenceBOccurance(int sentenceBOccurance) {
		SentenceBOccurance = sentenceBOccurance;
	}
	public List<String> getSentenceAToFrom() {
		return sentenceAToFrom;
	}
	public void setSentenceAToFrom(List<String> sentenceAToFrom) {
		this.sentenceAToFrom = sentenceAToFrom;
	}
	public List<String> getSentenceBToFrom() {
		return sentenceBToFrom;
	}
	public void setSentenceBToFrom(List<String> sentenceBToFrom) {
		this.sentenceBToFrom = sentenceBToFrom;
	} 
	
	
	
}
