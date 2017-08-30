package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

public class DynamicEdgeCondition {

	private boolean isCondition1Token;
	private String token;
	private boolean isTokenSemanticType;
	private boolean isTokenPOSType;
	private List<String> edgeNames;
	private List<String> fromTokens;
	private boolean isFromTokenSemanticType;
	private boolean isFromTokenPOSType;
	private List<String> toTokens;
	private boolean isToTokenSemanticType;
	private boolean isToTokenPOSType;
	private boolean isEqualTo;
	
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


	public boolean getIsFromTokenSemanticType() {
		return isFromTokenSemanticType;
	}

	public void setIsFromTokenSemanticType(boolean isFromTokenSemanticType) {
		this.isFromTokenSemanticType = isFromTokenSemanticType;
	}

	public boolean getIsFromTokenPOSType() {
		return isFromTokenPOSType;
	}

	public void setIsFromTokenPOSType(boolean isFromTokenPOSType) {
		this.isFromTokenPOSType = isFromTokenPOSType;
	}


	public boolean getIsToTokenSemanticType() {
		return isToTokenSemanticType;
	}

	public void setIsToTokenSemanticType(boolean isToTokenSemanticType) {
		this.isToTokenSemanticType = isToTokenSemanticType;
	}

	public boolean getIsToTokenPOSType() {
		return isToTokenPOSType;
	}

	public void setIsToTokenPOSType(boolean isToTokenPOSType) {
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
