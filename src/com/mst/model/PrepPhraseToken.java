package com.mst.model;

public class PrepPhraseToken extends GenericToken {

	private int nounPhraseIdx = -1;
	private boolean isObject;
	
	public PrepPhraseToken(String token, int position) {
		this(token, position, -1, false);
	}
	
	public PrepPhraseToken(String token, int position, int nounPhraseIdx) {
		this(token, position, nounPhraseIdx, false);
	}
	
	public PrepPhraseToken(String token, int position, boolean isObject) {
		this(token, position, -1, isObject);
	}
	
	public PrepPhraseToken(String token, int position, int nounPhraseIdx, boolean isObject) {
		super(token, position);
		this.nounPhraseIdx = nounPhraseIdx;
		this.isObject = isObject;
	}
	
	public int getNounPhraseIdx() {
		return nounPhraseIdx;
	}
	
	public void setNounPhraseIdx(int val) {
		nounPhraseIdx = val;
	}
	
	public boolean isObject() {
		return isObject;
	}
	
	public void isObject(boolean isObject) {
		this.isObject = isObject;
	}
}
