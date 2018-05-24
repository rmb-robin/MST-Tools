package com.mst.model.sentenceProcessing;

import java.util.HashSet;

public class PrepositionPhraseProcessingInput {

	private HashSet<String> punctuations;
	private HashSet<String> stLookups;
	private int range;
	
	public PrepositionPhraseProcessingInput(){
		punctuations = new HashSet<>();
		stLookups = new HashSet<>();
	}

	public HashSet<String> getPunctuations() {
		return punctuations;
	}
	public void setPunctuations(HashSet<String> punctuations) {
		this.punctuations = punctuations;
	}
	public HashSet<String> getStLookups() {
		return stLookups;
	}
	public void setStLookups(HashSet<String> stLookups) {
		this.stLookups = stLookups;
	}
	public int getRange() {
		return range;
	}
	public void setRange(int range) {
		this.range = range;
	}
}
