package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("dynamicedgecreationrules")
public class DynamicEdgeCreationRule {


	@Id
	private ObjectId id;
	private String name; 
	private List<DynamicEdgeCondition> conditions; 
	
	public DynamicEdgeCreationRule(){
		conditions = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DynamicEdgeCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<DynamicEdgeCondition> conditions) {
		this.conditions = conditions;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	
}
