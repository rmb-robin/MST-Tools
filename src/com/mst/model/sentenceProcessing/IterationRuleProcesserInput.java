package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.List;

public class IterationRuleProcesserInput {

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
	
	
}
