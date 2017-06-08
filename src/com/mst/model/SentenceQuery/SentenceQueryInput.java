package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private List<SentenceQueryInstance> sentenceQueryInstances; 
	public SentenceQueryInput(){
		sentenceQueryInstances = new ArrayList<>();
	}
	
	public List<SentenceQueryInstance> getSentenceQueryInstances() {
		return sentenceQueryInstances;
	}
	public void setSentenceQueryInstances(List<SentenceQueryInstance> sentenceQueryInstances) {
		this.sentenceQueryInstances = sentenceQueryInstances;
	}
}
