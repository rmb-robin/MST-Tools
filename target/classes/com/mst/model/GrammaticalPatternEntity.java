package com.mst.model;

public class GrammaticalPatternEntity {
	private String entity;
	private int tokenCount;
	
	public GrammaticalPatternEntity() { }
	
	public GrammaticalPatternEntity(String entity, int tokenCount) {
		this.entity = entity;
		this.tokenCount = tokenCount;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public int getTokenCount() {
		return tokenCount;
	}
	
	public void setTokenCount(int tokenCount) {
		this.tokenCount = tokenCount;
	}
	
	@Override
	public String toString() {
		return getEntity();
	}
}
