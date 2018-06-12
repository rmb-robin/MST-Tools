package com.mst.model;

public class StanfordDependency {
	String relation, governer, dependent;

	public StanfordDependency() {
	
	}
	
	public StanfordDependency(String relation, String governer, String dependent) {
		this.relation = relation;
		this.governer = governer;
		this.dependent = dependent;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getGoverner() {
		return governer;
	}

	public void setGoverner(String governer) {
		this.governer = governer;
	}

	public String getDependent() {
		return dependent;
	}

	public void setDependent(String dependent) {
		this.dependent = dependent;
	}
	
	
}