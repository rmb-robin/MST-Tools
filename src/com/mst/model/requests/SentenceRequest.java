package com.mst.model.requests;

import java.util.List;

public class SentenceRequest extends SentenceRequestBase {
	private List<String> senteceTexts;

	public List<String> getSenteceTexts() {
		return senteceTexts;
	}

	public void setSenteceTexts(List<String> senteceTexts) {
		this.senteceTexts = senteceTexts;
	} 
	
	
}
