package com.mst.model.sentenceProcessing;

public class RelationshipMapping {
	
	private String fromToken;
	private String toToken;
	private String edgeName;
	private String namedEdgeName;
	private int maxDistance;
	
	private boolean isFromSemanticType;
	private boolean isToSemanticType;
	private boolean isFromWildcard;
	private boolean isToWildcard;
	

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

	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public boolean getIsFromSemanticType() {
		return isFromSemanticType;
	}
	public void setFromSemanticType(boolean isFromSemanticType) {
		this.isFromSemanticType = isFromSemanticType;
	}
	public boolean getIsToSemanticType() {
		return isToSemanticType;
	}
	public void setToSemanticType(boolean isToSemanticType) {
		this.isToSemanticType = isToSemanticType;
	}
	public boolean getIsFromWildcard() {
		return isFromWildcard;
	}
	public void setFromWildcard(boolean isFromWildcard) {
		this.isFromWildcard = isFromWildcard;
	}
	public boolean getIsToWildcard() {
		return isToWildcard;
	}
	public void setToWildcard(boolean isToWildcard) {
		this.isToWildcard = isToWildcard;
	}
	public String getNamedEdgeName() {
		return namedEdgeName;
	}
	public void setNamedEdgeName(String namedEdgeName) {
		this.namedEdgeName = namedEdgeName;
	}
	public int getMaxDistance() {
		return maxDistance;
	}
	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}
}
