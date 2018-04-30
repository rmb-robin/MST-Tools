package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("iterationRuleProcesserInput")
public class IterationRuleProcesserInput {
 	
	@Id
	private ObjectId id;
	
	private List<IterationDataRule> leftRules; 
	private List<IterationDataRule> rightRules; 
	
	public IterationRuleProcesserInput(){
		leftRules = new ArrayList<>();
		rightRules = new ArrayList<>();
	}

	public List<IterationDataRule> getLeftRules() {
		return leftRules;
	}

	public void setLeftRules(List<IterationDataRule> leftRules) {
		this.leftRules = leftRules;
	}

	public List<IterationDataRule> getRightRules() {
		return rightRules;
	}

	public void setRightRules(List<IterationDataRule> rightRules) {
		this.rightRules = rightRules;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	
}
