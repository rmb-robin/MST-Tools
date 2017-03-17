package com.mst.model.gentwo;

import java.util.ArrayList;
import java.util.List;

public class NounRelationshipInput {

	private String frameName;
	private List<NounRelationship> nounRelationships;
	
	public NounRelationshipInput(){
		nounRelationships = new ArrayList<>();
	}

	public String getFrameName() {
		return frameName;
	}

	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}

	public List<NounRelationship> getNounRelationships() {
		return nounRelationships;
	}

	public void setNounRelationships(List<NounRelationship> nounRelationships) {
		this.nounRelationships = nounRelationships;
	}
	
}
