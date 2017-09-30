package com.mst.model.SentenceQuery;

import java.util.HashMap;
import java.util.Map;

public class EdgeMatchOnQueryResult {
	private boolean isMatch; 
	private boolean didTokenRelationsContainAnyMatches;
	private Map<String,MatchInfo> matches;

	public EdgeMatchOnQueryResult(){
		matches = new HashMap<String, MatchInfo>();
	}

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public boolean isDidTokenRelationsContainAnyMatches() {
		return didTokenRelationsContainAnyMatches;
	}

	public void setDidTokenRelationsContainAnyMatches(boolean didTokenRelationsContainAnyMatches) {
		this.didTokenRelationsContainAnyMatches = didTokenRelationsContainAnyMatches;
	}

	public Map<String, MatchInfo> getMatches() {
		return matches;
	}

	public void setMatches(Map<String, MatchInfo> matches) {
		this.matches = matches;
	}
	
}
