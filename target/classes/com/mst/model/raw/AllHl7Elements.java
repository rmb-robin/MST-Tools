package com.mst.model.raw;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Entity;

@Entity("allhl7elements")
public class AllHl7Elements {

	private List<String> elements;

	public List<String> getElements() {
		return elements;
	}

	public void setElements(List<String> elements) {
		this.elements = elements;
	} 
	
	public AllHl7Elements(){
		elements = new ArrayList<>();
	}
}
