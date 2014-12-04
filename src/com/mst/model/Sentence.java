package com.mst.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Sentence {

	private int position;
	private String fullSentence;
	private Date processDate;
	private Date procedureDate;
	private ArrayList<WordToken> wordList;
	private ArrayList<MetaMapToken> metaMapList;
	private ArrayList<StanfordDependency> stanfordDependencyList;
	private Map<String, Object> metadata = new HashMap<String, Object>();
	private SentenceMetadata sMetadata = new SentenceMetadata();
	private String id, source, clientId;
	
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
		this.fullSentence = fullSentence;
		this.processDate = new Date();
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}
	
	public boolean addMetadata(String key, Object value) {
		boolean ret = true;
		try {
			metadata.put(key, value);
		} catch(Exception e) {
			ret = false;
		}
		return ret;
	}
	
	public SentenceMetadata getSentenceMetadata() { return sMetadata; }
	public void setSentenceMetadata(SentenceMetadata val) { sMetadata = val; }
	
	public ArrayList<StanfordDependency> getStanfordDependencies() {
		return stanfordDependencyList;
	}

	public void setStanfordDependencies(ArrayList<StanfordDependency> stanfordDependencies) {
		this.stanfordDependencyList = stanfordDependencies;
	}
	
	public void setMetaMapList(ArrayList<MetaMapToken> metaMapList) {
		this.metaMapList = metaMapList;
	}
	
	public ArrayList<MetaMapToken> getMetaMapList() {
		return this.metaMapList;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientId() {
		return clientId;
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
		return fullSentence;
	}
	
	public void setFullSentence(String fullSentence) {
		this.fullSentence = fullSentence;
	}
	
	public void setWordList(ArrayList<WordToken> wordList) {
		this.wordList = wordList;
	}
	
	public ArrayList<WordToken> getWordList() {
		return this.wordList;
	}
	
	public void setDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getDate() {
		return this.processDate;
	}
	
	public void setProcedureDate(Date procedureDate) {
		this.procedureDate = procedureDate;
	}
	
	public Date getProcedureDate() {
		return this.procedureDate;
	}
}
