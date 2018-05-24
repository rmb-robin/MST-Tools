package com.mst.metadataProviders;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PartOfSpeechSentenceExpectedResult {

	private Map<String, List<String>> posValues; 
	
	public Map<String, List<String>> getPosValues() {
		return posValues;
	}

	public void setPosValues(Map<String, List<String>> posValues) {
		this.posValues = posValues;
	}

	public PartOfSpeechSentenceExpectedResult(){
		posValues = new HashMap<>();
	}
	
}
