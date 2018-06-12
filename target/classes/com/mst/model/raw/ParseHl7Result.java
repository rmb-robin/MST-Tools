package com.mst.model.raw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.requests.SentenceTextRequest;

public class ParseHl7Result {
	
	private SentenceTextRequest sentenceTextRequest;
	private List<String> missingFields;
	private Map<String, String> allFields; 
	
	public ParseHl7Result(){
		missingFields = new ArrayList<>();	
		allFields = new HashMap<>();
	}
	
	public SentenceTextRequest getSentenceTextRequest() {
		return sentenceTextRequest;
	}
	public void setSentenceTextRequest(SentenceTextRequest sentenceTextRequest) {
		this.sentenceTextRequest = sentenceTextRequest;
	}
	public List<String> getMissingFields() {
		return missingFields;
	}
	public void setMissingFields(List<String> missingFields) {
		this.missingFields = missingFields;
	}

	public Map<String, String> getAllFields() {
		return allFields;
	}

	public void setAllFields(Map<String, String> allFields) {
		this.allFields = allFields;
	}
	
	
}
