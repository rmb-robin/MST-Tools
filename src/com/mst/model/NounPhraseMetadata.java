package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class NounPhraseMetadata {
	private List<GenericToken> phrase = new ArrayList<GenericToken>();
	private boolean negated;
	private boolean inPP;
	//private boolean modByPP;
	//private List<PrepPhraseMetadata> prepPhrases = new ArrayList<PrepPhraseMetadata>();
	private List<Integer> prepPhrasesIdx = new ArrayList<Integer>();
	
	public List<GenericToken> getPhrase() {	return phrase; }
	
	public void setPhrase(List<GenericToken> phrase) { this.phrase = phrase; }
	
	public boolean isNegated() { return negated; }
	
	public void setNegated(boolean negated) { this.negated = negated; }
	
//	public boolean isHeadEqPPObj() {
//		return headEqPPObj;
//	}
//	
//	public void setHeadEqPPObj(boolean val) {
//		this.headEqPPObj = val;
//	}
	
	public boolean isWithinPP() { return inPP; }
	
	public void setWithinPP(boolean val) { this.inPP = val; }
	
	//public boolean addPrepPhrase(PrepPhraseMetadata val) { return prepPhrases.add(val); }
	
	//public void setPrepPhrases(List<PrepPhraseMetadata> val) { this.prepPhrases = val; }
	
	//public List<PrepPhraseMetadata> getPrepPhrases() { return prepPhrases; }
	
	public boolean addPrepPhraseIdx(Integer val) { return prepPhrasesIdx.add(val); }
	
	public void setPrepPhrasesIdx(List<Integer> val) { this.prepPhrasesIdx = val; }
		
	public List<Integer> getPrepPhrasesIdx() { return prepPhrasesIdx; }	
}

