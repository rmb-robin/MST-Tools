package com.mst.model.requests;

import java.util.ArrayList;
import java.util.List;

public class SentenceRequest extends SentenceRequestBase {
	private List<String> senteceTexts;

	public SentenceRequest(){
		senteceTexts = new ArrayList<>();
	}
	
	public List<String> getSenteceTexts() {
		return senteceTexts;
	}

	public void setSenteceTexts(List<String> senteceTexts) {
		this.senteceTexts = senteceTexts;
	} 
	
	
}
