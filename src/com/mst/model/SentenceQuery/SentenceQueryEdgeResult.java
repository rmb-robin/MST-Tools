package com.mst.model.SentenceQuery;

public class SentenceQueryEdgeResult {

	private String edgeName; 
	private String fromToken;
	private String toToken;
	private String EdgeResultType; 
	
	private String matchedValue; 
	private String tokenType; 
	
	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public String getFromToken() {
		return fromToken;
	}
	public void setFromToken(String fromToken) {
		this.fromToken = fromToken;
	}
	public String getToToken() {
		return toToken;
	}
	public void setToToken(String toToken) {
		this.toToken = toToken;
	}
	public String getEdgeResultType() {
		return EdgeResultType;
	}
	public void setEdgeResultType(String edgeResultType) {
		EdgeResultType = edgeResultType;
	}
	public String getMatchedValue() {
		return matchedValue;
	}
	public void setMatchedValue(String matchedValue) {
		this.matchedValue = matchedValue;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	} 
	
	
}
