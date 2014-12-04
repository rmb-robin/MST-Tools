package com.mst.model;

import com.mst.util.Constants;

public class VerbPhraseMetadata {
	private Constants.VerbClass _class;
	private TokenPosition subj;
	private TokenPosition verb;
	private TokenPosition subjc;
	private boolean compound;
	private boolean transitive;
	private boolean negated;
	private TokenPosition subjModByPP; // TPos value to = PP OBJ
	private TokenPosition subjcModByPP; // TPos value to = PP OBJ
	private boolean subjEqNPHead;
	private boolean subjcEqNPHead;

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
	
	public TokenPosition getSubj() {
		return subj;
	}

	public void setSubj(TokenPosition subj) {
		this.subj = subj;
	}

	public TokenPosition getVerb() {
		return verb;
	}

	public void setVerb(TokenPosition verb) {
		this.verb = verb;
	}

	public TokenPosition getSubjC() {
		return subjc;
	}

	public void setSubjC(TokenPosition subjc) {
		this.subjc = subjc;
	}

	public boolean isCompound() {
		return compound;
	}

	public void setTransitive(boolean transitive) {
		this.transitive = transitive;
	}

	public boolean isTransitive() {
		return transitive;
	}

	public void setCompound(boolean compound) {
		this.compound = compound;
	}
	
	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public TokenPosition getSubjModByPP() {
		return subjModByPP;
	}

	public void setSubjModByPP(TokenPosition subjModByPP) {
		this.subjModByPP = subjModByPP;
	}

	public TokenPosition getSubjCModByPP() {
		return subjcModByPP;
	}

	public void setSubjCModByPP(TokenPosition subjcModByPP) {
		this.subjcModByPP = subjcModByPP;
	}

	public boolean isSubjEqNPHead() {
		return subjEqNPHead;
	}

	public void setSubjEqNPHead(boolean subjEqNPHead) {
		this.subjEqNPHead = subjEqNPHead;
	}

	public boolean isSubjCEqNPHead() {
		return subjcEqNPHead;
	}

	public void setSubjCEqNPHead(boolean subjcEqNPHead) {
		this.subjcEqNPHead = subjcEqNPHead;
	}
}

