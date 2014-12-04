package com.mst.model;

public class TokenPosition {
	private String token;
	private int position;
	
	public TokenPosition(String token, int position) {
		this.token = token;
		this.position = position;
	}
	
	public String getToken() {
		return token;
	}
	
	public int getPosition() {
		return position;
	}
}
