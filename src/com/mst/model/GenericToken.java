package com.mst.model;

public class GenericToken {
	private String token;
	private int position; // position of the sentence/word within the paragraph/sentence
	
	public GenericToken() {	}
	
	public GenericToken(String token, int position) {
		this.setToken(token);
		this.setPosition(position);
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}