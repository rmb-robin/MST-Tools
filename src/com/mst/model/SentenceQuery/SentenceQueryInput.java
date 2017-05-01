package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private List<String> tokens; 
	private String edgeName;
	
	public SentenceQueryInput(){
		tokens = new ArrayList<>();
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public String getEdgeName() {
		return edgeName;
	}

	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	
	
	
	
	
}
