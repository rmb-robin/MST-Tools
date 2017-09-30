package com.mst.filter;

import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class FriendOfFriendServiceImpl implements FriendOfFriendService {

	SentenceFilter sentenceFilter;
	public FriendOfFriendServiceImpl(){
		sentenceFilter = new SentenceFilterImpl();
	}
	
	public ShouldMatchOnSentenceEdgesResult findFriendOfFriendEdges(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship, HashSet<String> edgeNames){
		
		for(TokenRelationship relationship:relationships){
			if(relationship.equals(originalRelationship)) continue;
			if(relationship.getEdgeName()==null)continue;
			if(!edgeNames.contains(relationship.getEdgeName())) continue;
			ShouldMatchOnSentenceEdgesResult result = sentenceFilter.shouldAddTokenFromRelationship(relationship,token);
			if(result.isMatch()){
				result.setRelationship(relationship);
				return result;
			}
		}
		return null;
	}
	
}
