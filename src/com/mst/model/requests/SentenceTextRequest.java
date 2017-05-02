package com.mst.model.requests;


public class SentenceTextRequest extends SentenceRequestBase {

	private String text;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
