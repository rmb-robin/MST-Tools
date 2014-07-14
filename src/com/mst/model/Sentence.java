package com.mst.model;

import java.util.ArrayList;
import java.util.Date;

public class Sentence {

	private String articleId;
	private int position;
	private String fullSentence;
	private Date processDate;
	private ArrayList<WordToken> wordList;
	private ArrayList<MetaMapToken> metaMapList;
	
	public Sentence() {	
		this.processDate = new Date();
	}
	
	public Sentence(String articleId, int position, ArrayList<WordToken> wordList) {	
		this.articleId = articleId;
		this.position = position;
		this.wordList = wordList;
		this.processDate = new Date();
	}

	public Sentence(String fullSentence) {	
		this.fullSentence = fullSentence;
		this.processDate = new Date();
	}
	
	public void setMetaMapList(ArrayList<MetaMapToken> metaMapList) {
		this.metaMapList = metaMapList;
	}
	
	public ArrayList<MetaMapToken> getMetaMapList() {
		return this.metaMapList;
	}
	
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
	
	public String getFullSentence() {
		return fullSentence;
	}
	
	public void setFullSentence(String fullSentence) {
		this.fullSentence = fullSentence;
	}
	
	public void setWordList(ArrayList<WordToken> wordList) {
		this.wordList = wordList;
	}
	
	public ArrayList<WordToken> getWordList() {
		return this.wordList;
	}
	
	public void setDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getDate() {
		return this.processDate;
	}
}
