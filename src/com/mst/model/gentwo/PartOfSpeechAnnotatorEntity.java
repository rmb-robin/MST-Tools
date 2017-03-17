package com.mst.model.gentwo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PartOfSpeechAnnotatorEntity {

	private Map<String,HashSet<String>> annotators; 
	

	public PartOfSpeechAnnotatorEntity(){
		annotators = new HashMap<>();
	}
	
	public Map<String, HashSet<String>> getAnnotators() {
		return annotators;
	}

	public void setAnnotators(Map<String, HashSet<String>> annotators) {
		this.annotators = annotators;
	}

}
