package com.mst.model.sentenceProcessing;

import java.io.Serializable;

import com.mst.model.GenericToken;
import com.mst.model.metadataTypes.PropertyValueTypes;


public class WordToken extends GenericToken implements Serializable {
		
	private String semanticType = null;
	private String pos = null;  
	private Verb verb;
	private PropertyValueTypes propertyValueType;
	private boolean isSubjectSetFromWildCard;
	
	
	public WordToken() {
		super();
	}
	
	public WordToken(String word, int position) {
		super(word, position);
	}

	public boolean isVerb() {	
		return verb != null;
	}
		
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getSemanticType() {
		//if(s)
		return semanticType;
	}

	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
	}
	
	
	@Override
	public String toString() {
		return super.getToken();
	}

	public Verb getVerb() {
		return verb;
	}

	public void setVerb(Verb verb) {
		this.verb = verb;
	}

	public PropertyValueTypes getPropertyValueType() {
		if(propertyValueType==null) return PropertyValueTypes.NA;
		return propertyValueType;
	}

	public void setPropertyValueType(PropertyValueTypes propertyValueType) {
		this.propertyValueType = propertyValueType;
	}
	
	public boolean isSubjectSetFromWildCard() {
		return isSubjectSetFromWildCard;
	}

	public void setSubjectSetFromWildCard(boolean isSubjectSetFromWildCard) {
		this.isSubjectSetFromWildCard = isSubjectSetFromWildCard;
	}
}