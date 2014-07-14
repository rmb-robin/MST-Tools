package com.mst.model;

import java.util.ArrayList;
import java.util.Date;

public class PubMedArticleList {
	private String searchTerm;
	private int minYear, maxYear;
	private int count;
	private int retMax;
	private int retStart;
	private Date dateProcessed;
	private Object tag;
	ArrayList<String> idList = new ArrayList<String>();
	
	public PubMedArticleList() { }

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	public int getMinYear() {
		return minYear;
	}

	public void setMinYear(int minYear) {
		this.minYear = minYear;
	}

	public Date getDateProcessed() {
		return dateProcessed;
	}

	public void setDateProcessed(Date dateProcessed) {
		this.dateProcessed = dateProcessed;
	}
	
	public int getMaxYear() {
		return maxYear;
	}

	public void setMaxYear(int maxYear) {
		this.maxYear = maxYear;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getRetMax() {
		return retMax;
	}

	public void setRetMax(int retMax) {
		this.retMax = retMax;
	}

	public int getRetStart() {
		return retStart;
	}

	public void setRetStart(int retStart) {
		this.retStart = retStart;
	}

	public ArrayList<String> getIdList() {
		return idList;
	}

	public void setIdList(ArrayList<String> idList) {
		this.idList = idList;
	}
	
	public String listToString() {
		StringBuilder ret = new StringBuilder();
		
		for(String id : idList)
			ret.append(id).append(",");
			
		return ret.toString();
	}
}