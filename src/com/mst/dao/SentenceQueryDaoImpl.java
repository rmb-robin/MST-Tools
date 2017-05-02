package com.mst.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;
	private HashSet<String> processedSentences; 
	public List<SentenceQueryResult> getSentences(SentenceQueryInput input){
		processedSentences = new HashSet<>();
		List<SentenceQueryResult> queryResults = new ArrayList<>();
		Datastore datastore =  datastoreProvider.getDataStore();
		for(String token: input.getTokens()){
			Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
			 query
			 .field("origSentence").contains(token)
			 .field("tokenRelationships.edgeName").equal(input.getEdgeName());
			 queryResults.addAll(getSentenceQueryResults(query.asList(), token, input.getEdgeName()));
		}
		return queryResults;
	}	

	private TokenRelationship findFriendOfFriendEdges(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship){
		
		for(TokenRelationship relationship:relationships){
			if(relationship.equals(originalRelationship)) continue;
			if(shouldAddTokenFromRelationship(relationship,token, null)){
				return relationship;
			}
		}
		return null;
	}

	private List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, String edgeName){
		
		List<SentenceQueryResult> result = new ArrayList<>();
		for(SentenceDb sentenceDb : sentences){
			String id = sentenceDb.getId().toString();
			if(processedSentences.contains(id))continue;
			processedSentences.add(id);
			String oppositeToken = null;
			TokenRelationship foundRelationship=null;
			SentenceQueryResult queryResult = null;
			for(TokenRelationship relationship: sentenceDb.getTokenRelationships()){
				if(shouldAddTokenFromRelationship(relationship,token, edgeName))
				{	
					queryResult = createSentenceQueryResult(sentenceDb);
					queryResult.getSentenceQueryEdgeResults().add(createSentenceQueryEdgeResult(relationship));
					oppositeToken = getOppositeTokenFromRelationship(relationship,token);
					foundRelationship = relationship;
					break;
				}
			}
			
			if(foundRelationship!=null){
				TokenRelationship friendOfFriend = findFriendOfFriendEdges(sentenceDb.getTokenRelationships(),oppositeToken,foundRelationship);
				if(friendOfFriend!=null)
					queryResult.getSentenceQueryEdgeResults().add(createSentenceQueryEdgeResult(friendOfFriend));
				
				result.add(queryResult);
			}
		}
		return result;
	}
	
	private SentenceQueryResult createSentenceQueryResult(SentenceDb sentenceDb){
		SentenceQueryResult result = new SentenceQueryResult();
		result.setSentence(sentenceDb.getNormalizedSentence());
		result.setSentenceId(sentenceDb.getId().toString());		
		return result;
	}
	
	private SentenceQueryEdgeResult createSentenceQueryEdgeResult(TokenRelationship relationship){
		SentenceQueryEdgeResult queryEdgeResult = new SentenceQueryEdgeResult();
		queryEdgeResult.setEdgeName(relationship.getEdgeName());
		queryEdgeResult.setFromToken(relationship.getFromToken().getToken());
		queryEdgeResult.setToToken(relationship.getToToken().getToken());
		return queryEdgeResult;
	}
	
	private String getOppositeTokenFromRelationship(TokenRelationship relationship, String token){
		if(relationship.getToToken().getToken().equals(token)) return relationship.getFromToken().getToken();
		return relationship.getToToken().getToken();
	}
	
	private boolean shouldAddTokenFromRelationship(TokenRelationship relation, String token, String edgeName){
		if(edgeName != null)
			if(!relation.getEdgeName().equals(edgeName)) return false;
		
		
			if(relation.getFromToken().getToken().equals(token)) return true;
		if(relation.getToToken().getToken().equals(token)) return true;
		return false;
	}

	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;
		
	}
}