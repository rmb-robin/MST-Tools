package com.mst.model.sentenceProcessing;

import com.mst.model.metadataTypes.VerbTense;
import com.mst.model.metadataTypes.VerbType;

public class Verb {

	private VerbType verbType; 
	private VerbTense verbTense;
	private String verbState;
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
	public boolean isExistance() {
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
