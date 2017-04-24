package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.common.base.Joiner;
import com.mst.model.SentenceMetadata;

public class Sentence {

	private int position;
	private long lineId;
	private String origSentence;
	private String normalizedSentence;
	private Date processDate;
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private List<TokenRelationship> tokenRelationships = new ArrayList<>();
	private String id, source, practice, study;
	
	public Sentence() {	
		this.processDate = new Date();
	}
	
	public Sentence(String id, int position) {	
		this.id = id;
		this.position = position;
		this.processDate = new Date();
	}

	public Sentence(String fullSentence) {	
		this.origSentence = fullSentence;
		this.normalizedSentence = fullSentence;
		this.processDate = new Date();
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

	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getProcessDate() {
		return this.processDate;
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
}
