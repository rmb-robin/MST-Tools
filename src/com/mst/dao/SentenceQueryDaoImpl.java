package com.mst.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

import com.mst.filter.SentenceDiscoveryFilterImpl;
import com.mst.filter.SentenceFilterControllermpl;
import com.mst.filter.SentenceQueryResultFactory;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.filter.SentenceDiscoveryFilter;
import com.mst.interfaces.filter.SentenceFilterController;
import com.mst.model.SemanticType;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.EdgeQueryMapResult;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.graph.Edge;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.QueryAppenderTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;

	private SentenceFilterController sentenceFilterController; 
	private DiscreteDataDao discreteDataDao; 
	private SentenceDiscoveryFilter sentenceDiscoveryFilter; 
			
	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;	
	}
	
	private void init(){
		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(this.datastoreProvider);
	}
	
	private List<DiscreteData> getDiscreteDatas(SentenceQueryInput input){
		return discreteDataDao.getDiscreteDatas(input.getDiscreteDataFilter(), input.getOrganizationId(),false);
	}
	
	public List<SentenceQueryResult> getSentences(SentenceQueryInput input){
		sentenceFilterController = new SentenceFilterControllermpl();
		Datastore datastore =  datastoreProvider.getDefaultDb();

		boolean filterOnDiscreteData = false;
		List<DiscreteData> discreteDataIds = null;
		if(input.getDiscreteDataFilter()!=null && !input.getDiscreteDataFilter().isEmpty()){
			filterOnDiscreteData = true;
			init();
			discreteDataIds = getDiscreteDatas(input);
		}
		
		for(int i =0;i< input.getSentenceQueryInstances().size();i++){
			SentenceQueryInstance sentenceQueryInstance = input.getSentenceQueryInstances().get(i);
			if(i==0){
				sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData));
				continue;
			}
			
			if(sentenceQueryInstance.getAppender()==null) continue;
			String appender = sentenceQueryInstance.getAppender().toLowerCase();
			if(appender.equals(QueryAppenderTypes.OR)){
				sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData));
				continue;
			}
			
			if(appender.equals(QueryAppenderTypes.AND)){
				sentenceFilterController.filterForAnd(sentenceQueryInstance);
			}
			
			if(appender.equals(QueryAppenderTypes.ANDNOT))
				sentenceFilterController.filterForAndNot(sentenceQueryInstance);
			
			if(appender.equals(QueryAppenderTypes.ANDNOTALL))
				sentenceFilterController.filterForAndNotAll(sentenceQueryInstance);
		}
		return new ArrayList<SentenceQueryResult>(sentenceFilterController.getQueryResults().values());
	}	
	
	@Override
	public List<SentenceQueryResult> getSentencesByText(SentenceQueryTextInput input) {
		sentenceDiscoveryFilter = new SentenceDiscoveryFilterImpl();
		List<SentenceQueryResult> result = new ArrayList<SentenceQueryResult>();
		Datastore datastore =  datastoreProvider.getDefaultDb();
		Pattern pattern = Pattern.compile(input.getText(), Pattern.CASE_INSENSITIVE);
		Query<SentenceDiscovery> query = datastore.createQuery(SentenceDiscovery.class);
		query
		.field("origSentence").equal(pattern);
		 
		List<SentenceDiscovery> sentences = query.asList();
		sentences = sentenceDiscoveryFilter.filter(sentences, input.getText());
		SentenceQueryResultFactory factory = new SentenceQueryResultFactory(); 
		for(SentenceDiscovery db:sentences){
			SentenceQueryResult queryResult = new SentenceQueryResult();
			queryResult.setSentence(db.getOrigSentence());
			queryResult.setSentenceId(db.getId().toString());
			queryResult.getSentenceQueryEdgeResults().add(factory.createSentenceQueryResultForDiscovery(input.getText(), db));
			result.add(queryResult);
		}
		return filterTextSentences(result);
	}
	
	private List<SentenceQueryResult> filterTextSentences(List<SentenceQueryResult> sentences){
		List<SentenceQueryResult> result = new ArrayList<>();
		
		for(SentenceQueryResult sentenceQueryResult: sentences){
			SentenceQueryEdgeResult edge = sentenceQueryResult.getSentenceQueryEdgeResults().get(0);
			if(edge.getEdgeName()==null) continue;
			if(edge.getEdgeResultType()==null) continue;
			if(edge.getFromToken()==null) continue;
			if(edge.getToToken()==null) continue;
			if(edge.getTokenType()==null) continue;
			if(edge.getMatchedValue()==null) continue;
			if(isMatchTokenMatch(edge))
				result.add(sentenceQueryResult);
		}
		return result;
	}
	
	private boolean isMatchTokenMatch(SentenceQueryEdgeResult edge){
		if(edge.getMatchedValue().trim().equals(edge.getToToken().trim()))return true;
		if(edge.getMatchedValue().trim().equals(edge.getFromToken().trim()))return true;
		return false;
	}

 
	
	private SentenceQueryInstanceResult processQueryInstance(SentenceQueryInstance sentenceQueryInstance,Datastore datastore,String organizationId, List<DiscreteData> discreteDataIds, boolean filterForDiscrete){
		EdgeQueryMapResult   mapResult  = sentenceFilterController.convertEdgeQueryToDictionary(sentenceQueryInstance);

		SentenceQueryInstanceResult result = new SentenceQueryInstanceResult();

		
		
		this.init();
		for(String token: sentenceQueryInstance.getTokens()){
			Query<SentenceDiscovery> query = datastore.createQuery(SentenceDiscovery.class);
			 query
			 .search(token)
			 .field("organizationId").equal(organizationId);
			 
			 
			 if(!mapResult.getNonNamedEdges().isEmpty())
				 query.field("wordEmbeddings.tokenRelationship.edgeName").hasAllOf(mapResult.getNonNamedEdges().keySet());
				 
			 if(!mapResult.getNamedEdges().isEmpty()){
				 query.field("wordEmbeddings.tokenRelationship.namedEdge").hasAllOf(mapResult.getNamedEdges().keySet());
			 }
			 
			 if(filterForDiscrete)
				 query.field("discreteData").hasAnyOf(discreteDataIds);
			 
			 
			 query.retrievedFields(true, "id", "wordEmbeddings", "normalizedSentence","origSentence", "discreteData");
			 List<SentenceDiscovery> sentences = query.asList();
			 
			 List<SentenceQueryResult> queryResults = sentenceFilterController.getSentenceQueryResults(sentences, token,sentenceQueryInstance.getEdges(), token);
			 result.getSentenceQueryResult().addAll(queryResults);
			 result.getSentences().addAll(getMatchedSentences(queryResults, sentences));
		}
		return result;
	}

	
	private List<SentenceDiscovery> getMatchedSentences(List<SentenceQueryResult> queryResults, List<SentenceDiscovery> sentences){
		HashSet<String> ids = new HashSet<>();
		for(SentenceQueryResult q: queryResults){
			ids.add(q.getSentenceId());
		}
		List<SentenceDiscovery> result = new ArrayList<>();
		for(SentenceDiscovery s: sentences){
			if(ids.contains(s.getId().toString()))
				result.add(s);
		}
		return result;
	}
	
	
	public List<String> getEdgeNamesByTokens(List<String> tokens) {
		Datastore datastore =  datastoreProvider.getDefaultDb();
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

	public List<SentenceDb> getSentencesForReprocess(SentenceReprocessingInput input) {
		Query<SentenceDb> query =   datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
		 query
		 .search(input.getToken())
		 .field("organizationId").equal(input.getOrganizationId())
		 .field("reprocessId").notEqual(input.getReprocessId())
		 .limit(input.getTakeSize());
		 return query.asList();
	}
	
	
	public List<SentenceDb> getSentencesByDiscreteDataIds(Set<String> ids){
		init();
		List<DiscreteData> discreteData = discreteDataDao.getByIds(ids);
		if(discreteData.isEmpty()) return new ArrayList<SentenceDb>();
		Query<SentenceDb> query =   datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
		 query
		 .field("discreteData").hasAnyOf(discreteData);
		 return query.asList();
	}


  } 