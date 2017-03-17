package com.mst.model.gentwo;

import com.mst.model.WordToken;

public class TokenRelationship {

	private String edgeName; 
	private String frameName; 
	
	//should reference Ids maybe...
	private WordToken toToken; 
	private WordToken fromToken;
	
	
	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	public WordToken getToToken() {
		return toToken;
	}
	public void setToToken(WordToken toToken) {
		this.toToken = toToken;
	}
	public WordToken getFromToken() {
		return fromToken;
	}
	public void setFromToken(WordToken fromToken) {
		this.fromToken = fromToken;
	} 
	
	
	
	
	
}
