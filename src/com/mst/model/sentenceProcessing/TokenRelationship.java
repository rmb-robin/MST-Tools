package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.mst.model.metadataTypes.SemanticTypes;

public class TokenRelationship {

	public TokenRelationship(){
		links = new ArrayList<>();
	}
	
	
	private String edgeName; 
	private String frameName; 
	private DateTime createdTime; 
	private String uniqueIdentifier;
	private List<String> links;
	
	
	//should reference Ids maybe...
	private WordToken toToken; 
	private WordToken fromToken;
	

	public String getEdgeName() {
		if(edgeName==null) return "";
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	public WordToken getToToken() {
		return toToken;
	}
	public void setToToken(WordToken toToken) {
		this.toToken = toToken;
	}
	public WordToken getFromToken() {
		return fromToken;
	}
	public void setFromToken(WordToken fromToken) {
		this.fromToken = fromToken;
	}
	public DateTime getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(DateTime createdTime) {
		this.createdTime = createdTime;
	} 
	
	public boolean isToFromWordTokenMatch(WordToken wordToken){
		if(this.fromToken!=null){
			if(this.fromToken.equals(wordToken))return true;
		}
		
		if(this.toToken!=null){
			if(this.toToken.equals(wordToken))return true;
		}
		return false;
	}
	
	public boolean isToFromTokenMatch(String token){
		if(this.fromToken!=null)
			if(fromToken.getToken().equals(token))return true;
		if(this.toToken!=null)
			if(toToken.getToken().equals(token))return true;
		return false;
	}
	
	public boolean isToFromTokenSetMatch(HashSet<String> tokens){
		if(this.fromToken!=null)
			if(tokens.contains(fromToken.getToken()))return true;
		if(this.toToken!=null)
			if(tokens.contains(toToken.getToken()))return true;
		return false;
	}	
	
	public String getOppositeToken(String token){
		if(this.getToToken().getToken().equals(token)) return this.getFromToken().getToken();
		return this.getToToken().getToken();
	}
	
	public String getFromTokenToTokenString(){
		return this.fromToken.getToken()+this.toToken.getToken();
	}
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	public List<String> getLinks() {
		return links;
	}
	public void setLinks(List<String> links) {
		this.links = links;
	}

}
