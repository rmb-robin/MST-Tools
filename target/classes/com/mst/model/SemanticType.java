package com.mst.model;

// used by WordToken.java
public class SemanticType {

	private String semanticType;
	private String token;
	
	public SemanticType(String semanticType, String token) {
		this.semanticType = semanticType;
		this.token = token;
	}

	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getSemanticType() {
		return this.semanticType;
	}
	
	public String getToken() {
		return this.token;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof SemanticType){
	    	SemanticType in = (SemanticType) o;
	    	if(in.getSemanticType().equalsIgnoreCase(this.semanticType) && in.getToken().equalsIgnoreCase(this.token))
	    		return true;
	    }
	    return false;
	}
}
