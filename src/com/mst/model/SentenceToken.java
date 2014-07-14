package com.mst.model;

public class SentenceToken extends GenericToken {
	int begin = 0, end = 0;
	
	public SentenceToken(int begin, int end, String token, String normalizedForm, int position) {
		super(token, position);
		this.begin = begin;
		this.end = end;
	}
	
	public int getBegin() {
		return this.begin;
	}
	
	public int getEnd() {
		return end;
	}
}