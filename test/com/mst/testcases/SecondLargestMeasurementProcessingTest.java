package com.mst.testcases;


import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;
import org.bson.types.ObjectId;
import org.junit.Test;
import test.SaveSentenceTextRequest;

import java.util.*;

import static com.mst.model.metadataTypes.EdgeNames.diseaseLocation;
import static com.mst.model.metadataTypes.EdgeNames.existence;
import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static com.mst.model.metadataTypes.MeasurementClassification.MEDIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SecondLargestMeasurementProcessingTest {
    private final String AUTOMATED_TEST_ID = "5b072a6a18b65e3ace2edcdf";
    private SentenceQueryDao sentenceQueryDao;
    private SentenceProcessingController controller;

    public SecondLargestMeasurementProcessingTest() {
        final String SERVER = "10.0.129.218";
        final String DATABASE = "automatedTesting";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(provider);
        controller = new SentenceProcessingControllerImpl();
        controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
    }

    @Test
    public void testRule0() {        // rule 0 two measurements (AP and Transverse)
        SentenceQueryInput input = getInput("1", "9", MEDIAN);
        assertNotNull(input);
        String text = "cyst in ovary measures 1.1 cm transverse and 3.2 cm ap.";
        SentenceTextRequest request = getSentenceTextRequest(text, 37, "F");
        new SaveSentenceTextRequest().process(request);
        List<SentenceDb> documents = new ArrayList<>();
        try {
            SentenceProcessingResult result = controller.processText(request);
            List<Sentence> sentences = result.getSentences();
            for (Sentence sentence : sentences) {
                SentenceDb document = SentenceConverter.convertToDocument(sentence);
                documents.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<SentenceQueryResult> results = sentenceQueryDao.getSentences(input, documents);
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void testRule1() {        // rule 1 two measurements (AP and Length)

    }

    @Test
    public void testRule2() {        // rule 2 two measurements (Transverse and Length)

    }

    @Test
    public void testRule3() {        // rule 3 three measurements (AP, Transverse, and Length)

    }

    @Test
    public void testRule4() {        // rule 4 two measurements (x and y)

    }

    @Test
    public void testRule5() {        // rule 5 three measurements (x, y, z)

    }

    private SentenceTextRequest getSentenceTextRequest(String text, int patientAge, String patientSex) {
        SentenceTextRequest sentence = new SentenceTextRequest();
        sentence.setText(text);
        sentence.setId(new ObjectId());
        sentence.setSource(this.getClass().getSimpleName());
        DiscreteData discreteData = new DiscreteData();
        discreteData.setId(new ObjectId());
        discreteData.setPatientAge(patientAge);
        discreteData.setSex(patientSex);
        discreteData.setOrganizationId(AUTOMATED_TEST_ID);
        sentence.setDiscreteData(discreteData);
        sentence.setConvertMeasurements(true);
        sentence.setNeedResult(false);
        return sentence;
    }

    private SentenceQueryInput getInput(String minRange, String maxRange, String measurementClassification) {
        SentenceQueryInput input = new SentenceQueryInput();
        input.setNotAndAll(false);
        List<SentenceQueryInstance> instances = new ArrayList<>();
        SentenceQueryInstance instance = new SentenceQueryInstance();
        instance.setTokens(new ArrayList<>(Collections.singletonList("cyst")));
        List<EdgeQuery> edges = new ArrayList<>();
        EdgeQuery edge = new EdgeQuery();
        edge.setName(existence);
        edge.setIncludeValues(false);
        edge.setIsNamedEdge(false);
        edges.add(edge);
        edge = new EdgeQuery();
        edge.setName(diseaseLocation);
        edge.setValues(new HashSet<>(Collections.singletonList("ovary")));
        edge.setIncludeValues(false);
        edge.setIsNamedEdge(false);
        edges.add(edge);
        edge = new EdgeQuery();
        edge.setName(measurement);
        edge.setValues(new HashSet<>(Arrays.asList(minRange, maxRange)));
        edge.setIncludeValues(false);
        edge.setIsNamedEdge(false);
        edges.add(edge);
        instance.setEdges(edges);
        instance.setMeasurementClassification(measurementClassification);
        instance.setIsSt(false);
        instance.setExcludeTokenSequence(false);
        instances.add(instance);
        input.setSentenceQueryInstances(instances);
        input.setOrganizationId(AUTOMATED_TEST_ID);
        input.setDebug(false);
        input.setFilterByReport(true);
        input.setFilterByTokenSequence(true);
        return input;
    }
}
