package com.mst.model;

import com.mst.util.Constants;

public class VerbPhraseMetadata {
	private Constants.VerbClass _class;
	private TokenPositionVerbPhrase subj;
	private TokenPosition verb;
	private TokenPositionVerbPhrase subjc;
	private boolean compound;
	private boolean intransitive = true;
	//private boolean subjNegated;
	//private boolean subjcNegated;
	//private TokenPosition subjModByPP; // TPos value to = PP OBJ
	//private TokenPosition subjcModByPP; // TPos value to = PP OBJ
	//private boolean subjEqNPHead;
	//private boolean subjcEqNPHead;
	private boolean infFollowsPP;

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
	
	public TokenPositionVerbPhrase getSubj() {
		return subj;
	}

	public void setSubj(TokenPositionVerbPhrase subj) {
		this.subj = subj;
	}

	public TokenPosition getVerb() {
		return verb;
	}

	public void setVerb(TokenPosition verb) {
		this.verb = verb;
	}

	public TokenPositionVerbPhrase getSubjC() {
		return subjc;
	}

	public void setSubjC(TokenPositionVerbPhrase subjc) {
		this.subjc = subjc;
	}

	public boolean isCompound() {
		return compound;
	}

	public void setIntransitive(boolean intransitive) {
		this.intransitive = intransitive;
	}

	public boolean isIntransitive() {
		return intransitive;
	}

	public void setCompound(boolean compound) {
		this.compound = compound;
	}
	
//	public boolean isSubjectNegated() {
//		return subjNegated;
//	}
//
//	public void setSubjectNegated(boolean negated) {
//		this.subjNegated = negated;
//	}
//
//	public boolean isSubjectComplementNegated() {
//		return subjcNegated;
//	}
//
//	public void setSubjectComplementNegated(boolean negated) {
//		this.subjcNegated = negated;
//	}
//	
//	public TokenPosition getSubjModByPP() {
//		return subjModByPP;
//	}
//
//	public void setSubjModByPP(TokenPosition subjModByPP) {
//		this.subjModByPP = subjModByPP;
//	}
//
//	public TokenPosition getSubjCModByPP() {
//		return subjcModByPP;
//	}
//
//	public void setSubjCModByPP(TokenPosition subjcModByPP) {
//		this.subjcModByPP = subjcModByPP;
//	}
//
//	public boolean isSubjEqNPHead() {
//		return subjEqNPHead;
//	}
//
//	public void setSubjEqNPHead(boolean subjEqNPHead) {
//		this.subjEqNPHead = subjEqNPHead;
//	}
//
//	public boolean isSubjCEqNPHead() {
//		return subjcEqNPHead;
//	}
//
//	public void setSubjCEqNPHead(boolean subjcEqNPHead) {
//		this.subjcEqNPHead = subjcEqNPHead;
//	}
	
	public void setInfFollowsPP(boolean infFollowsPP) {
		this.infFollowsPP = infFollowsPP;
	}

	public boolean isInfFollowsPP() {
		return infFollowsPP;
	}
}

