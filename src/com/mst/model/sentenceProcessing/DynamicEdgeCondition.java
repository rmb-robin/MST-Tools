package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

public class DynamicEdgeCondition {

	private boolean isCondition1Token;
	private String token;
	private Boolean isTokenSemanticType;
	private Boolean isTokenPOSType;
	private List<String> edgeNames;
	private List<String> fromTokens;
	private Boolean isFromTokenSemanticType;
	private Boolean isFromTokenPOSType;
	private List<String> toTokens;
	private Boolean isToTokenSemanticType;
	private Boolean isToTokenPOSType;
	private Boolean isEqualTo;
	
	public DynamicEdgeCondition(){
		edgeNames = new ArrayList<>();
		fromTokens = new ArrayList<>();
		toTokens = new ArrayList<>();
	}

	public boolean isCondition1Token() {
		return isCondition1Token;
	}

	public void setCondition1Token(boolean isCondition1Token) {
		this.isCondition1Token = isCondition1Token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getIsTokenSemanticType() {
		return isTokenSemanticType;
	}

	public void setIsTokenSemanticType(Boolean isTokenSemanticType) {
		this.isTokenSemanticType = isTokenSemanticType;
	}

	public Boolean getIsTokenPOSType() {
		return isTokenPOSType;
	}

	public void setIsTokenPOSType(Boolean isTokenPOSType) {
		this.isTokenPOSType = isTokenPOSType;
	}

	public List<String> getEdgeNames() {
		return edgeNames;
	}

	public void setEdgeNames(List<String> edgeNames) {
		this.edgeNames = edgeNames;
	}


	public Boolean getIsFromTokenSemanticType() {
		return isFromTokenSemanticType;
	}

	public void setIsFromTokenSemanticType(Boolean isFromTokenSemanticType) {
		this.isFromTokenSemanticType = isFromTokenSemanticType;
	}

	public Boolean getIsFromTokenPOSType() {
		return isFromTokenPOSType;
	}

	public void setIsFromTokenPOSType(Boolean isFromTokenPOSType) {
		this.isFromTokenPOSType = isFromTokenPOSType;
	}


	public Boolean getIsToTokenSemanticType() {
		return isToTokenSemanticType;
	}

	public void setIsToTokenSemanticType(Boolean isToTokenSemanticType) {
		this.isToTokenSemanticType = isToTokenSemanticType;
	}

	public Boolean getIsToTokenPOSType() {
		return isToTokenPOSType;
	}

	public void setIsToTokenPOSType(Boolean isToTokenPOSType) {
		this.isToTokenPOSType = isToTokenPOSType;
	}

	public Boolean getIsEqualTo() {
		return isEqualTo;
	}

	public void setIsEqualTo(Boolean isEqualTo) {
		this.isEqualTo = isEqualTo;
	}

	public List<String> getFromTokens() {
		return fromTokens;
	}

	public void setFromTokens(List<String> fromTokens) {
		this.fromTokens = fromTokens;
	}

	public List<String> getToTokens() {
		return toTokens;
	}

	public void setToTokens(List<String> toTokens) {
		this.toTokens = toTokens;
	}


}
