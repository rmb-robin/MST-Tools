package com.mst.model.discreet;

import java.util.Date;

public class Discreet { //implements Comparable<Discreet> {
	public String name;
	public String value;
	public Date date;
	
	public Discreet(String name, String value, Date date) {
		this.name = name;
		this.value = value;
		this.date = date;
	}
	
	//@Override
	//public int compareTo(Discreet compare) {
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
	    if(o instanceof Discreet){
	    	Discreet toCompare = (Discreet) o;
	        return this.toString().equals(toCompare.toString());
	    }
	    return false;
	}
}
