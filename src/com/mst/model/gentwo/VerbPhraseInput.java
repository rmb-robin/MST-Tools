package com.mst.model.gentwo;

 
import java.util.HashSet;
 

public class VerbPhraseInput {

	private HashSet<String> firstWordSubjects;
	private HashSet<String> stTypes;
	
	public VerbPhraseInput(){
		firstWordSubjects = new HashSet<>();
		stTypes = new HashSet<>();
		
		firstWordSubjects.add("there");
		firstWordSubjects.add("this");
		firstWordSubjects.add("it");
		
		stTypes.add("dysn");
		stTypes.add("dysn"); //fix
		stTypes.add("drugpr");		
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
