package com.mst.model.gentwo;

import java.util.UUID;

public class ActionVerbItem {

	private UUID id; 
	private UUID InfinitivePresentId;
	private String verb;
	private VerbTense verbTense;
	
	
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