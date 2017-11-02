package com.mst.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

import com.mst.filter.SentenceFilterControllermpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.filter.SentenceFilterController;
import com.mst.model.SemanticType;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.SentenceQuery.EdgeQuery;
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
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;

	private SentenceFilterController sentenceFilterController; 
	private DiscreteDataDao discreteDataDao; 
	
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
		Datastore datastore =  datastoreProvider.getDataStore();

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
			if(appender.equals("or")){
				sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData));
				continue;
			}
			
			if(appender.equals("and")){
				sentenceFilterController.filterForAnd(sentenceQueryInstance);
			}
			
			if(appender.equals("andnot"))
				sentenceFilterController.filterForAndNot(sentenceQueryInstance);
		}
		return new ArrayList<SentenceQueryResult>(sentenceFilterController.getQueryResults().values());
	}	
	
	@Override
	public List<SentenceQueryResult> getSentencesByText(SentenceQueryTextInput input) {
		List<SentenceQueryResult> result = new ArrayList<SentenceQueryResult>();
		Datastore datastore =  datastoreProvider.getDataStore();
		Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
		 query
		 .field("origSentence").contains(input.getText()) 
		 .field("organizationId").equal(input.getOrganizationId());
		 
		 query.retrievedFields(true, "id", "tokenRelationships", "normalizedSentence","origSentence", "discreteData");
		 List<SentenceDb> sentences = query.asList();
		
		 
		for(SentenceDb db:sentences){
			SentenceQueryResult queryResult = new SentenceQueryResult();
			queryResult.setDiscreteData(db.getDiscreteData());
			queryResult.setSentence(db.getOrigSentence());
			queryResult.setSentenceId(db.getId().toString());
			result.add(queryResult);
		}
		return result;
	}

	private SentenceQueryInstanceResult processQueryInstance(SentenceQueryInstance sentenceQueryInstance,Datastore datastore,String organizationId, List<DiscreteData> discreteDataIds, boolean filterForDiscrete){
		Map<String,EdgeQuery> edgeQueriesByName = sentenceFilterController.convertEdgeQueryToDictionary(sentenceQueryInstance);
		SentenceQueryInstanceResult result = new SentenceQueryInstanceResult();

		this.init();
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
			 
			 
			 result.getSentenceQueryResult().addAll(sentenceFilterController.getSentenceQueryResults(sentences, token,sentenceQueryInstance.getEdges(), token));
		}
		return result;
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

	public List<SentenceDb> getSentencesForReprocess(SentenceReprocessingInput input) {
		Query<SentenceDb> query =   datastoreProvider.getDataStore().createQuery(SentenceDb.class);
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
		Query<SentenceDb> query =   datastoreProvider.getDataStore().createQuery(SentenceDb.class);
		 query
		 .field("discreteData").hasAnyOf(discreteData);
		 return query.asList();
	}


  } 