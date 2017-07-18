package com.mst.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SemanticType;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private class SentenceQueryInstanceResult{
		public List<SentenceQueryResult> sentenceQueryResult;
		public List<SentenceDb> sentences;
	}
	
	private MongoDatastoreProvider datastoreProvider;
	private HashSet<String> processedSentences; 
	
	private DiscreteDataDao discreteDataDao; 
	
	
	private Map<String,SentenceQueryResult> queryResults;
	private Map<String,SentenceDb> cumalativeSentenceResults;
	
	
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;	
	}
	
	private void initDaos(){
		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(this.datastoreProvider);
	}
	
	private List<DiscreteData> getDiscreteDatas(SentenceQueryInput input){
		return discreteDataDao.getDiscreteDataIds(input.getDiscreteDataFilter(), input.getOrganizationId());
	}
	
	public List<SentenceQueryResult> getSentences(SentenceQueryInput input){
		processedSentences = new HashSet<>(); 
		Datastore datastore =  datastoreProvider.getDataStore();
		queryResults = new HashMap<>();
		cumalativeSentenceResults = new HashMap<>();

		boolean filterOnDiscreteData = false;
		List<DiscreteData> discreteDataIds = null;
		if(input.getDiscreteDataFilter()!=null){
			filterOnDiscreteData = true;
			initDaos();
			discreteDataIds = getDiscreteDatas(input);
		}
		
		for(int i =0;i< input.getSentenceQueryInstances().size();i++){
			SentenceQueryInstance sentenceQueryInstance = input.getSentenceQueryInstances().get(i);
			if(i==0){
				addResults(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData));
				continue;
			}
			
			if(sentenceQueryInstance.getAppender()==null) continue;
			String appender = sentenceQueryInstance.getAppender().toLowerCase();
			if(appender.equals("or")){
				addResults(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData));
				continue;
			}
			
			if(appender.equals("and")){
				filterForAnd(sentenceQueryInstance);
			}
		}
		return new ArrayList<SentenceQueryResult>(queryResults.values());
	}	

	
	private void addResults(SentenceQueryInstanceResult result){
		
		Map<String, SentenceDb> sentencesById = new HashMap<String,SentenceDb>();
		for(SentenceDb s: result.sentences){
			if(sentencesById.containsKey(s.getId().toString()))continue;
			sentencesById.put(s.getId().toString(),s);
		}
		
		for(SentenceQueryResult queryResult: result.sentenceQueryResult){
			if(this.queryResults.containsKey(queryResult.getSentenceId())) continue;
			this.queryResults.put(queryResult.getSentenceId(), queryResult);
			SentenceDb matchedSentence = sentencesById.get(queryResult.getSentenceId());
			if(matchedSentence!=null && !this.cumalativeSentenceResults.containsKey(queryResult.getSentenceId())){
				this.cumalativeSentenceResults.put(queryResult.getSentenceId(), matchedSentence);
			}
		}
	}
	
	
	private void filterForAnd(SentenceQueryInstance sentenceQueryInstance){
		Map<String,EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
		HashSet<String> matchedIds = new HashSet<>();
		for (Map.Entry<String, SentenceDb> entry : cumalativeSentenceResults.entrySet()) {
			 boolean tokenMatch = false;
			 for(String token: sentenceQueryInstance.getTokens()){
				if(entry.getValue().getOrigSentence().contains(token)){
					tokenMatch = true;
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
			if(!shouldAddSentenceToResult(entry.getValue().getTokenRelationships(),sentenceQueryInstance.getEdges())) continue;
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
	
	private SentenceQueryInstanceResult processQueryInstance(SentenceQueryInstance sentenceQueryInstance,Datastore datastore,String organizationId, List<DiscreteData> discreteDataIds, boolean filterForDiscrete){
		Map<String,EdgeQuery> edgeQueriesByName = convertEdgeQueryToDictionary(sentenceQueryInstance);
		SentenceQueryInstanceResult result = new SentenceQueryInstanceResult();
		result.sentenceQueryResult  = new ArrayList<>();
		result.sentences = new ArrayList<>();
		
		for(String token: sentenceQueryInstance.getTokens()){
			Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
			 query
			 .search(token)
			 .field("tokenRelationships.edgeName").hasAllOf(edgeQueriesByName.keySet())
			 .field("organizationId").equal(organizationId);
			 
			 if(filterForDiscrete)
				 query.field("discreteData").hasAnyOf(discreteDataIds);
			 
			 
			 query.retrievedFields(true, "id", "tokenRelationships", "normalizedSentence","origSentence", "discreteData");
			 List<SentenceDb> sentences = query.asList();
			 result.sentences.addAll(sentences);
			 result.sentenceQueryResult.addAll(getSentenceQueryResults(sentences, token,sentenceQueryInstance.getEdges()));
		}
		return result;
	}
	
	private Map<String, EdgeQuery> convertEdgeQueryToDictionary(SentenceQueryInstance input){
		Map<String,EdgeQuery> result = new HashMap<String, EdgeQuery>();
		
		for(EdgeQuery q : input.getEdges()){
			if(result.containsKey(q.getName()))continue;
			result.put(q.getName(),q);
		}
		
		return result;
	}
	
	private Map<String,List<TokenRelationship>> convertSentenceRelationshipsToMap(List<TokenRelationship> relationships){
		Map<String,List<TokenRelationship>> result = new HashMap<>();
		for(TokenRelationship tokenRelationship: relationships){
		 if(!result.containsKey(tokenRelationship.getEdgeName()))
				 result.put(tokenRelationship.getEdgeName(), new ArrayList<TokenRelationship>());
		 
		 result.get(tokenRelationship.getEdgeName()).add(tokenRelationship); 
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
	
	private boolean shouldAddSentenceToResult(List<TokenRelationship> existingtokenRelationships,List<EdgeQuery> edgeQuerys){
		
		Map<String,List<TokenRelationship>> relationshipsByEdgeName = convertSentenceRelationshipsToMap(existingtokenRelationships);
		for(EdgeQuery edgeQuery: edgeQuerys){
			if(!relationshipsByEdgeName.containsKey(edgeQuery.getName()))continue;
			HashSet<String> edgeValues = edgeQuery.getValuesLower();
			if(edgeValues==null || edgeValues.isEmpty()) continue;
			
			List<String> edgeValuesList = new ArrayList<>(edgeValues);
			
			List<TokenRelationship> tokenRelationships = relationshipsByEdgeName.get(edgeQuery.getName());
			
			if(edgeQuery.getIsNumeric()==null)
				edgeQuery.setIsNumeric(isEdgeQueryNumeric(edgeValuesList));
			
			boolean isEdgeNumeric = edgeQuery.getIsNumeric();
			boolean isEdgeInRange =false;
			for(TokenRelationship relationship: tokenRelationships){
				
				if(isEdgeNumeric && !isEdgeInRange){
					if(isTokenCardinal(relationship.getFromToken()))
						isEdgeInRange = isNumericInRange(edgeValuesList,relationship.getFromToken().getToken());
					else if(isTokenCardinal(relationship.getToToken())) 
						isEdgeInRange = isNumericInRange(edgeValuesList,relationship.getToToken().getToken());
				}
				
				if(!isEdgeNumeric){
				if(!edgeValues.contains(relationship.getFromToken().getToken()) && 
				   !edgeValues.contains(relationship.getToToken().getToken())) return false;
				}
			}
			if(isEdgeNumeric && !isEdgeInRange) return false;
		}
		return true;
	}

	private boolean isTokenCardinal(WordToken wordToken){
		if(wordToken.getSemanticType()==null);
		return wordToken.getSemanticType().equals(SemanticTypes.cardinalNumber);
	}

	
	private boolean isNumericInRange(List<String> edgeValues, String relationShipValue){
		
		if(!isNumericValue(relationShipValue)) return false;
		double value = Double.parseDouble(relationShipValue);
		
		double valueOne = Double.parseDouble(edgeValues.get(0));
		double valueTwo = Double.parseDouble(edgeValues.get(1));
		double min = Math.min(valueOne, valueTwo);
		double max = Math.max(valueOne, valueTwo);
		if(value>=min && value <=max) return true;
		return false;
	}
	
	
	private boolean isEdgeQueryNumeric(List<String> edgeValues){
		if(edgeValues.size()>2) return false;
		if(!isNumericValue(edgeValues.get(0))) return false;
		if(!isNumericValue(edgeValues.get(1))) return false;
		return true;
	}
	
	private boolean isNumericValue(String value){
		return value.matches("[-+]?\\d*\\.?\\d+");
	}
	
	private List<SentenceQueryResult> getSentenceQueryResults(List<SentenceDb> sentences, String token, List<EdgeQuery> edgeQuery){
		
		List<SentenceQueryResult> result = new ArrayList<>();
		for(SentenceDb sentenceDb : sentences){
			try{
			String id = sentenceDb.getId().toString();

			if(!shouldAddSentenceToResult(sentenceDb.getTokenRelationships(),edgeQuery)) continue;
			
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
	    	Exception e = ex;
	    	ex.printStackTrace();
	    	 //to do.. add logging .for now this wil do..
	     }
		}
		return result;
	}
	
	private SentenceQueryResult createSentenceQueryResult(SentenceDb sentenceDb){
		SentenceQueryResult result = new SentenceQueryResult();
		result.setSentence(sentenceDb.getNormalizedSentence());
		result.setSentenceId(sentenceDb.getId().toString());
		result.setDiscreteData(sentenceDb.getDiscreteData());
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
		
		if(token.split(" ").length>1){
			token = token.replace(" ","-");
		}
		
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