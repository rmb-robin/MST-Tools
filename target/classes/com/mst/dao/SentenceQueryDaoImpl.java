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

import com.mst.filter.CystAndAAAReportFilterImpl;
import com.mst.filter.ITNReportFilterImpl;
import com.mst.filter.ImpressionReportFilterImpl;
import com.mst.filter.ReportFilterByQueryImpl;
import com.mst.filter.ReportFilterException;
import com.mst.filter.ReportQueryFilter;
import com.mst.filter.SentenceDiscoveryFilterImpl;
import com.mst.filter.SentenceFilterControllermpl;
import com.mst.filter.SentenceQueryResultFactory;
import com.mst.filter.TokenSequenceQueryBusinessRuleFilterImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.filter.SentenceDiscoveryFilter;
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
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.graph.Edge;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeResultTypes;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.model.metadataTypes.SemanticTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.Constants;
import com.mst.util.TokenRelationshipUtil;


public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;

	public SentenceFilterControllermpl sentenceFilterController; 
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
		List<SentenceQueryResult> result = getSentences(input, null);
		
		if(input.isFilterByTokenSequence()){
			QueryBusinessRuleDaoImpl queryBusinessRuleDao = new QueryBusinessRuleDaoImpl();
			queryBusinessRuleDao.setMongoDatastoreProvider(this.datastoreProvider);
			QueryBusinessRule rule = queryBusinessRuleDao.get(input.getOrganizationId(),QueryBusinessRuleTypes.tokensequenceexlcude);
			TokenSequenceQueryBusinessRuleFilterImpl queryBusinessRuleFilterImpl = new TokenSequenceQueryBusinessRuleFilterImpl();
			result = queryBusinessRuleFilterImpl.filterByBusinessRule(result, rule);
		}

		if(input.isFilterByReport())
			result = filterResultsByDistinctReport(result,input, this.sentenceFilterController.cumalativeSentenceResults);
		return result;
	}

	public List<SentenceQueryResult> getSentences(SentenceQueryInput input, List<SentenceDb> sentences) {

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
				sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData, sentences));
				continue;
			}
			
			if(sentenceQueryInstance.getAppender()==null) continue;
			String appender = sentenceQueryInstance.getAppender().toLowerCase();
			if(appender.equals("or")){
				sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore,input.getOrganizationId(),discreteDataIds,filterOnDiscreteData, sentences));
				continue;
			}
			
			if(appender.equals("and")){
				sentenceFilterController.filterForAnd(sentenceQueryInstance);
			}
			
			if(appender.equals("andnot") || appender.equals("and not"))
				sentenceFilterController.filterForAndNot(sentenceQueryInstance);
			
			if(appender.equals("andnotall"))
				sentenceFilterController.filterForAndNotAll(sentenceQueryInstance);
		}
		
		return new ArrayList<SentenceQueryResult>(sentenceFilterController.getQueryResults().values());
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

	private SentenceQueryInstanceResult processQueryInstance(SentenceQueryInstance sentenceQueryInstance,
			Datastore datastore, String organizationId, List<DiscreteData> discreteDataIds, boolean filterForDiscrete,
			List<SentenceDb> passedSentences) {
		Map<String, EdgeQuery> edgeQueriesByName = sentenceFilterController
				.convertEdgeQueryToDictionary(sentenceQueryInstance);
		SentenceQueryInstanceResult result = new SentenceQueryInstanceResult();
		this.init();
		for (String token : sentenceQueryInstance.getTokens()) {
			List<SentenceDb> sentences = new ArrayList<SentenceDb>();
			if (passedSentences == null) {
				Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
				query.search(token)
					.field("tokenRelationships.edgeName").hasAllOf(edgeQueriesByName.keySet())
					.field("organizationId").equal(organizationId);

				if (filterForDiscrete)
					query.field("discreteData").hasAnyOf(discreteDataIds);

				query.retrievedFields(true, "id", "tokenRelationships", "normalizedSentence", "origSentence",
						"discreteData");
				sentences = query.asList();
			} else {
				sentences.addAll(filterForEdges(passedSentences, edgeQueriesByName));
			}
			List<SentenceQueryResult> queryResults = sentenceFilterController.getSentenceQueryResults(sentences, token,
					sentenceQueryInstance.getEdges(), token);
			result.getSentenceQueryResult().addAll(queryResults);
			result.getSentences().addAll(getMatchedSentences(queryResults, sentences));
		}
		return result;
	}

	private List<SentenceDb> filterForEdges(List<SentenceDb> sentences, Map<String, EdgeQuery> edgeQueriesByName){
		List<SentenceDb> result = new ArrayList<>();
		Set<String> keys = edgeQueriesByName.keySet();
		for(SentenceDb sentence: sentences){
			boolean isMatch = true;
			Map<String, List<TokenRelationship>> edgesByName = TokenRelationshipUtil.getMapByEdgeName(sentence.getTokenRelationships(),false);
			for(String edgeName: keys){
				if(!edgesByName.containsKey(edgeName)){
					isMatch = false; 
					break;
					
				}	
			}
			
			if(isMatch)result.add(sentence);	
		}
		return result;

	}
	
	private List<SentenceDb> getMatchedSentences(List<SentenceQueryResult> queryResults, List<SentenceDb> sentences){
		HashSet<String> ids = new HashSet<>();
		for(SentenceQueryResult q: queryResults){
			ids.add(q.getSentenceId());
		}
		List<SentenceDb> result = new ArrayList<>();
		for(SentenceDb s: sentences){
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
	
	public List<SentenceDb> getSentencesForDiscreteDataId(String id){
		Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
		query.disableValidation();
		query.field("discreteData.$id").equal(new ObjectId(id));
		 query.retrievedFields(true, "origSentence");
		return query.asList();
	}
	
	
	private List<SentenceQueryResult> getMatchesByDiscreateId(List<SentenceQueryResult> results, String id) {
		return results.parallelStream().filter((result) -> result.getDiscreteData().getId().toString().equals(id))
				.collect(Collectors.toList());
	}
	
	private List<SentenceQueryResult> filterResultsByDistinctReport(List<SentenceQueryResult> results, SentenceQueryInput input,
			Map<String, SentenceDb> sentenceCache) {

		List<String> ids = results.stream().map((result) -> result.getDiscreteData().getId().toString()).distinct()
				.collect(Collectors.toList());

		List<SentenceQueryResult> result = new ArrayList<>();
		for (String id : ids) {

			List<SentenceQueryResult> idResults = getMatchesByDiscreateId(results, id);
			if ( idResults.size() <= 1 ) {
				
				if(idResults.size()==1)
					result.add(idResults.get(0));
				continue;
			}

			List<SentenceDb> sentences = idResults.parallelStream()
					.map(idResult -> sentenceCache.get(idResult.getSentenceId())).filter(s -> s != null).distinct()
					.collect(Collectors.toList());

		
			List<ReportQueryFilter> filters = new ArrayList<ReportQueryFilter>();
			filters.add(new ITNReportFilterImpl(input, sentenceCache));
			filters.add(new ImpressionReportFilterImpl(input, sentenceCache));
			filters.add(new CystAndAAAReportFilterImpl(input, sentenceCache));
			filters.add(new ReportFilterByQueryImpl(input, sentenceCache));
						
			for (ReportQueryFilter filter : filters) {
				List<SentenceQueryResult> filterResult = filter.build(this, results, sentences, input);
				if(filterResult.size()!= 0){
					result.add(filterResult.get(0));
					break;
				}

//				if (filter.qualifingFilter()) {
//					int rowsFiltered;
//					try {
//						rowsFiltered = filter.process(results);
//
//					} catch (ReportFilterException e) {
//						e.printStackTrace();
//					}
//				
//				} else {
//						System.out.println("  Filter does not qualify: " + filter.getClass().getName());
//				}
			}

		}
	
		return result;
		
	}
	
	
	
	
  }  