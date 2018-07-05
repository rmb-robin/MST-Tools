package com.mst.model;

import java.io.Serializable;

public class GenericToken implements Serializable {
	private String token = "";
	private int idx; // position of the sentence/word within the paragraph/sentence; 1-based
	
	public GenericToken() {	}
	
	public GenericToken(String token, int position) {
		this.setToken(token);
		this.setPosition(position);
	}
	
	public int getPosition() {
		return idx;
	}

	public void setPosition(int position) {
		this.idx = position;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}