package com.mst.model.sentenceProcessing;

import java.util.UUID;

public class ActionVerbItem {

	private UUID id; 
	private UUID InfinitivePresentId;
	private String verb;
	private VerbTense verbTense;
	private String verbNetClass;
	
	public String getVerbNetClass() {
		return verbNetClass;
	}
	public void setVerbNetClass(String verbNetClass) {
		this.verbNetClass = verbNetClass;
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public UUID getInfinitivePresentId() {
		return InfinitivePresentId;
	}
	public void setInfinitivePresentId(UUID infinitivePresentId) {
		InfinitivePresentId = infinitivePresentId;
	}
	public String getVerb() {
		return verb;
	}
	public void setVerb(String verb) {
		this.verb = verb;
	}
	public VerbTense getVerbTense() {
		return verbTense;
	}
	public void setVerbTense(VerbTense verbTense) {
		this.verbTense = verbTense;
	}
	
}
