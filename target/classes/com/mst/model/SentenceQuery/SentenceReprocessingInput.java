package com.mst.model.SentenceQuery;

public class SentenceReprocessingInput {

	private String token; 
	private String organizationId;
	private String reprocessId;
	private int takeSize;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
	public String getReprocessId() {
		return reprocessId;
	}
	public void setReprocessId(String reprocessId) {
		this.reprocessId = reprocessId;
	}
	public int getTakeSize() {
		return takeSize;
	}
	public void setTakeSize(int takeSize) {
		this.takeSize = takeSize;
	} 
	
	
}
