package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInstance {
	private List<String> tokens; 
	private List<EdgeQuery> edges;
//	private List<EdgeQuery> exclusiveEdges; 
	private String appender; 
	private boolean isSt; 
	private boolean excludeTokenSequence; 

	public SentenceQueryInstance(){
		tokens = new ArrayList<>();
		edges = new ArrayList<>();
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

	public String getAppender() {
		return appender;
	}

	public void setAppender(String appender) {
		this.appender = appender;
	}

	public boolean getIsSt() {
		return isSt;
	}

	public void setIsSt(boolean isSt) {
		this.isSt = isSt;
	}

	public boolean isExcludeTokenSequence() {
		return excludeTokenSequence;
	}

	public void setExcludeTokenSequence(boolean excludeTokenSequence) {
		this.excludeTokenSequence = excludeTokenSequence;
	}



//	public List<EdgeQuery> getExclusiveEdges() {
//		return exclusiveEdges;
//	}
//
//	public void setExclusiveEdges(List<EdgeQuery> exclusiveEdges) {
//		this.exclusiveEdges = exclusiveEdges;
//	}
//	
}
