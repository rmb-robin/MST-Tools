package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class OrphanMetadata {
	private GenericToken token = new GenericToken();
	private List<String> classification = new ArrayList<>();
	
	public OrphanMetadata() { }
	
	public OrphanMetadata(GenericToken gt) { 
		token = gt;
	}
	
	public GenericToken getToken() {
		return token;
	}
	
	public void setToken(GenericToken token) {
		this.token = token;
	}
	
	public List<String> getClassification() {
		return classification;
	}
	
	public boolean addClassification(String classification) {
		return this.classification.add(classification);
	}
}
