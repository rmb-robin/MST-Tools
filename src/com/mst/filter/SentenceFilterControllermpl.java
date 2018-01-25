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
import com.mst.model.SentenceQuery.EdgeQueryMapResult;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.RecommandedTokenRelationshipUtil;
import com.mst.util.TokenRelationshipUtil;

public class SentenceFilterControllermpl implements SentenceFilterController {
	private HashSet<String> processedSentences; 
	private SentenceFilter sentenceFilter;
	private Map<String,SentenceQueryResult> queryResults;
	private Map<String,SentenceDiscovery> cumalativeSentenceResults;
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
	public List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDiscovery> sentences, String token, List<EdgeQuery> edgeQuery, String searchToken){
		 
		List<SentenceQueryResult> result = new ArrayList<>();
		for(SentenceDiscovery sentenceDiscovery : sentences){
			try{
				String id = sentenceDiscovery.getId().toString();
				
				if(id.equals("5a661f2191775d0aaa69a391"))	{
					int a =1;
					int b = a;
					System.out.println("");
				}
				
				if(processedSentences.contains(id))continue;
				List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(sentenceDiscovery.getWordEmbeddings());
				if(shouldByPassResult(relationships,edgeQuery,searchToken)) continue;
			    
				String oppositeToken = null;
				TokenRelationship foundRelationship=null;
				SentenceQueryResult queryResult = null;
				HashSet<String> edgeNameHash = new HashSet<>();
				edgeQuery.forEach(a-> edgeNameHash.add(a.getName()) );
				boolean addFriendofFriendExistence=true;
				
				for(TokenRelationship relationship: relationships){
				  if(relationship.getEdgeName()==null)continue;
				  boolean isEdgeInSearchQuery = TokenRelationshipUtil.isEdgeMatchFromHas(relationship, edgeNameHash);
				  ShouldMatchOnSentenceEdgesResult edgesResult  = sentenceFilter.shouldAddTokenFromRelationship(relationship,token);
				  if(edgesResult.isMatch())
					{	
					    if(queryResult==null){
					    	queryResult = SentenceQueryResultFactory.createSentenceQueryResult(sentenceDiscovery);
					    }
						if(isEdgeInSearchQuery)
							queryResult.getSentenceQueryEdgeResults()
							.add(SentenceQueryResultFactory.createSentenceQueryEdgeResult(relationship,EdgeResultTypes.primaryEdge,matches));
						
						oppositeToken = relationship.getOppositeToken(token);
						foundRelationship = relationship;
											 
						ShouldMatchOnSentenceEdgesResult friendResult = friendOfFriendService.findFriendOfFriendEdges(relationships,oppositeToken,foundRelationship,edgeNameHash);
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
		
		EdgeQueryMapResult   mapResult = convertEdgeQueryToDictionary(sentenceQueryInstance);
		Map<String,EdgeQuery> edgeQueriesByName = mapResult.getNonNamedEdges();
		HashSet<String> matchedIds = new HashSet<>();
		for (Map.Entry<String, SentenceDiscovery> entry : cumalativeSentenceResults.entrySet()) {
			List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(entry.getValue().getWordEmbeddings()); 
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
			relationships.stream().forEach(a-> sentenceUniqueEdgeNames.add(a.getEdgeName()));
			for(String edgeName: edgeQueriesByName.keySet()){
				if(!sentenceUniqueEdgeNames.contains(edgeName)){
					tokenMatch = false;
					break;
				}
			}
				
			if(!tokenMatch) continue;
			if(shouldByPassResult(relationships,sentenceQueryInstance.getEdges(),matchedToken)) continue;
		
			matchedIds.add(entry.getKey());
		 }
		updateExistingResults(matchedIds);
	}


	public void filterForAndNot(SentenceQueryInstance sentenceQueryInstance){
	HashSet<String> matchedIds = new HashSet<>();
		for (Map.Entry<String, SentenceDiscovery> entry : cumalativeSentenceResults.entrySet()) {
			List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(entry.getValue().getWordEmbeddings()); 
			 
			boolean shouldExclude=false; 
			for(String token: sentenceQueryInstance.getTokens()){
				shouldExclude =shouldByPassResultExclude(relationships,sentenceQueryInstance.getEdges(), token);
				if(shouldExclude) break;
			}
			if(shouldExclude) continue;
			matchedIds.add(entry.getKey());
		 }
		updateExistingResults(matchedIds);
	}
	
	public void filterForAndNotAll(SentenceQueryInstance sentenceQueryInstance){
		//TO DO.. ChECK..
		EdgeQueryMapResult mapResult = convertEdgeQueryToDictionary(sentenceQueryInstance);
		Map<String,EdgeQuery> edgeQueriesByName = mapResult.getNonNamedEdges();
		HashSet<String> matchedIds = new HashSet<>();
		List<String> unmatchedTokens = new ArrayList<String>();
		for (Map.Entry<String, SentenceDiscovery> entry : cumalativeSentenceResults.entrySet()) {
			List<TokenRelationship> relationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(entry.getValue().getWordEmbeddings()); 
			 
			int tokenMatchCount=0;
			HashSet<String> sentenceUniqueEdgeNames = new HashSet<>();
			relationships.stream().forEach(a-> sentenceUniqueEdgeNames.add(a.getEdgeName()));
			for(String edgeName: edgeQueriesByName.keySet()){
				if(sentenceUniqueEdgeNames.contains(edgeName)){
					tokenMatchCount+=1;
				}
			}
				
			if(tokenMatchCount==edgeQueriesByName.size()) continue;
			for(String token: unmatchedTokens){
				if(shouldByPassResultExclude(relationships,sentenceQueryInstance.getEdges(), token)) continue;
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
		Map<String, SentenceDiscovery> sentencesById = new HashMap<String,SentenceDiscovery>();
		for(SentenceDiscovery s: result.getSentences()){
			if(sentencesById.containsKey(s.getId().toString()))continue;
			sentencesById.put(s.getId().toString(),s);
		}
		
		for(SentenceQueryResult queryResult: result.getSentenceQueryResult()){
			if(this.queryResults.containsKey(queryResult.getSentenceId())) continue;
			this.queryResults.put(queryResult.getSentenceId(), queryResult);
			SentenceDiscovery matchedSentence = sentencesById.get(queryResult.getSentenceId());
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
	

	private void addToMapResult(EdgeQuery q, Map<String, EdgeQuery> edgeQuery){
		if(edgeQuery.containsKey(q.getName()))return;
		edgeQuery.put(q.getName(),q);
	}
	
	
	public EdgeQueryMapResult convertEdgeQueryToDictionary(SentenceQueryInstance input){
		EdgeQueryMapResult result = new EdgeQueryMapResult();
		
		for(EdgeQuery q : input.getEdges()){
			if(q.getIsNamedEdge()){
				addToMapResult(q, result.getNamedEdges());
			}
			else {
				addToMapResult(q, result.getNonNamedEdges());
			}
		}
		
		return result;
	}
}
