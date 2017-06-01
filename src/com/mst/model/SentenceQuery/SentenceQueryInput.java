package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private List<String> tokens; 
	private List<EdgeQuery> edges;
	
	public SentenceQueryInput(){
		tokens = new ArrayList<>();
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public List<EdgeQuery> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeQuery> edges) {
		this.edges = edges;
	}
}
