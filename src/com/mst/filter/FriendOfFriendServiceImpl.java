package com.mst.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.TokenRelationshipUtil;

public class FriendOfFriendServiceImpl implements FriendOfFriendService {

	SentenceFilter sentenceFilter;
	HashSet<String> edgeNames = EdgeNames.getExistenceSet();
	HashSet<String> nonExistenceSetOnly = EdgeNames.getNonExistenceSetOnly();
	
	public FriendOfFriendServiceImpl(){
		sentenceFilter = new SentenceFilterImpl();
	}
	
	public ShouldMatchOnSentenceEdgesResult findFriendOfFriendEdges(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship, HashSet<String> edgeNames){
		
		for(TokenRelationship relationship:relationships){
			if(relationship.equals(originalRelationship)) continue;
			if(relationship.getEdgeName()==null)continue;
			if(!edgeNames.contains(relationship.getEdgeName())) continue; //removing for now...
			ShouldMatchOnSentenceEdgesResult result = sentenceFilter.shouldAddTokenFromRelationship(relationship,token);
			if(result.isMatch()){
				result.setRelationship(relationship);
				return result;
			}
		}
		return null;
	}
	
	public List<TokenRelationship> getFriendOfFriendForBothTokens(List<TokenRelationship> relationships,TokenRelationship originalRelationship){
		List<TokenRelationship> results = new ArrayList<TokenRelationship>();
		
		for(TokenRelationship relationship:relationships){
			if(relationship.equals(originalRelationship)) continue;
			ShouldMatchOnSentenceEdgesResult result = sentenceFilter.shouldAddTokenFromRelationship(relationship,originalRelationship.getToToken().getToken());
			if(result.isMatch()){
				results.add(relationship);
				continue;	
			}
		}
		
		return results;
	}
	
	
	public boolean shouldAddSentenceOnExistenceFriendOfFriend(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship){
		
		for(int i=0;i<5;i++){
			ShouldMatchOnSentenceEdgesResult  result =  findFriendOfFriendEdges(relationships, token, originalRelationship, edgeNames);
			if(result == null) return false;
			
			 originalRelationship = result.getRelationship();
			 token = originalRelationship.getOppositeToken(token);
			 if(nonExistenceSetOnly.contains(originalRelationship.getEdgeName())) return false;
		}
		return true;
	}
}
