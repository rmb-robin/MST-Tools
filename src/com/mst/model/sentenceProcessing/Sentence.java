package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.discrete.DiscreteData;

import java.time.LocalDate;


public class Sentence {

	private int position;
	private long lineId;
	private String origSentence;
	private String normalizedSentence;
	private LocalDate processingDate;
	
	private List<String> originalWords;
	private List<WordToken> modifiedWordList = new ArrayList<>();
	private List<TokenRelationship> tokenRelationships = new ArrayList<>();
	private String id, source, practice, study;
	private String organizationId;

	private boolean didFail;
	private DiscreteData discreteData;
	
	
	public Sentence(String id, int position) {	
		this.id = id;
		this.position = position;
	}

	public Sentence(String fullSentence) {	
		this.origSentence = fullSentence;
		this.normalizedSentence = fullSentence;
	}
	
	public WordToken getTokenBySemanticType(String semanticType){
		if(this.modifiedWordList==null) return null;
		
		for(WordToken wordToken: this.modifiedWordList){
			if(wordToken.getSemanticType()==null) continue;
			if(wordToken.getSemanticType().equals(semanticType))
				return wordToken;
			
		}
		return null;
	}
	
	
	public List<TokenRelationship> getTokenRelationshipsByEdgeName(String edgeName){
		List<TokenRelationship> result = new ArrayList<>();
		if(this.tokenRelationships==null) return result;
		
		for(TokenRelationship tokenRelationship: this.tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(""))continue;
			if(tokenRelationship.getEdgeName().equals(edgeName))
				result.add(tokenRelationship);
		}
		return result;
	}
	
	public Map<String,List<TokenRelationship>> getTokenRelationsByNameMap(){
		HashMap<String,List<TokenRelationship>> result = new HashMap<>();
		if(this.tokenRelationships==null) return result;
		
		for(TokenRelationship tokenRelationship: this.tokenRelationships){
			if(!result.containsKey(tokenRelationship.getEdgeName()))
				result.put(tokenRelationship.getEdgeName(), new ArrayList<TokenRelationship>());
			result.get(tokenRelationship.getEdgeName()).add(tokenRelationship);
		}
		return result;
	}
	
	
	public boolean doesSentenceContainVerb(){
		for(WordToken token: modifiedWordList){
			if(token.isVerb()) return true;
		}
		return false;
	}
	
	public void addHasVerb(){
		WordToken hasToken = new WordToken();
		hasToken.setToken("has");
		hasToken.setVerb(new Verb());
		modifiedWordList.add(0, hasToken);
	}
	
	public void setProcessDate(){
		this.processingDate = LocalDate.now();
	}
	
	public LocalDate getProcessDate(){
		return processingDate;
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

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public boolean isDidFail() {
		return didFail;
	}

	public void setDidFail(boolean didFail) {
		this.didFail = didFail;
	}
}
