package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class VerbPhraseToken extends GenericToken {
	private boolean negated;
	//private boolean inDP; // within a dependent phrase
	//private List<PrepPhraseMetadata> prepPhrases = new ArrayList<PrepPhraseMetadata>();
	//private NounPhraseMetadata nounPhrase = new NounPhraseMetadata();
	private List<Integer> prepPhrasesIdx = new ArrayList<Integer>();
	private int nounPhraseIdx = -1;
	private int depPhraseIdx = -1;
	
	public VerbPhraseToken(String token, int position) {
		super(token, position);
	}
	
	public boolean isNegated() { return negated; }

	public void setNegated(boolean negated) { this.negated = negated; }
	
	//public boolean isWithinDependentPhrase() { return inDP;	}

	//public void setWithinDependentPhrase(boolean inDP) { this.inDP = inDP; }
	
	//public boolean addPrepPhrase(PrepPhraseMetadata val) { return prepPhrases.add(val); }
	
	//public void setPrepPhrases(List<PrepPhraseMetadata> val) { this.prepPhrases = val; }
	
	//public List<PrepPhraseMetadata> getPrepPhrases() { return prepPhrases; }
	
	//public void setNounPhrase(NounPhraseMetadata val) { this.nounPhrase = val; }
	
	//public NounPhraseMetadata getNounPhrase() { return nounPhrase; }
	
	public boolean addPrepPhraseIdx(Integer val) { return prepPhrasesIdx.add(val); }
	
	public void setPrepPhrasesIdx(List<Integer> val) { this.prepPhrasesIdx = val; }
		
	public List<Integer> getPrepPhrasesIdx() { return prepPhrasesIdx; }	
	
	public int getNounPhraseIdx() {	return nounPhraseIdx; }
	
	public void setNounPhraseIdx(int idx) {	nounPhraseIdx = idx; }
	
	public int getDepPhraseIdx() {	return depPhraseIdx; }
	
	public void setDepPhraseIdx(int idx) {	depPhraseIdx = idx; }
 }
