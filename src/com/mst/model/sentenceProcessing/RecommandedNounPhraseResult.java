package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.recommandation.RecommandedTokenRelationship;

public class RecommandedNounPhraseResult {

	private List<RecommandedTokenRelationship> recommandedTokenRelationships;
	private Map<Integer, Integer> nounPhraseIndexes; 
	
	public RecommandedNounPhraseResult(){
		recommandedTokenRelationships = new ArrayList<>();
		nounPhraseIndexes = new HashMap<>();
	}

	public List<RecommandedTokenRelationship> getRecommandedTokenRelationships() {
		return recommandedTokenRelationships;
	}

	public void setRecommandedTokenRelationships(List<RecommandedTokenRelationship> recommandedTokenRelationships) {
		this.recommandedTokenRelationships = recommandedTokenRelationships;
	}

	public Map<Integer, Integer> getNounPhraseIndexes() {
		return nounPhraseIndexes;
	}

	public void setNounPhraseIndexes(Map<Integer, Integer> nounPhraseIndexes) {
		this.nounPhraseIndexes = nounPhraseIndexes;
	}
}
