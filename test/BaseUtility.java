import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.filter.SentenceFilterControllerImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.discrete.ComplianceResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.*;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

import static com.mst.model.businessRule.BusinessRule.RuleType.SENTENCE_PROCESSING;
import static com.mst.model.metadataTypes.EdgeNames.*;

class BaseUtility {
    private String orgId;
    private SentenceQueryDao sentenceQueryDao;
    private BusinessRuleDao businessRuleDao;
    private SentenceProcessingController sentenceProcessingController;
    private SentenceFilterControllerImpl sentenceFilterController;
    private List<BusinessRule> sentenceProcessingRules;

    BaseUtility() {
        final String SERVER = "10.0.129.218";
        final String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(provider);
        businessRuleDao = new BusinessRuleDaoImpl(BusinessRule.class);
        businessRuleDao.setMongoDatastoreProvider(provider);
        sentenceProcessingController = new SentenceProcessingControllerImpl();
        sentenceProcessingController.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
        sentenceFilterController = new SentenceFilterControllerImpl();
    }

    void setOrgId(String orgId) {
        this.orgId = orgId;
        sentenceProcessingRules = businessRuleDao.get(orgId, SENTENCE_PROCESSING);
    }

    List<WordToken> getWordTokens(String text, boolean convertMeasurements) {
        List<WordToken> wordTokens = new ArrayList<>();
        try {
            SentenceRequest request = new SentenceRequest();
            request.setConvertMeasurements(convertMeasurements);
            List<String> input = Arrays.asList(text.split(Pattern.quote(". ")));
            request.setSentenceTexts(input);
            List<Sentence> sentences = sentenceProcessingController.processSentences(request);
            for (Sentence sentence : sentences)
                wordTokens.addAll(sentence.getModifiedWordList());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return wordTokens;
    }

    List<TokenRelationship> getTokenRelationships(String text, boolean convertMeasurements) {
        List<TokenRelationship> results = new ArrayList<>();
        try {
            SentenceRequest request = new SentenceRequest();
            request.setConvertMeasurements(convertMeasurements);
            List<String> input = Arrays.asList(text.split(Pattern.quote(". ")));
            request.setSentenceTexts(input);
            List<Sentence> sentences = sentenceProcessingController.processSentences(request);
            for (Sentence sentence : sentences) {
                List<TokenRelationship> tokenRelationships = sentence.getTokenRelationships();
                for (TokenRelationship tokenRelationship : tokenRelationships)
                    if (!tokenRelationship.getEdgeName().equals(""))
                        results.add(tokenRelationship);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    SentenceQueryInput getSentenceQueryInput(String token, String diseaseLocation, String minRange, String maxRange, String measurementClassification) {
        SentenceQueryInput input = new SentenceQueryInput();
        input.setNotAndAll(false);
        List<SentenceQueryInstance> instances = new ArrayList<>();
        SentenceQueryInstance instance = new SentenceQueryInstance();
        instance.setTokens(new ArrayList<>(Collections.singletonList(token)));
        List<EdgeQuery> edges = new ArrayList<>();
        EdgeQuery edge = new EdgeQuery();
        edge.setName(existence);
        edge.setIncludeValues(false);
        edge.setIsNamedEdge(false);
        edges.add(edge);
        edge = new EdgeQuery();
        edge.setName(EdgeNames.diseaseLocation);
        edge.setValues(new HashSet<>(Collections.singletonList(diseaseLocation)));
        edge.setIncludeValues(false);
        edge.setIsNamedEdge(false);
        edges.add(edge);
        if (minRange != null && maxRange != null && measurementClassification != null) {
            edge = new EdgeQuery();
            edge.setName(measurement);
            edge.setValues(new HashSet<>(Arrays.asList(minRange, maxRange)));
            edge.setIncludeValues(false);
            edge.setIsNamedEdge(false);
            edges.add(edge);
        }
        instance.setEdges(edges);
        if (minRange != null && maxRange != null && measurementClassification != null)
            instance.setMeasurementClassification(measurementClassification);
        instance.setIsSt(false);
        instance.setExcludeTokenSequence(false);
        instances.add(instance);
        input.setSentenceQueryInstances(instances);
        input.setOrganizationId(orgId);
        input.setDiscreteDataFilters(new DiscreteDataFilter());
        input.setDebug(false);
        input.setFilterByReport(true);
        input.setFilterByTokenSequence(true);
        return input;
    }

    SentenceRequest getSentenceRequest(String text, boolean convertMeasurements, int age, String sex) {
        SentenceRequest sentenceRequest = new SentenceRequest();
        sentenceRequest.setConvertMeasurements(convertMeasurements);
        List<String> input = Arrays.asList(text.split(Pattern.quote(". ")));
        sentenceRequest.setSentenceTexts(input);
        sentenceRequest.setSource(this.getClass().getSimpleName());
        DiscreteData discreteData = new DiscreteData();
        discreteData.setId(new ObjectId());
        discreteData.setPatientAge(age);
        discreteData.setSex(sex);
        discreteData.setOrganizationId(orgId);
        sentenceRequest.setDiscreteData(discreteData);
        return sentenceRequest;
    }

    SentenceTextRequest getSentenceTextRequest(String text, int age, String sex) {
        SentenceTextRequest sentence = new SentenceTextRequest();
        sentence.setText(text);
        sentence.setSource(this.getClass().getSimpleName());
        DiscreteData discreteData = new DiscreteData();
        discreteData.setId(new ObjectId());
        discreteData.setPatientAge(age);
        discreteData.setSex(sex);
        discreteData.setOrganizationId(orgId);
        sentence.setDiscreteData(discreteData);
        sentence.setConvertMeasurements(true);
        return sentence;
    }

    List<SentenceQueryResult> getQueryResults(SentenceQueryInput input, SentenceRequest request) {
        List<Sentence> sentences = getSentences(request);
        List<SentenceDb> convertedToSentenceDb = new ArrayList<>();
        for (Sentence sentence : sentences)
            convertedToSentenceDb.add(SentenceConverter.convertToSentenceDb(sentence, true));
        return sentenceQueryDao.getSentences(input, convertedToSentenceDb);
    }

    boolean isCompliant(SentenceRequest request, String bucketName) {
        DiscreteData discreteData = request.getDiscreteData();
        List<Sentence> sentences = getSentences(request);
        List<SentenceDb> convertedToSentenceDb = new ArrayList<>();
        for (Sentence sentence : sentences) {
            sentence.setDiscreteData(discreteData);
            convertedToSentenceDb.add(SentenceConverter.convertToSentenceDb(sentence, true));
        }
        sentenceFilterController.processCompliance(convertedToSentenceDb, sentenceProcessingRules, false);
        Map<DiscreteData, ComplianceResult> complianceResults = sentenceFilterController.getComplianceResults();
        Map.Entry<DiscreteData, ComplianceResult> entry = complianceResults.entrySet().iterator().next();
        Map<String, Boolean> bucketCompliance = entry.getValue().getBucketCompliance();
        return bucketCompliance.getOrDefault(bucketName, false);
    }

    boolean testResults(List<SentenceQueryEdgeResult> edges, String edgeName, String descriptor, String value) {
        for (SentenceQueryEdgeResult edge : edges) {
            String name = edge.getEdgeName();
            String edgeDescriptor = edge.getDescriptor();
            String edgeValue = edge.getMatchedValue();
            if (name.equals(edgeName) && (edgeDescriptor == null || edgeDescriptor.equals(descriptor)) && edgeValue.equals(value))
                return true;
        }
        return false;
    }

    List<Sentence> getSentences(SentenceRequest request) {
        List<Sentence> sentences = new ArrayList<>();
        try {
            sentences = sentenceProcessingController.processSentences(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sentences;
    }

    List<Sentence> getSentences(SentenceTextRequest request) {
        List<Sentence> sentences = new ArrayList<>();
        try {
            SentenceProcessingResult sentenceProcessingResult = sentenceProcessingController.processText(request);
            sentences = sentenceProcessingResult.getSentences();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sentences;
    }
}
