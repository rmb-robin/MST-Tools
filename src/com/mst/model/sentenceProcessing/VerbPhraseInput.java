package com.mst.model.sentenceProcessing;

 
import java.util.HashSet;
 

public class VerbPhraseInput {

	private HashSet<String> firstWordSubjects;
	private HashSet<String> stTypes;
	
	public VerbPhraseInput(){
		firstWordSubjects = new HashSet<>();
		stTypes = new HashSet<>();
	}
	
	public HashSet<String> getFirstWordSubjects() {
		return firstWordSubjects;
	}
	public void setFirstWordSubjects(HashSet<String> firstWordSubjects) {
		this.firstWordSubjects = firstWordSubjects;
	}
	public HashSet<String> getStTypes() {
		return stTypes;
	}
	public void setStTypes(HashSet<String> stTypes) {
		this.stTypes = stTypes;
	} 
	
	
	
}
