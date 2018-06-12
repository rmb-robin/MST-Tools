package com.mst.model.SentenceQuery;

import com.mst.model.sentenceProcessing.TokenRelationship;

public class ShouldMatchOnSentenceEdgesResult {
	private boolean isMatch; 
	private TokenRelationship relationship;
	
	public boolean isMatch() {
		return isMatch;
	}
	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}
	public TokenRelationship getRelationship() {
		return relationship;
	}
	public void setRelationship(TokenRelationship relationship) {
		this.relationship = relationship;
	}
}
