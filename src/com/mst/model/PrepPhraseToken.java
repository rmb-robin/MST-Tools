package com.mst.model;

import java.util.List;

public class PrepPhraseToken extends GenericToken {

	private String precedingToken, value;
	private List<String> comprisingTokens;
	
	public PrepPhraseToken(int begin, int end, 
						String word, String parent,
						String value, String precedingToken, List<String> comprisingTokens) {
		super(word, -1);
		
		this.setValue(value);
		this.setPrecedingToken(precedingToken);
		this.setComprisingTokens(comprisingTokens);
	}

	public String getPrecedingToken() {
		return precedingToken;
	}

	public void setPrecedingToken(String precedingToken) {
		this.precedingToken = precedingToken;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getComprisingTokens() {
		return comprisingTokens;
	}

	public void setComprisingTokens(List<String> comprisingTokens) {
		this.comprisingTokens = comprisingTokens;
	}
}
