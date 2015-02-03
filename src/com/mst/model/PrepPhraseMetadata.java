package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class PrepPhraseMetadata {
	private List<TokenPosition> phrase = new ArrayList<TokenPosition>();
	private boolean negated;
	
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
	
}
