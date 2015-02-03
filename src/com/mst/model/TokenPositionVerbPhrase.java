package com.mst.model;

public class TokenPositionVerbPhrase extends TokenPosition {
	private boolean negated;
	private TokenPosition modByPP;
	private boolean eqNPHead;
	
	public TokenPositionVerbPhrase(String token, int position) {
		super(token, position);
	}
	
	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public TokenPosition getModByPP() {
		return modByPP;
	}

	public void setModByPP(TokenPosition modByPP) {
		this.modByPP = modByPP;
	}
	
	public boolean isEqNPHead() {
		return eqNPHead;
	}

	public void setEqNPHead(boolean eqNPHead) {
		this.eqNPHead = eqNPHead;
	}
}
