package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class NounPhraseMetadata {
	private List<TokenPosition> phrase = new ArrayList<TokenPosition>();
	private boolean negated;
	private boolean headEqPPObj;
	private boolean modByPP;
	
	public List<TokenPosition> getPhrase() {
		return phrase;
	}
	
	public void setPhrase(List<TokenPosition> phrase) {
		this.phrase = phrase;
	}
	
	public boolean isNegated() {
		return negated;
	}
	
	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public boolean isHeadEqPPObj() {
		return headEqPPObj;
	}
	
	public void setHeadEqPPObj(boolean val) {
		this.headEqPPObj = val;
	}
	
	public boolean getModByPP() {
		return modByPP;
	}
	
	public void setModByPP(boolean val) {
		this.modByPP = val;
	}
}

