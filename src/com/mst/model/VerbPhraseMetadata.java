package com.mst.model;

import com.mst.util.Constants;

public class VerbPhraseMetadata {
	private Constants.VerbClass _class;
	private VerbPhraseToken subj;
	private VerbPhraseToken verb;
	private VerbPhraseToken subjc;
	private boolean compound;
	private boolean intransitive = true;
	private boolean infFollowsPP; // infinitive verb follows prep phrase

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

	public VerbPhraseToken getVerb() {
		return verb;
	}

	public void setVerb(VerbPhraseToken verb) {
		this.verb = verb;
	}

	public VerbPhraseToken getSubjC() {
		return subjc;
	}

	public void setSubjC(VerbPhraseToken subjc) {
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
	
	public void setInfFollowsPP(boolean infFollowsPP) {
		this.infFollowsPP = infFollowsPP;
	}

	public boolean isInfFollowsPP() {
		return infFollowsPP;
	}
}

