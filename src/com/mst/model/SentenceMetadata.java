package com.mst.model;

import java.util.List;

public class SentenceMetadata {
	private int nounPhraseCount;
	private int prepPhraseCount;
	private List<TokenPosition> nounPhrases;
	private List<TokenPosition> prepPhrases;
	private List<TokenPosition> nounPhraseInPrepPhrase;
	private List<TokenPosition> infVerbFollowsPrepPhrase;
	private List<TokenPosition> prepPhraseModifiesSUBJ;
	private boolean beginsWithPreposition;
	private List<TokenPosition> negationList;
	private List<VerbPhraseMetadata> verbMetadata;// = new List<VerbMetadata>(); 
	private List<TokenPosition> compoundVerbPhrases;
	
	public boolean containsPrepPhrase() {
		return prepPhraseCount > 0;
	}
	
	public boolean containsNounPhrase() {
		return nounPhraseCount > 0;
	}
		
	public boolean addNounPhraseInPrepPhrase(TokenPosition val) { return nounPhraseInPrepPhrase.add(val); }
	public boolean isNounPhraseInPrepPhrase() {	return !nounPhraseInPrepPhrase.isEmpty(); }
	
	public boolean addInfVerbFollowsPrepPhrase(TokenPosition val) { return infVerbFollowsPrepPhrase.add(val); }
	public boolean infVerbFollowsPrepPhrase() {	return !infVerbFollowsPrepPhrase.isEmpty(); }
	
	public boolean addVerbMetadata(VerbPhraseMetadata val) { return verbMetadata.add(val); }
	public List<VerbPhraseMetadata> getVerbMetadata() { return verbMetadata; }
	
	public void setCompoundVerbPhrases(List<TokenPosition> val) { compoundVerbPhrases = val; }
	public List<TokenPosition> getCompoundVerbPhrases() { return compoundVerbPhrases; }
	
	public boolean addPrepPhraseModifiesSUBJ(TokenPosition val) { return prepPhraseModifiesSUBJ.add(val); }
	public boolean prepPhraseModifiesSUBJ() { return !prepPhraseModifiesSUBJ.isEmpty(); }
	
	public void setBeginsWithPreposition(boolean val) { beginsWithPreposition = val; };
	public boolean beginsWithPreposition() { return beginsWithPreposition; };
	
	public void setNegationList(List<TokenPosition> val) { negationList = val; };
	public boolean hasNegation() { return !negationList.isEmpty(); };
}
