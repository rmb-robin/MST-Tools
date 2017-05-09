package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.DiscreteData;

import java.time.LocalDate;


public class Sentence {

	private int position;
	private long lineId;
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processDate;
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private List<TokenRelationship> tokenRelationships = new ArrayList<>();
	private String id, source, practice, study;

	private DiscreteData discreteData;
	
	
	public Sentence(String id, int position) {	
		this.id = id;
		this.position = position;
	}

	public Sentence(String fullSentence) {	
		this.origSentence = fullSentence;
		this.normalizedSentence = fullSentence;
	}
	
	
	public void setProcessDate(){
		this.processDate = LocalDate.now();
	}
	
	public LocalDate getProcessDate(){
		return processDate;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setLineId(long lineId) {
		this.lineId = lineId;
	}
	
	public long getLineId() {
		return lineId;
	}
	
	public void setPractice(String practice) {
		this.practice = practice;
	}
	
	public String getPractice() {
		return practice;
	}
	
	public void setStudy(String study) {
		this.study = study;
	}
	
	public String getStudy() {
		return study;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return source;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public String getNormalizedSentence() {
		return normalizedSentence;
	}
	
	public void setNormalizedSentence(String sentence) {
		this.normalizedSentence = sentence;
	}
	
	public String getOrigSentence() {
		return origSentence;
	}
	
	public void setOrigSentence(String sentence) {
		this.origSentence = sentence;
	}

	public List<WordToken> getModifiedWordList() {
		return modifiedWordList;
	}

	public void setModifiedWordList(List<WordToken> modifiedWordList) {
		this.modifiedWordList = modifiedWordList;
	}

	public List<String> getOriginalWords() {
		return originalWords;
	}

	public void setOriginalWords(List<String> originalWords) {
		this.originalWords = originalWords;
	}

	public List<TokenRelationship> getTokenRelationships() {
		return tokenRelationships;
	}

	public void setTokenRelationships(List<TokenRelationship> tokenRelationships) {
		this.tokenRelationships = tokenRelationships;
	}

	public DiscreteData getDiscreteData() {
		return discreteData;
	}

	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}
}
