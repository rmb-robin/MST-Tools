package com.mst.model.autocomplete;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteRequest {

	private List<String> tokens; 
	
	public AutoCompleteRequest(){
		tokens = new ArrayList<>();
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}
	
	
	
	
}
