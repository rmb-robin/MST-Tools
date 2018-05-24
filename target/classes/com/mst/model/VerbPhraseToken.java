package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class VerbPhraseToken extends GenericToken {
	private boolean negated;
	private List<Integer> prepPhrasesIdx = new ArrayList<Integer>();
	private List<Integer> modifierIdx = new ArrayList<Integer>();
	private int nounPhraseIdx = -1;
	private int depPhraseIdx = -1;
	
	public VerbPhraseToken(String token, int position) {
		super(token, position);
	}
	
	public boolean hasModifiers() {
		return !modifierIdx.isEmpty();
	}
	
	public boolean isNegated() { return negated; }

	public void setNegated(boolean negated) { this.negated = negated; }
		
	public boolean addPrepPhraseIdx(Integer val) { return prepPhrasesIdx.add(val); }
	
	public void setPrepPhrasesIdx(List<Integer> val) { this.prepPhrasesIdx = val; }
		
	public List<Integer> getPrepPhrasesIdx() { return prepPhrasesIdx; }	
	
	public void setModifierIdx(List<Integer> val) { this.modifierIdx = val; }
	
	public List<Integer> getModifierList() { return modifierIdx; }
	
	public int getNounPhraseIdx() {	return nounPhraseIdx; }
	
	public void setNounPhraseIdx(int idx) {	nounPhraseIdx = idx; }
	
	public int getDepPhraseIdx() {	return depPhraseIdx; }
	
	public void setDepPhraseIdx(int idx) {	depPhraseIdx = idx; }
	
	@Override
	public String toString() {
		return this.getToken();
	}
 }
