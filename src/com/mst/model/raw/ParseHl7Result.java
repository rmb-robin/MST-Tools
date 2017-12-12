package com.mst.model.raw;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.requests.SentenceTextRequest;

public class ParseHl7Result {
	
	private SentenceTextRequest sentenceTextRequest;
	private List<String> missingFields;
	
	
	public ParseHl7Result(){
		missingFields = new ArrayList<>();	
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
	
	
}
