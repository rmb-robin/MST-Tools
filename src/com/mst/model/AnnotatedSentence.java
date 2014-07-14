package com.mst.model;

import java.util.ArrayList;
import java.util.Date;

public class AnnotatedSentence {

	private String articleId;
	private int position;
	private Date processDate;
	private ArrayList<String> sentenceList;
	
	public AnnotatedSentence() { }

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}
	
	public String getArticleId() {
		return articleId;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public void setDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getDate() {
		return this.processDate;
	}
	
	public void setSentenceList(ArrayList<String> sentenceList) {
		this.sentenceList = sentenceList;
	}
	
	public ArrayList<String> getSentenceList() {
		return this.sentenceList;
	}
}
