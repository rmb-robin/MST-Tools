package com.mst.model.requests;

import java.util.ArrayList;
import java.util.List;

public class IcdTenRequest extends SentenceRequestBase { 

	private List<IcdTenSentenceInstance> sentenceInstances;
	
	public IcdTenRequest(){
		sentenceInstances = new ArrayList<>();
	}

	public List<IcdTenSentenceInstance> getSentenceInstances() {
		return sentenceInstances;
	}

	public void setSentenceInstances(List<IcdTenSentenceInstance> sentenceInstances) {
		this.sentenceInstances = sentenceInstances;
	}
	
	
	
	
	
}
