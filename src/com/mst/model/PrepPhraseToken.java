package com.mst.model;

public class PrepPhraseToken extends GenericToken {

	private int nounPhraseIdx = -1;
	
	public PrepPhraseToken(String token, int position) {
		super(token, position);
	}
	
	public PrepPhraseToken(String token, int position, int nounPhraseIdx) {
		this(token, position);
		this.nounPhraseIdx = nounPhraseIdx;
	}
	
	public int getNounPhraseIdx() {
		return nounPhraseIdx;
	}
	
	public void setNounPhraseIdx(int val) {
		nounPhraseIdx = val;
	}
	
	// This class was initially used to mimic Eric's code. The only method that used it
	// has been deprecated.
	/*
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
	*/
}
