package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private List<String> tokens; 
	private List<String> edgeNames;
	
	public SentenceQueryInput(){
		tokens = new ArrayList<>();
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public List<String> getEdgeNames() {
		return edgeNames;
	}

	public void setEdgeNames(List<String> edgeNames) {
		this.edgeNames = edgeNames;
	}
}
