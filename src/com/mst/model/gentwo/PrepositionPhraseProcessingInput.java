package com.mst.model.gentwo;

import java.util.HashSet;

public class PrepositionPhraseProcessingInput {

	private HashSet<String> punctuations;
	private HashSet<String> stLookups;
	private int range;
	
	public PrepositionPhraseProcessingInput(){
		punctuations = new HashSet<>();
		stLookups = new HashSet<>();
		
		
		punctuations.add(";");
		punctuations.add(".");
		
		//should be inputs.
		stLookups = new HashSet<>();
		stLookups.add("dysn");
		stLookups.add("drugpr");
		stLookups.add("proc");
		stLookups.add("bpoc");
		range=7;
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
