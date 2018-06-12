package com.mst.model;

public class VerbQualifier {
	public String verb;
	public String st;
	public String nonInf;
	public String inf;
	public String pastPart; // -ed
	public String presentPart; // -ing
	
	public VerbQualifier(String verb, String st, String nonInf, String inf, String pastPart, String presentPart) {
		this.verb = verb;
		this.st = st;
		this.nonInf = nonInf;
		this.inf = inf;
		this.pastPart = pastPart;
		this.presentPart = presentPart;
	}
}
