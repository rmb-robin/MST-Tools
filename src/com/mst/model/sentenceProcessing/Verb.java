package com.mst.model.sentenceProcessing;

public class Verb {

	private VerbType verbType; 
	private VerbTense verbTense;
	private String verbState;
	private String verbNetClass;
	
	public String getVerbNetClass() {
		return verbNetClass;
	}
	public void setVerbNetClass(String verbNetClass) {
		this.verbNetClass = verbNetClass;
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
