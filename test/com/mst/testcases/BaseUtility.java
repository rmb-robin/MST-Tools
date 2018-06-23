package com.mst.testcases;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.SentenceQuery.*;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mst.model.metadataTypes.EdgeNames.*;

class BaseUtility {
    private String orgId;
    private SentenceQueryDao sentenceQueryDao;
    private SentenceProcessingController controller;

    BaseUtility() {
        final String SERVER = "10.0.129.218";
        final String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(provider);
        controller = new SentenceProcessingControllerImpl();
        controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
    }

    void setOrgId(String orgId) {
        this.orgId = orgId;
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
        sentence.setNeedResult(false);
        return sentence;
    }

    List<SentenceQueryResult> getResults(SentenceQueryInput input, SentenceTextRequest request) {
        List<SentenceDb> sentenceDbs = new ArrayList<>();
        try {
            SentenceProcessingResult result = controller.processText(request);
            List<Sentence> sentences = result.getSentences();
            for (Sentence sentence : sentences) {
                SentenceDb sentenceDb = SentenceConverter.convertToSentenceDb(sentence, true);
                sentenceDbs.add(sentenceDb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sentenceQueryDao.getSentences(input, sentenceDbs);
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
}
