package com.mst.model.ontology;

import java.util.ArrayList;

public class Finding {

	public String value;  //Stable
	public String _class;  //Appearance-Viz
	public String parent;
	public ArrayList<FindingSite> findingSites = new ArrayList<FindingSite>();
	public String match;

}
