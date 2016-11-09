package com.mst.model.discrete;

import java.util.Date;

public class Discrete { //implements Comparable<Discreet> {
	public String name;
	public String value;
	public Date date;
	
	public Discrete(String name, String value, Date date) {
		this.name = name;
		this.value = value;
		this.date = date;
	}
	
	//@Override
	//public int compareTo(Discrete compare) {
	//	return this.name.compareTo(compare.name);
    //}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name).append(" | ")
			.append(this.value).append(" | ")
			.append(this.date);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Discrete){
	    	Discrete toCompare = (Discrete) o;
	        return this.toString().equals(toCompare.toString());
	    }
	    return false;
	}
}
