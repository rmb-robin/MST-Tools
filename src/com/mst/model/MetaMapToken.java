package com.mst.model;

import java.util.List;

public class MetaMapToken {

	//private int wordId;
	private String value, conceptID, conceptName, preferredName;
	private List<String> sources, semanticTypes;
	
	public MetaMapToken(String value, String conceptID, String conceptName, String preferredName, List<String> semanticTypes, List<String> sources) {
		//this.setWordId(wordId);
		this.setValue(value);
		this.setConceptID(conceptID);
		this.setConceptName(conceptName);
		this.setPreferredName(preferredName);
		this.setSemanticTypes(semanticTypes);
		this.setSources(sources);
	}

//	public int getWordId() {
//		return wordId;
//	}
//
//	public void setWordId(int wordId) {
//		this.wordId = wordId;
//	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getConceptID() {
		return conceptID;
	}

	public void setConceptID(String conceptID) {
		this.conceptID = conceptID;
	}

	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public List<String> getSources() {
		return sources;
	}

	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	public List<String> getSemanticTypes() {
		return semanticTypes;
	}

	public void setSemanticTypes(List<String> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}
}
