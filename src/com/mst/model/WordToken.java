package com.mst.model;

import java.util.ArrayList;

public class WordToken extends GenericToken {
	
	private String pos = null;  // part of speech
	private String normalizedForm = null;
	private ArrayList<SemanticType> semanticTypeList = new ArrayList<SemanticType>();
	private boolean nounPhraseHead;
	private boolean nounPhraseModifier;
	private boolean prepPhraseMember;
	private boolean prepPhraseObject;

	public WordToken(String word, String normalizedForm, int position) {
		super(word, position);
		this.setNormalizedForm(normalizedForm);
	}
	
	public String getPOS() {
		return pos;
	}

	public void setPOS(String pos) {
		this.pos = pos;
	}
	
	public ArrayList<SemanticType> getSemanticTypeList() {
		return this.semanticTypeList;
	}
	
	public void setSemanticTypeList(ArrayList<SemanticType> semanticTypeList) {
		this.semanticTypeList = semanticTypeList;
	}
	
	public void setNounPhraseHead(boolean isNounPhraseHead) {
		this.nounPhraseHead = isNounPhraseHead;
	}
	
	public boolean nounPhraseHead() {
		return nounPhraseHead;
	}
	
	public void setNounPhraseModifier(boolean isNounPhraseModifier) {
		this.nounPhraseModifier = isNounPhraseModifier;
	}
	
	public boolean nounPhraseModifier() {
		return nounPhraseModifier;
	}
	
	public boolean isPrepPhraseMember() {
		return prepPhraseMember;
	}

	public void setPrepPhraseMember(boolean isPrepPhraseMember) {
		this.prepPhraseMember = isPrepPhraseMember;
	}
	
	public boolean isPrepPhraseObject() {
		return prepPhraseObject;
	}

	public void setPrepPhraseObject(boolean isPrepPhraseObject) {
		this.prepPhraseObject = isPrepPhraseObject;
	}
	
	public String getNormalizedForm() {
		return normalizedForm;
	}

	public void setNormalizedForm(String normalizedForm) {
		this.normalizedForm = normalizedForm;
	}
}