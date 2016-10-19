package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class PrepPhraseMetadata {
	private List<PrepPhraseToken> phrase = new ArrayList<PrepPhraseToken>();
	private boolean negated;
	private String st = null;
	private boolean modifiesVerb;
	
	public List<PrepPhraseToken> getPhrase() {
		return phrase;
	}
	
	public void setPhrase(List<PrepPhraseToken> phrase) {
		this.phrase = phrase;
	}
	
	public boolean isNegated() {
		return negated;
	}
	
	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public boolean modifiesVerb() {
		return modifiesVerb;
	}
	
	public void setModifiesVerb(boolean modifiesVerb) {
		this.modifiesVerb = modifiesVerb;
	}
	
	public String getSemanticType() {return st;	}
	
	public void setSemanticType(String val) { st = val;	}
	
	public String getPrepPhraseString() {
		StringBuilder pp = new StringBuilder();
		for(GenericToken token : phrase) {
			pp.append(token.getToken()).append(" ");
		}
		return pp.toString().trim();
	}
}
