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
	private Date procedureDate;
	//private ArrayList<WordToken> wordList;
	
	private List<String> originalWords;
	private ArrayList<WordToken> modifiedWordList = new ArrayList<>();
	
	// a period is counted as token id.... leave them for now..might remove..
	private ArrayList<WordToken> nonPuncWordList = new ArrayList<>();
	private ArrayList<WordToken> puncOnlyWordList = new ArrayList<>();
	
	

	
	//is going away.. need to find code references...
	private SentenceMetadata metadata = new SentenceMetadata();
	private String id, source, practice, study;
	private HashMap<String, String> discrete = new HashMap<>();
	private List<ObjectId> structuredOIDs = new ArrayList<>();
	
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
	
	private int getEntityTokenCount(int start, int end) {
		int count = 0;
		
		// certain types of tokens do not get considered when tallying the token count
		
		for(int i=start; i < start+end; i++) {
			WordToken word = modifiedWordList.get(i); 
			if(!ignoreToken(word))
				count++;
		}
		
		return count;
	}
	
	public int getCuratedTokenCount() {
		return getEntityTokenCount(0, modifiedWordList.size());
	}
	
	private boolean ignoreToken(WordToken word) {
		return word.isPunctuation() || 
			   word.isDeterminerPOS() || 
			   word.isConjunctionPOS() || 
			   word.isNegationSignal();
	}
	
	public SentenceMetadata getMetadata() { return metadata; }
	
	public void setMetadata(SentenceMetadata val) { metadata = val; }
	
	public HashMap<String, String> getDiscrete() {
		return this.discrete;
	}
	
	public void setDiscrete(HashMap<String, String> map) {
		this.discrete = map;
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
	
	
	public void setNonPuncWordList(ArrayList<WordToken> nonPuncWordList) {
		this.nonPuncWordList = nonPuncWordList;
	}
	
	public ArrayList<WordToken> getNonPuncWordList() {
		return this.nonPuncWordList;
	}
	
	public void setPuncOnlyWordList(ArrayList<WordToken> puncOnlyWordList) {
		this.puncOnlyWordList = puncOnlyWordList;
	}
	
	public ArrayList<WordToken> getPuncOnlyWordList() {
		return this.puncOnlyWordList;
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
	
	public List<ObjectId> getStructuredOIDs() {
		return structuredOIDs;
	}
	
	public void setStructuredOIDs(List<ObjectId> structuredOIDs) {
		this.structuredOIDs = structuredOIDs;
	}
 
	public ArrayList<WordToken> getModifiedWordList() {
		return modifiedWordList;
	}

	public void setModifiedWordList(ArrayList<WordToken> modifiedWordList) {
		this.modifiedWordList = modifiedWordList;
	}

	public List<String> getOriginalWords() {
		return originalWords;
	}

	public void setOriginalWords(List<String> originalWords) {
		this.originalWords = originalWords;
	}
}
