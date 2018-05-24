package com.mst.model;

import java.util.ArrayList;
import java.util.List;

import com.mst.util.Constants;

public class VerbPhraseMetadata {
	private Constants.VerbClass _class;
	private VerbPhraseToken subj;
	private List<VerbPhraseToken> subjs = new ArrayList<>();
	private List<VerbPhraseToken> verbs = new ArrayList<>();
	private List<VerbPhraseToken> subjc = new ArrayList<>();
	//private boolean compound;
	//private boolean intransitive = true;
	private boolean infFollowsPP; // infinitive verb follows prep phrase
	private String st = null;
	private String dpSignal = null; // if any part of phrase (subj, vb, obj) is within a dependent phrase this will represent the DP signal 

	public VerbPhraseMetadata() {	}
	
	public VerbPhraseMetadata(Constants.VerbClass _class) {
		this._class = _class;
	}

	public Constants.VerbClass getVerbClass() {
		return _class;
	}

	public void setVerbClass(Constants.VerbClass _class) {
		this._class = _class;
	}
	
	public VerbPhraseToken getSubj() {
		return subj;
	}

	public void setSubj(VerbPhraseToken subj) {
		this.subj = subj;
	}

	public List<VerbPhraseToken> getVerbs() {
		return verbs;
	}
	
	public void addVerb(VerbPhraseToken verb) {
		this.verbs.add(verb);
	}

	public List<VerbPhraseToken> getSubjC() {
		return subjc;
	}

	public void addSubjC(VerbPhraseToken subjc) {
		this.subjc.add(subjc);
	}

	public List<VerbPhraseToken> getSubjects() {
		return subjs;
	}

	public void addSubj(VerbPhraseToken subj) {
		this.subjs.add(subj);
	}
	
//	public boolean isCompound() {
//		return compound;
//	}
//
//	public void setIntransitive(boolean intransitive) {
//		this.intransitive = intransitive;
//	}
//
//	public boolean isIntransitive() {
//		return intransitive;
//	}
//
//	public void setCompound(boolean compound) {
//		this.compound = compound;
//	}
//	
	public void setInfFollowsPP(boolean infFollowsPP) {
		this.infFollowsPP = infFollowsPP;
	}

	public boolean isInfFollowsPP() {
		return infFollowsPP;
	}
	
	public void setDPSignal(String dpSignal) {
		this.dpSignal = dpSignal;
	}

	public String getDPSignal() {
		return dpSignal;
	}
	
	public boolean isWithinDP() {
		return dpSignal != null;
	}
	
	public String getSemanticType() {
		return st;
	}
	
	public void setSemanticType(String val) {
		st = val;
	}
	
	public String getVerbString() {
		StringBuilder vp = new StringBuilder();
		for(VerbPhraseToken token : verbs) {
			vp.append(token.getToken()).append(" ");
		}
		return vp.toString().trim().toLowerCase();
	}

	public boolean isPhraseNegated() {
		if(subj != null && subj.isNegated())
			return true;
		
		for(VerbPhraseToken subj : subjs) 
			if(subj.isNegated())
				return true;
		
		for(VerbPhraseToken verb : verbs) 
			if(verb.isNegated())
				return true;

		for(VerbPhraseToken obj : subjc) 
			if(obj.isNegated())
				return true;
		
		return false;
	}
	
	public boolean isVerbNegated() {
		
		for(VerbPhraseToken verb : verbs) 
			if(verb.isNegated())
				return true;
		
		return false;
	}
}

