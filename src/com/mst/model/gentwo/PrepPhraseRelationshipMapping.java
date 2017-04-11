package com.mst.model.gentwo;

public class PrepPhraseRelationshipMapping {

	private String token; 
	private String previousToken;
	private String prepObjectToken; 
	private String edgeName;
	private boolean isTokenSemanticType;
	private boolean isPreviousTokenSemanticType;
	private boolean isPrepObjectTokenSemanticType;
	private boolean isTokenPOSType;
	private boolean isPreviousTokenPOSType;
	private boolean isPrepObjectTokenPOSType;
	
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getPreviousToken() {
		return previousToken;
	}
	public void setPreviousToken(String previousPrepToken) {
		this.previousToken = previousPrepToken;
	}
	public String getPrepObjectToken() {
		return prepObjectToken;
	}
	public void setPrepObjectToken(String prepObjectToken) {
		this.prepObjectToken = prepObjectToken;
	}
	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public boolean isTokenSemanticType() {
		return isTokenSemanticType;
	}
	public void setTokenSemanticType(boolean isTokenSemanticType) {
		this.isTokenSemanticType = isTokenSemanticType;
	}
	public boolean isPreviousTokenSemanticType() {
		return isPreviousTokenSemanticType;
	}
	public void setPreviousTokenSemanticType(boolean isPreviousTokenSemanticType) {
		this.isPreviousTokenSemanticType = isPreviousTokenSemanticType;
	}
	public boolean isPrepObjectTokenSemanticType() {
		return isPrepObjectTokenSemanticType;
	}
	public void setPrepObjectTokenSemanticType(boolean isPrepObjectTokenSemanticType) {
		this.isPrepObjectTokenSemanticType = isPrepObjectTokenSemanticType;
	}
	public boolean isTokenPOSType() {
		return isTokenPOSType;
	}
	public void setTokenPOSType(boolean isTokenPOSType) {
		this.isTokenPOSType = isTokenPOSType;
	}
	public boolean isPreviousTokenPOSType() {
		return isPreviousTokenPOSType;
	}
	public void setPreviousTokenPOSType(boolean isPreviousTokenPOSType) {
		this.isPreviousTokenPOSType = isPreviousTokenPOSType;
	}
	public boolean isPrepObjectTokenPOSType() {
		return isPrepObjectTokenPOSType;
	}
	public void setPrepObjectTokenPOSType(boolean isPrepObjectTokenPOSType) {
		this.isPrepObjectTokenPOSType = isPrepObjectTokenPOSType;
	}
	
	
	
	
}
