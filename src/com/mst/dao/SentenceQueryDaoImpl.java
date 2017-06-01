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
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;
	private HashSet<String> processedSentences; 
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;	
	}
	
	public List<SentenceQueryResult> getSentences(SentenceQueryInput input){
		processedSentences = new HashSet<>(); 
		Map<String,EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(input);
		List<SentenceQueryResult> queryResults = new ArrayList<>();
		Datastore datastore =  datastoreProvider.getDataStore();
		for(String token: input.getTokens()){
			Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
			 query
			 .search(token)
			 .field("tokenRelationships.edgeName").hasAnyOf(edgeQueriesByName.keySet())
			 .retrievedFields(true, "id", "tokenRelationships", "normalizedSentence");
			 
			 queryResults.addAll(getSentenceQueryResults(query.asList(), token));
		}
		return queryResults;
	}	

	private Map<String, EdgeQuery> convertEdgeQueryToDictionary(SentenceQueryInput input){
		Map<String,EdgeQuery> result = new HashMap<String, EdgeQuery>();
		
		for(EdgeQuery q : input.getEdges()){
			if(result.containsKey(q.getName()))continue;
			result.put(q.getName(),q);
		}
		
		return result;
	}
	
	
	private TokenRelationship findFriendOfFriendEdges(List<TokenRelationship> relationships, String token, TokenRelationship originalRelationship){
		
		for(TokenRelationship relationship:relationships){
			if(relationship.equals(originalRelationship)) continue;
			if(shouldAddTokenFromRelationship(relationship,token)){
				return relationship;
			}
		}
		return null;
	}

	private List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token){
		
		List<SentenceQueryResult> result = new ArrayList<>();
		for(SentenceDb sentenceDb : sentences){
			try{
			String id = sentenceDb.getId().toString();
			
			if(processedSentences.contains(id))continue;
			processedSentences.add(id);
			String oppositeToken = null;
			TokenRelationship foundRelationship=null;
			SentenceQueryResult queryResult = null;
			for(TokenRelationship relationship: sentenceDb.getTokenRelationships()){
				if(shouldAddTokenFromRelationship(relationship,token))
				{	
					queryResult = createSentenceQueryResult(sentenceDb);
					queryResult.getSentenceQueryEdgeResults().add(createSentenceQueryEdgeResult(relationship,EdgeResultTypes.primaryEdge));
					oppositeToken = getOppositeTokenFromRelationship(relationship,token);
					foundRelationship = relationship;
					break;
				}
			}
			
			if(foundRelationship!=null){
				TokenRelationship friendOfFriend = findFriendOfFriendEdges(sentenceDb.getTokenRelationships(),oppositeToken,foundRelationship);
				if(friendOfFriend!=null)
					queryResult.getSentenceQueryEdgeResults().add(createSentenceQueryEdgeResult(friendOfFriend,EdgeResultTypes.friendOfFriend));
				result.add(queryResult);
			}
		}
	     catch(Exception ex){
	    	 //to do.. add logging .for now this wil do..
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
	
	private SentenceQueryEdgeResult createSentenceQueryEdgeResult(TokenRelationship relationship, String edgeType){
		SentenceQueryEdgeResult queryEdgeResult = new SentenceQueryEdgeResult();
		queryEdgeResult.setEdgeName(relationship.getEdgeName());
		queryEdgeResult.setFromToken(relationship.getFromToken().getToken());
		queryEdgeResult.setToToken(relationship.getToToken().getToken());
		queryEdgeResult.setEdgeResultType(edgeType);
		return queryEdgeResult;
	}
	
	private String getOppositeTokenFromRelationship(TokenRelationship relationship, String token){
		if(relationship.getToToken().getToken().equals(token)) return relationship.getFromToken().getToken();
		return relationship.getToToken().getToken();
	}
	
	private boolean shouldAddTokenFromRelationship(TokenRelationship relation, String token){
		if(relation.getFromToken()==null) return false;
		if(relation.getToToken()==null) return false;
		
		if(relation.getFromToken().getToken().equals(token)) return true;
		if(relation.getToToken().getToken().equals(token)) return true;
		return false;
	}

	public List<String> getEdgeNamesByTokens(List<String> tokens) {
		Datastore datastore =  datastoreProvider.getDataStore();
		HashSet<String> edgeNames = new HashSet<>();
		for(String token: tokens){
			Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
			 query
			 .search(token)
			 .retrievedFields(true, "tokenRelationships");
			 
			 List<SentenceDb> sentences = query.asList();
			 edgeNames.addAll(getEdgeNamesForSentences(sentences));
	}
		return new ArrayList<>(edgeNames);
	}

	private HashSet<String> getEdgeNamesForSentences(List<SentenceDb> sentences){
		HashSet<String> result = new HashSet<>();
		for(SentenceDb sentence : sentences){
			if(sentence.getTokenRelationships()==null)continue;
			  sentence.getTokenRelationships().forEach(a-> result.add(a.getEdgeName()));
		}
		return result;
	}
}