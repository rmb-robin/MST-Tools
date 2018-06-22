package com.mst.testcases;


import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.SentenceQuery.*;
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

import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static org.junit.Assert.*;

public class SecondLargestMeasurementProcessingTest {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private SentenceQueryDao sentenceQueryDao;
    private SentenceProcessingController controller;
    private SentenceQueryInput input;
    private List<SentenceQueryResult> results;

    public SecondLargestMeasurementProcessingTest() {
        final String SERVER = "10.0.129.218";
        final String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(provider);
        controller = new SentenceProcessingControllerImpl();
        controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
    }

    @Test
    public void testRule0() {        // rule 0 two measurements (AP and Transverse)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm ap."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("3.2"));
    }

    @Test
    public void testRule1() {        // rule 1 two measurements (AP and Length)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm ap and 3.2 cm in length."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("1.1"));
    }

    @Test
    public void testRule2() {        // rule 2 two measurements (Transverse and Length)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm in length."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("1.1"));
    }

    @Test
    public void testRule3() {        // rule 3 three measurements (AP, Transverse, and Length)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm ap, 5.3 cm transverse and 3.2 cm in length."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("5.3"));
    }

    @Test
    public void testRule4() {        // rule 4 two measurements (x and y)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm x 3.2 cm."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("3.2"));
    }

    @Test
    public void testRule5() {        // rule 5 three measurements (x, y, z)
        input = getInput("1", "9");
        assertNotNull(input);
        results = getResults(input, getSentenceTextRequest("cyst in ovary measures 1.1 cm x 5.3 cm x and 3.2 cm."));
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResults("3.2"));
    }

    private boolean testResults(String value) {
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        for (SentenceQueryEdgeResult edge : edges) {
            String edgeName = edge.getEdgeName();
            assertNotNull(edgeName);
            String edgeDescriptor = edge.getDescriptor();
            String edgeValue = edge.getMatchedValue();
            assertNotNull(value);
            if (edgeName.equals(measurement) && edgeDescriptor != null && edgeDescriptor.equals(SECOND_LARGEST) && edgeValue.equals(value))
                return true;
        }
        return false;
    }

    private List<SentenceQueryResult> getResults(SentenceQueryInput input, SentenceTextRequest request) {
        new SaveSentenceTextRequest().process(request);
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

    private SentenceTextRequest getSentenceTextRequest(String text) {
        SentenceTextRequest sentence = new SentenceTextRequest();
        sentence.setText(text);
        sentence.setSource(this.getClass().getSimpleName());
        DiscreteData discreteData = new DiscreteData();
        discreteData.setId(new ObjectId());
        discreteData.setPatientAge(37);
        discreteData.setSex("F");
        discreteData.setOrganizationId(ORG_ID);
        sentence.setDiscreteData(discreteData);
        sentence.setConvertMeasurements(true);
        sentence.setNeedResult(false);
        return sentence;
    }

    private SentenceQueryInput getInput(String minRange, String maxRange) {
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
        instance.setMeasurementClassification(MEDIAN);
        instance.setIsSt(false);
        instance.setExcludeTokenSequence(false);
        instances.add(instance);
        input.setSentenceQueryInstances(instances);
        input.setOrganizationId(ORG_ID);
        input.setDiscreteDataFilters(new DiscreteDataFilter());
        input.setDebug(false);
        input.setFilterByReport(true);
        input.setFilterByTokenSequence(true);
        return input;
    }
}
