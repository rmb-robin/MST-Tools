package com.mst.model;

import java.util.ArrayList;

public class StanfordDependencyInfo {
	private ArrayList<StanfordDependency> dependencyList = new ArrayList<StanfordDependency>();
	private int objectCount = 0;
	private int subjectCount = 0;
	private boolean beginsWithDependentPhrase;
	private boolean compundSentence;
	
	public void incrementObjCount() {
		objectCount++;
	}
	
	public void incrementSubjCount() {
		subjectCount++;
	}
	
	public ArrayList<StanfordDependency> getDependencyList() {
		return dependencyList;
	}
	
	public void setDependencyList(ArrayList<StanfordDependency> dependencyList) {
		this.dependencyList = dependencyList;
	}
	
	public int getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}
	
	public int getSubjectCount() {
		return subjectCount;
	}
	
	public void setSubjectCount(int subjectCount) {
		this.subjectCount = subjectCount;
	}
	
	public boolean beginsWithDependentPhrase() {
		return beginsWithDependentPhrase;
	}
	
	public void setBeginsWithDependentPhrase(boolean beginsWithDependentPhrase) {
		this.beginsWithDependentPhrase = beginsWithDependentPhrase;
	}
	
	public boolean isCompundSentence() {
		return compundSentence;
	}
	
	public void setCompundSentence(boolean compundSentence) {
		this.compundSentence = compundSentence;
	}
	
}

