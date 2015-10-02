package com.mst.model;

import java.util.ArrayList;
import java.util.Date;

public class Sentence {

	private int position;
	private String origSentence;
	private String normalizedSentence;
	private Date processDate;
	private Date procedureDate;
	private ArrayList<WordToken> wordList;
	//private ArrayList<StanfordDependency> stanfordDependencyList;
	private SentenceMetadata metadata = new SentenceMetadata();
	private String id, source, practice, study;
	
	public Sentence() {	
		this.processDate = new Date();
	}
	
	public Sentence(String id, int position, ArrayList<WordToken> wordList) {	
		this.id = id;
		this.position = position;
		this.wordList = wordList;
		this.processDate = new Date();
	}

	public Sentence(String fullSentence) {	
		this.origSentence = fullSentence;
		this.normalizedSentence = fullSentence;
		this.processDate = new Date();
	}

	public SentenceMetadata getMetadata() { return metadata; }
	
	public void setMetadata(SentenceMetadata val) { metadata = val; }
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
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
	
	public String getFullSentence() {
		return normalizedSentence;
	}
	
	public void setFullSentence(String sentence) {
		this.normalizedSentence = sentence;
	}
	
	public String getOrigSentence() {
		return origSentence;
	}
	
	public void setOrigSentence(String sentence) {
		this.origSentence = sentence;
	}
	
	public void setWordList(ArrayList<WordToken> wordList) {
		this.wordList = wordList;
	}
	
	public ArrayList<WordToken> getWordList() {
		return this.wordList;
	}
	
	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getProcessDate() {
		return this.processDate;
	}
	
	public void setProcedureDate(Date procedureDate) {
		this.procedureDate = procedureDate;
	}
	
	public Date getProcedureDate() {
		return this.procedureDate;
	}
}
