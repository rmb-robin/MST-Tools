package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class NounPhraseMetadata {
	private List<GenericToken> phrase = new ArrayList<GenericToken>();
	private boolean negated;
	private boolean inPP;
	private List<Integer> prepPhrasesIdx = new ArrayList<Integer>();
	private String st = null;
	
	public List<GenericToken> getPhrase() {	return phrase; }
	
	public void setPhrase(List<GenericToken> phrase) { this.phrase = phrase; }
	
	public boolean isNegated() { return negated; }
	
	public void setNegated(boolean negated) { this.negated = negated; }
	
	public boolean isWithinPP() { return inPP; }
	
	public void setWithinPP(boolean val) { this.inPP = val; }
	
	public boolean addPrepPhraseIdx(Integer val) { return prepPhrasesIdx.add(val); }
	
	public void setPrepPhrasesIdx(List<Integer> val) { this.prepPhrasesIdx = val; }
		
	public List<Integer> getPrepPhrasesIdx() { return prepPhrasesIdx; }
	
	public String getSemanticType() {return st;	}
	
	public void setSemanticType(String val) { st = val;	}
	
	public String getNounPhraseString() {
		StringBuilder np = new StringBuilder();
		for(GenericToken token : phrase) {
			np.append(token.getToken()).append(" ");
		}
		return np.toString().trim();
	}
}

