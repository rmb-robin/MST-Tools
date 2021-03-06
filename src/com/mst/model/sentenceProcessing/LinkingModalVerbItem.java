package com.mst.model.sentenceProcessing;

import com.mst.model.metadataTypes.VerbTense;
import com.mst.model.metadataTypes.VerbType;

public class LinkingModalVerbItem {
	private String token; 
	private VerbType verbType;
	private VerbTense verbTense; 
	private String verbState;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public VerbType getVerbType() {
		return verbType;
	}
	public void setVerbType(VerbType verbType) {
		this.verbType = verbType;
	}
	public VerbTense getVerbTense() {
		return verbTense;
	}
	public void setVerbTense(VerbTense verbTense) {
		this.verbTense = verbTense;
	}
	public String getVerbState() {
		return verbState;
	}
	public void setVerbState(String verbState) {
		this.verbState = verbState;
	}

}
