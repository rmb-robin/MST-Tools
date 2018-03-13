package com.mst.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.interfaces.filter.SentenceFilterController;
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.TokenRelationshipUtil;

public class SentenceFilterControllermpl implements SentenceFilterController {
	private HashSet<String> processedSentences; 
	private SentenceFilter sentenceFilter;
	private Map<String,SentenceQueryResult> queryResults;
	private Map<String,SentenceDb> cumalativeSentenceResults;
	private FriendOfFriendService friendOfFriendService; 
	
	public SentenceFilterControllermpl(){
		processedSentences = new HashSet<>();
		queryResults = new HashMap<>();
		cumalativeSentenceResults = new HashMap<>();
		sentenceFilter = new SentenceFilterImpl();
		friendOfFriendService = new FriendOfFriendServiceImpl();
	}
	
	public Map<String,SentenceQueryResult>  getQueryResults(){
		return queryResults;
	}
	
	private Map<String, MatchInfo> matches; 
	public List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, List<EdgeQuery> edgeQuery, String searchToken){
		 
		List<SentenceQueryResult> result = new ArrayList<>();
		for(SentenceDb sentenceDb : sentences){
			try{
			String id = sentenceDb.getId().toString();
			if(processedSentences.contains(id))continue;
			if(shouldByPassResult(sentenceDb.getTokenRelationships(),edgeQuery,searchToken)) continue;
		    
			String oppositeToken = null;
			TokenRelationship foundRelationship=null;
			SentenceQueryResult queryResult = null;
			HashSet<String> edgeNameHash = new HashSet<>();
			edgeQuery.forEach(a-> edgeNameHash.add(a.getName()) );
			boolean addFriendofFriendExistence=true;
			Map<String, List<TokenRelationship>> relationsByUniqueTofrom = TokenRelationshipUtil.getMapByDistinctToFrom(sentenceDb.getTokenRelationships());
			for(TokenRelationship relationship: sentenceDb.getTokenRelationships()){
			  if(relationship.getEdgeName()==null)continue;
			  boolean isEdgeInSearchQuery = edgeNameHash.contains(relationship.getEdgeName());

			  ShouldMatchOnSentenceEdgesResult edgesResult  = sentenceFilter.shouldAddTokenFromRelationship(relationship,token);
			  if(edgesResult.isMatch())
				{	
				    if(queryResult==null){
				    	queryResult = SentenceQueryResultFactory.createSentenceQueryResult(sentenceDb);
				    }

				    if(isEdgeInSearchQuery){
				    	queryResult.getSentenceQueryEdgeResults()
							.add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship,EdgeResultTypes.primaryEdge,matches));
				    }
					oppositeToken = relationship.getOppositeToken(token);
					foundRelationship = relationship;
										
					ShouldMatchOnSentenceEdgesResult friendResult = friendOfFriendService.findFriendOfFriendEdges(sentenceDb.getTokenRelationships(),oppositeToken,foundRelationship,edgeNameHash);
					if(friendResult!=null)
						queryResult.getSentenceQueryEdgeResults().add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(friendResult.getRelationship(),EdgeResultTypes.friendOfFriend,matches));
				}
			}

			addFriendofFriendExistence = true;
			if(addFriendofFriendExistence && queryResult!=null){
				result.add(queryResult);
				processedSentences.add(id);
			}
		}
	     catch(Exception ex){
	    	Exception e = ex;
	    	ex.printStackTrace();
	    	 //to do.. add logging .for now this wil do..
	     }
		}
		return result;
	}
	
	public void filterForAnd(SentenceQueryInstance sentenceQueryInstance){
		Map<String,EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
		HashSet<String> matchedIds = new HashSet<>();
		for (Map.Entry<String, SentenceDb> entry : cumalativeSentenceResults.entrySet()) {
			 boolean tokenMatch = false;
			 String matchedToken = "";
			 for(String token: sentenceQueryInstance.getTokens()){
				if(entry.getValue().getOrigSentence().contains(token)){
					tokenMatch = true;
					matchedToken = token;
					break;
				}
			}
			 
			if(!tokenMatch) continue;

			HashSet<String> sentenceUniqueEdgeNames = new HashSet<>();
			entry.getValue().getTokenRelationships().stream().forEach(a-> sentenceUniqueEdgeNames.add(a.getEdgeName()));
			for(String edgeName: edgeQueriesByName.keySet()){
				if(!sentenceUniqueEdgeNames.contains(edgeName)){
					tokenMatch = false;
					break;
				}
			}
				
			if(!tokenMatch) continue;
			if(shouldByPassResult(entry.getValue().getTokenRelationships(),sentenceQueryInstance.getEdges(),matchedToken)) continue;
		
			matchedIds.add(entry.getKey());
		 }
		updateExistingResults(matchedIds);
	}


	public void filterForAndNot(SentenceQueryInstance sentenceQueryInstance) {
		HashSet<String> matchedIds = new HashSet<>();
		for (Map.Entry<String, SentenceDb> entry : cumalativeSentenceResults.entrySet()) {

			// do all tokens as this is not a db query impact
			for (String token : sentenceQueryInstance.getTokens()) {
				if (!entry.getValue().getOrigSentence().contains(token)) {
					continue;
				}
				EdgeMatchOnQueryResult result = sentenceFilter.AreEdgesMatchOnQuery(entry.getValue().getTokenRelationships(),sentenceQueryInstance.getEdges(),token);
 				
				if (result.isMatch()) {
					matchedIds.add(entry.getKey());
					break;
				}
			}
			// matchedIds.add(entry.getKey());
		}
		// changing since update existing does the oppisite of what we want here.
		for(String id: matchedIds){
			queryResults.remove(id);
			if(this.cumalativeSentenceResults.containsKey(id))
				this.cumalativeSentenceResults.remove(id);
		}
		//updateExistingResults(matchedIds);
	}

	public void filterForAndNotAll(SentenceQueryInstance sentenceQueryInstance){
		Map<String,EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
		HashSet<String> matchedIds = new HashSet<>();
		List<String> unmatchedTokens = new ArrayList<String>();
		for (Map.Entry<String, SentenceDb> entry : cumalativeSentenceResults.entrySet()) {
			int tokenMatchCount=0;
			HashSet<String> sentenceUniqueEdgeNames = new HashSet<>();
			entry.getValue().getTokenRelationships().stream().forEach(a-> sentenceUniqueEdgeNames.add(a.getEdgeName()));
			for(String edgeName: edgeQueriesByName.keySet()){
				if(sentenceUniqueEdgeNames.contains(edgeName)){
					tokenMatchCount+=1;
				}
			}
				
			if(tokenMatchCount==edgeQueriesByName.size()) continue;
			for(String token: unmatchedTokens){
				if(shouldByPassResultExclude(entry.getValue().getTokenRelationships(),sentenceQueryInstance.getEdges(), token)) continue;
			}
			matchedIds.add(entry.getKey());
		 }
		updateExistingResults(matchedIds);
	}
		
	

	
	private void updateExistingResults(HashSet<String> matchedIds){
		List<String> idsToRemove = new ArrayList<>();
		for(String id: this.queryResults.keySet()){
			if(matchedIds.contains(id))continue;
			idsToRemove.add(id);
		}
	
		for(String id: idsToRemove){
			queryResults.remove(id);
			if(this.cumalativeSentenceResults.containsKey(id))
				this.cumalativeSentenceResults.remove(id);
		}
	}
	
	public void addSentencesToResult(SentenceQueryInstanceResult result){		
		Map<String, SentenceDb> sentencesById = new HashMap<String,SentenceDb>();
		for(SentenceDb s: result.getSentences()){
			if(sentencesById.containsKey(s.getId().toString()))continue;
			sentencesById.put(s.getId().toString(),s);
		}
		
		for(SentenceQueryResult queryResult: result.getSentenceQueryResult()){
			if(this.queryResults.containsKey(queryResult.getSentenceId())) continue;
			this.queryResults.put(queryResult.getSentenceId(), queryResult);
			SentenceDb matchedSentence = sentencesById.get(queryResult.getSentenceId());
			if(matchedSentence!=null && !this.cumalativeSentenceResults.containsKey(queryResult.getSentenceId())){
				this.cumalativeSentenceResults.put(queryResult.getSentenceId(), matchedSentence);
			}
		}
	}
	
	private boolean shouldByPassResult(List<TokenRelationship> existingtokenRelationships,List<EdgeQuery> edgeQueries, String searchToken){
		EdgeMatchOnQueryResult edgeMatchOnQueryResult  = sentenceFilter.AreEdgesMatchOnQuery(existingtokenRelationships,edgeQueries,searchToken);
		matches = edgeMatchOnQueryResult.getMatches();
		return !edgeMatchOnQueryResult.isMatch();
	}
	
	
	private boolean shouldByPassResultExclude(List<TokenRelationship> existingtokenRelationships,List<EdgeQuery> edgeQueries, String searchToken){
		EdgeMatchOnQueryResult edgeMatchOnQueryResult  =sentenceFilter.AreEdgesMatchOnQuery(existingtokenRelationships,edgeQueries,searchToken);
		if(edgeMatchOnQueryResult.isMatch() && edgeMatchOnQueryResult.isDidTokenRelationsContainAnyMatches()) return true;
		return false;
	}
	


	public Map<String, EdgeQuery> convertEdgeQueryToDictionary(SentenceQueryInstance input){
		Map<String,EdgeQuery> result = new HashMap<String, EdgeQuery>();
		
		for(EdgeQuery q : input.getEdges()){
			if(result.containsKey(q.getName()))continue;
			result.put(q.getName(),q);
		}
		
		return result;
	}
}
