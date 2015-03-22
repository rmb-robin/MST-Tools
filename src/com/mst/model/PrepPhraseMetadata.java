package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class PrepPhraseMetadata {
	private List<PrepPhraseToken> phrase = new ArrayList<PrepPhraseToken>();
	private boolean negated;
	
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
	
}
