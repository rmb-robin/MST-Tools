package com.mst.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.mst.model.sentenceProcessing.WordToken;

public class GrammaticalPattern {
	private List<GrammaticalPatternEntity> entities = new ArrayList<>();
	private List<WordToken> verbPhraseOrphans = new ArrayList<>();
	private List<WordToken> otherOrphans = new ArrayList<>();
	
	public GrammaticalPattern() {
	
	}
	
	public GrammaticalPattern(List<GrammaticalPatternEntity> entities) {
		this.entities.addAll(new ArrayList<GrammaticalPatternEntity>(entities));		
	}
	
	public int getPatternTokenCount() {
		int count = 0;
		
		for(GrammaticalPatternEntity entity : entities) {
			count += entity.getTokenCount();
			System.out.println(entity.getEntity() + " " + entity.getTokenCount());
		}
		
		return count;
	}
	
	public List<GrammaticalPatternEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<GrammaticalPatternEntity> entities) {
		this.entities = entities;
	}

	public List<WordToken> getVerbPhraseOrphans() {
		return verbPhraseOrphans;
	}

	public void setVerbPhraseOrphans(List<WordToken> verbPhraseOrphans) {
		this.verbPhraseOrphans = verbPhraseOrphans;
	}

	public List<WordToken> getOtherOrphans() {
		return otherOrphans;
	}

	public void setOtherOrphans(List<WordToken> otherOrphans) {
		this.otherOrphans = otherOrphans;
	}
	
	@Override
	public String toString() {
		return Joiner.on(",").join(entities);
	}
}
