package com.mst.model;

import java.util.ArrayList;
import java.util.List;

import com.mst.util.Constants;

public class DependentPhraseMetadata {
	private Constants.DependentPhraseClass _class;
	private List<GenericToken> phrase = new ArrayList<GenericToken>();
	private List<Integer> prepPhrasesIdx = new ArrayList<Integer>();
	
	public DependentPhraseMetadata(Constants.DependentPhraseClass _class) {
		this._class = _class;
	}

	public Constants.DependentPhraseClass getDependentClass() {
		return _class;
	}
	
	public void setDependentClass(Constants.DependentPhraseClass _class) {
		this._class = _class;
	}
	
	public List<GenericToken> getPhrase() {
		return phrase;
	}
	
	public void setPhrase(List<GenericToken> phrase) {
		this.phrase = phrase;
	}
	
	public boolean addPrepPhraseIdx(Integer val) { return prepPhrasesIdx.add(val); }
	
	public void setPrepPhrasesIdx(List<Integer> val) { this.prepPhrasesIdx = val; }
		
	public List<Integer> getPrepPhrasesIdx() { return prepPhrasesIdx; }	
}
