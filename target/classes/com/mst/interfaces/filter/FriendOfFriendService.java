package com.mst.interfaces.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface FriendOfFriendService {
	ShouldMatchOnSentenceEdgesResult findFriendOfFriendEdges(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship, HashSet<String> edgeNames);
	boolean shouldAddSentenceOnExistenceFriendOfFriend(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship);
	List<TokenRelationship> getFriendOfFriendForBothTokens(List<TokenRelationship> relationships,TokenRelationship originalRelationship);

}
