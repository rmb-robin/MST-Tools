package com.mst.model.gentwo;

import java.util.ArrayList;
import java.util.List;

public class RelationshipInput {

	private String frameName;
	private List<RelationshipMapping> relationshipMappings;
	
	public RelationshipInput(){
		relationshipMappings = new ArrayList<>();
	}

	public String getFrameName() {
		return frameName;
	}

	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}

	public List<RelationshipMapping> getRelationshipMappings() {
		return relationshipMappings;
	}

	public void setRelationMappings(List<RelationshipMapping> relationshipMappings) {
		this.relationshipMappings = relationshipMappings;
	}
	
}
