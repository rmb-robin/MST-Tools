package com.mst.dao;

import java.util.*;
import java.util.stream.Collectors;

import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.BusinessRule;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.filter.CystAndAAAReportFilterImpl;
import com.mst.filter.ITNReportFilterImpl;
import com.mst.filter.ImpressionReportFilterImpl;
import com.mst.filter.ReportFilterByQueryImpl;
import com.mst.filter.ReportQueryFilter;
import com.mst.filter.SentenceFilterControllerImpl;
import com.mst.filter.TokenSequenceQueryBusinessRuleFilterImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryInstanceResult;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.TokenRelationshipUtil;

import static com.mst.model.businessRule.BusinessRule.RuleType.SENTENCE_PROCESSING;
import static com.mst.model.metadataTypes.EdgeNames.measurement;

//TODO replace deprecated calls
public class SentenceQueryDaoImpl implements SentenceQueryDao {
    private MongoDatastoreProvider datastoreProvider;
    private SentenceFilterControllerImpl sentenceFilterController;
    private DiscreteDataDao discreteDataDao;
    private BusinessRuleDao businessRuleDao;
    private List<BusinessRule> businessRules;

    @Override
    public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
        datastoreProvider = provider;
        init();
    }

    public List<SentenceQueryResult> getSentences(SentenceQueryInput input) {
        List<SentenceQueryResult> result = getSentences(input, null);
        if (input.isFilterByTokenSequence()) {
            QueryBusinessRuleDaoImpl queryBusinessRuleDao = new QueryBusinessRuleDaoImpl();
            queryBusinessRuleDao.setMongoDatastoreProvider(datastoreProvider);
            QueryBusinessRule rule = queryBusinessRuleDao.get(input.getOrganizationId(), QueryBusinessRuleTypes.tokensequenceexlcude);
            TokenSequenceQueryBusinessRuleFilterImpl queryBusinessRuleFilterImpl = new TokenSequenceQueryBusinessRuleFilterImpl();
            result = queryBusinessRuleFilterImpl.filterByBusinessRule(result, rule);
        }
        if (input.isFilterByReport())
            result = filterResultsByDistinctReport(result, input, sentenceFilterController.getCumulativeSentenceResults());
        return result;
    }

    public List<SentenceQueryResult> getSentences(SentenceQueryInput input, List<SentenceDb> sentences) {
        //TODO pass business rules to sentenceFilterController
        businessRules = businessRuleDao.get(input.getOrganizationId(), SENTENCE_PROCESSING);



        sentenceFilterController = new SentenceFilterControllerImpl();
        Datastore datastore = datastoreProvider.getDefaultDb();
        boolean filterOnDiscreteData = false;
        List<DiscreteData> discreteDataIds = null;
        if (input.getDiscreteDataFilter() != null && !input.getDiscreteDataFilter().isEmpty()) {
            filterOnDiscreteData = true;
            init();
            discreteDataIds = getDiscreteData(input);
        }
        for (int i = 0; i < input.getSentenceQueryInstances().size(); i++) {
            SentenceQueryInstance sentenceQueryInstance = input.getSentenceQueryInstances().get(i);
            if (i == 0) {
                sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore, input.getOrganizationId(), discreteDataIds, filterOnDiscreteData, sentences));
                continue;
            }
            if (sentenceQueryInstance.getAppender() == null)
                continue;
            switch (sentenceQueryInstance.getAppender().toLowerCase()) {
                case "or":
                    sentenceFilterController.addSentencesToResult(processQueryInstance(sentenceQueryInstance, datastore, input.getOrganizationId(), discreteDataIds, filterOnDiscreteData, sentences));
                    break;
                case "and":
                    sentenceFilterController.filterForAnd(sentenceQueryInstance, businessRules);
                    break;
                case "andnot":
                case "and not":
                    sentenceFilterController.filterForAndNot(sentenceQueryInstance, businessRules);
                    break;
                case "andnotall":
                    sentenceFilterController.filterForAndNotAll(sentenceQueryInstance);
                    break;
            }
        }
        return new ArrayList<>(sentenceFilterController.getQueryResults().values());
    }

    public List<String> getEdgeNamesByTokens(List<String> tokens) {
        Datastore datastore = datastoreProvider.getDefaultDb();
        HashSet<String> edgeNames = new HashSet<>();
        for (String token : tokens) {
            Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
            query.search(token).retrievedFields(true, "tokenRelationships");
            List<SentenceDb> sentences = query.asList();
            edgeNames.addAll(getEdgeNamesForSentences(sentences));
        }
        return new ArrayList<>(edgeNames);
    }

    public List<SentenceDb> getSentencesForReprocess(SentenceReprocessingInput input) {
        Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
        query.search(input.getToken()).field("organizationId").equal(input.getOrganizationId()).field("reprocessId").notEqual(input.getReprocessId()).limit(input.getTakeSize());
        return query.asList();
    }

    public List<SentenceDb> getSentencesByDiscreteDataIds(Set<String> ids) {
        init();
        List<DiscreteData> discreteData = discreteDataDao.getByIds(ids);
        if (discreteData.isEmpty())
            return new ArrayList<>();
        Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
        query.field("discreteData").hasAnyOf(discreteData);
        return query.asList();
    }

    public List<SentenceDb> getSentencesForDiscreteDataId(String id) {
        Query<SentenceDb> query = datastoreProvider.getDefaultDb().createQuery(SentenceDb.class);
        query.disableValidation();
        query.field("discreteData.$id").equal(new ObjectId(id));
        query.retrievedFields(true, "origSentence");
        return query.asList();
    }

    private void init() {
        discreteDataDao = new DiscreteDataDaoImpl();
        discreteDataDao.setMongoDatastoreProvider(datastoreProvider);
        businessRuleDao = new BusinessRuleDaoImpl(BusinessRule.class);
        businessRuleDao.setMongoDatastoreProvider(datastoreProvider);
    }

    private List<DiscreteData> getDiscreteData(SentenceQueryInput input) {
        return discreteDataDao.getDiscreteDatas(input.getDiscreteDataFilter(), input.getOrganizationId(), false);
    }

    private SentenceQueryInstanceResult processQueryInstance(SentenceQueryInstance sentenceQueryInstance, Datastore datastore, String organizationId, List<DiscreteData> discreteDataIds, boolean filterForDiscrete, List<SentenceDb> passedSentences) {
        //TODO send business rules to sentenceFilterController
        Map<String, EdgeQuery> edgeQueriesByName = sentenceFilterController.convertEdgeQueryToDictionary(sentenceQueryInstance);
        edgeQueriesByName.remove(measurement); //NOTE dysn tokens no longer have a measurement edge
        SentenceQueryInstanceResult result = new SentenceQueryInstanceResult();
        init();
        String measurementClassification = sentenceQueryInstance.getMeasurementClassification();
        for (String token : sentenceQueryInstance.getTokens()) {
            List<SentenceDb> sentences;
            if (passedSentences == null) {
                Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
                query.search(token).field("tokenRelationships.edgeName").hasAllOf(edgeQueriesByName.keySet()).field("organizationId").equal(organizationId);
                if (filterForDiscrete)
                    query.field("discreteData").hasAnyOf(discreteDataIds);
                query.retrievedFields(true, "id", "tokenRelationships", "normalizedSentence", "origSentence", "discreteData");
                sentences = query.asList();
            } else
                sentences = new ArrayList<>(filterForEdges(passedSentences, edgeQueriesByName));
            List<SentenceQueryResult> queryResults = sentenceFilterController.getSentenceQueryResults(sentences, token, sentenceQueryInstance.getEdges(), measurementClassification, businessRules);
            result.getSentenceQueryResult().addAll(queryResults);
            result.getSentences().addAll(getMatchedSentences(queryResults, sentences));
        }
        return result;
    }

    private List<SentenceDb> filterForEdges(List<SentenceDb> sentences, Map<String, EdgeQuery> edgeQueriesByName) {
        List<SentenceDb> result = new ArrayList<>();
        Set<String> keys = edgeQueriesByName.keySet();
        for (SentenceDb sentence : sentences) {
            boolean isMatch = true;
            Map<String, List<TokenRelationship>> edgesByName = TokenRelationshipUtil.getMapByEdgeName(sentence.getTokenRelationships(), false);
            for (String edgeName : keys) {
                if (!edgesByName.containsKey(edgeName)) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch)
                result.add(sentence);
        }
        return result;
    }

    private List<SentenceDb> getMatchedSentences(List<SentenceQueryResult> queryResults, List<SentenceDb> sentences) {
        HashSet<String> ids = new HashSet<>();
        for (SentenceQueryResult q : queryResults)
            ids.add(q.getSentenceId());
        List<SentenceDb> result = new ArrayList<>();
        for (SentenceDb s : sentences)
            if (ids.contains(s.getId().toString()))
                result.add(s);
        return result;
    }

    private HashSet<String> getEdgeNamesForSentences(List<SentenceDb> sentences) {
        HashSet<String> result = new HashSet<>();
        for (SentenceDb sentence : sentences) {
            if (sentence.getTokenRelationships() == null)
                continue;
            sentence.getTokenRelationships().forEach(a -> result.add(a.getEdgeName()));
        }
        return result;
    }

    private List<SentenceQueryResult> getMatchesByDiscreteId(List<SentenceQueryResult> results, String id) {
        return results.parallelStream().filter((result) -> result.getDiscreteData().getId().toString().equals(id)).collect(Collectors.toList());
    }

    private List<SentenceQueryResult> filterResultsByDistinctReport(List<SentenceQueryResult> results, SentenceQueryInput input, Map<String, SentenceDb> sentenceCache) {
        List<String> ids = results.stream().map((result) -> result.getDiscreteData().getId().toString()).distinct().collect(Collectors.toList());
        List<SentenceQueryResult> result = new ArrayList<>();
        for (String id : ids) {
            List<SentenceQueryResult> idResults = getMatchesByDiscreteId(results, id);
            if (idResults.size() <= 1) {
                if (idResults.size() == 1)
                    result.add(idResults.get(0));
                continue;
            }
            List<SentenceDb> sentences = idResults.parallelStream().map(idResult -> sentenceCache.get(idResult.getSentenceId())).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<ReportQueryFilter> filters = new ArrayList<>();
            filters.add(new ITNReportFilterImpl(input, sentenceCache));
            filters.add(new ImpressionReportFilterImpl(input, sentenceCache));
            filters.add(new CystAndAAAReportFilterImpl(input, sentenceCache));
            filters.add(new ReportFilterByQueryImpl(input, sentenceCache));
            for (ReportQueryFilter filter : filters) {
                List<SentenceQueryResult> filterResult = filter.build(this, results, sentences, input);
                if (filterResult.size() != 0) {
                    result.add(filterResult.get(0));
                    break;
                }
            }
        }
        return result;
    }
}