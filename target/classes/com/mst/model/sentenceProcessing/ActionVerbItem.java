package com.mst.model.sentenceProcessing;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.UuidSerializer;
import com.mst.model.metadataTypes.VerbTense;

public class ActionVerbItem {

	private UUID id; 
	private UUID InfinitivePresentId;
	private String verb;
	private VerbTense verbTense;
	private String verbNetClass;
	private boolean isExistance; 
	private boolean isMaintainVerbNetClass;
	private boolean isNegation; 
	
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
	public boolean getIsExistance() {
		return isExistance;
	}
	public void setExistance(boolean isExistance) {
		this.isExistance = isExistance;
	}
	public boolean getIsMaintainVerbNetClass() {
		return isMaintainVerbNetClass;
	}
	public void setMaintainVerbNetClass(boolean isMaintainVerbNetClass) {
		this.isMaintainVerbNetClass = isMaintainVerbNetClass;
	}
	public boolean getIsNegation() {
		return isNegation;
	}
	
	public void setNegation(boolean isNegation) {
		this.isNegation = isNegation;
	}
}
