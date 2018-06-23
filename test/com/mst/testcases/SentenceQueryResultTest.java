package com.mst.testcases;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.List;

import static com.mst.model.metadataTypes.Descriptor.*;
import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static com.mst.model.metadataTypes.MeasurementClassification.LARGEST;
import static org.junit.Assert.*;

public class SentenceQueryResultTest {
    private SentenceQueryDao sentenceQueryDao;
    private final String AUTOMATED_TEST_ID = "5b072a6a18b65e3ace2edcdf";

    public SentenceQueryResultTest() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault mongoProvider = new MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);
    }

    @Test
    public void testQueryNoMeasurement() {
        List<SentenceQueryInput> inputs = null; //TODO use BaseUtility
        assertNotNull(inputs);
        assertEquals(2, inputs.size());
        SentenceQueryInput input = inputs.get(0);
        assertEquals(1, input.getSentenceQueryInstances().size());
        assertEquals(2, input.getSentenceQueryInstances().get(0).getEdges().size());

        assertEquals(AUTOMATED_TEST_ID, input.getOrganizationId());
        List<SentenceQueryInstance> instances = input.getSentenceQueryInstances();
        assertNotNull(instances);
        assertEquals(1, instances.size());

        List<SentenceQueryResult> results = sentenceQueryDao.getSentences(input);
        assertNotNull(results);
        assertEquals(2, results.size());

        String normalizedSentence = "cyst in left ovary measures 3cm"; //TODO '3cm' should be exist as 1 word in the normalized sentence
        SentenceQueryResult result = results.get(0);
        assertEquals(normalizedSentence, result.getSentence());
        List<SentenceQueryEdgeResult> edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        int measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                assertEquals(X_AXIS, edge.getDescriptor());
                assertEquals(String.valueOf(3), edge.getMatchedValue());
                measurementEdgeCount++;
            }
        //TODO if measurement is not in the query, measurement edges should be marked as displayEdge = false
        assertEquals(1, measurementEdgeCount);

        normalizedSentence = "cyst in right ovary measures 4x5x3 cm";
        result = results.get(1);
        assertEquals(normalizedSentence, result.getSentence());
        edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                switch (Integer.parseInt(edge.getMatchedValue())) {
                    case 4:
                        assertEquals(X_AXIS, edge.getDescriptor());
                        break;
                    case 5:
                        assertEquals(Y_AXIS, edge.getDescriptor());
                        break;
                    case 3:
                        assertEquals(Z_AXIS, edge.getDescriptor());
                }
                measurementEdgeCount++;
            }
        assertEquals(3, measurementEdgeCount);
    }

    @Test
    public void testQueryLargestMeasurement() {
        List<SentenceQueryInput> inputs = null; //TODO use BaseUtility
        assertNotNull(inputs);
        assertEquals(2, inputs.size());
        SentenceQueryInput input = inputs.get(1);
        assertEquals(1, input.getSentenceQueryInstances().size());
        SentenceQueryInstance instance = input.getSentenceQueryInstances().get(0);
        assertEquals(3, instance.getEdges().size());
        assertEquals(LARGEST, instance.getMeasurementClassification());

        assertEquals(AUTOMATED_TEST_ID, input.getOrganizationId());
        List<SentenceQueryInstance> instances = input.getSentenceQueryInstances();
        assertNotNull(instances);
        assertEquals(1, instances.size());

        List<SentenceQueryResult> results = sentenceQueryDao.getSentences(input);
        assertNotNull(results);
        assertEquals(2, results.size()); //FAIL actual = 0

        String normalizedSentence = "cyst in left ovary measures 3cm"; //TODO '3cm' should be exist as 1 word in the normalized sentence
        SentenceQueryResult result = results.get(0);
        assertEquals(normalizedSentence, result.getSentence());
        List<SentenceQueryEdgeResult> edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        int measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                assertEquals(X_AXIS, edge.getDescriptor());
                assertEquals(String.valueOf(3), edge.getMatchedValue());
                measurementEdgeCount++;
            }
        assertEquals(1, measurementEdgeCount);

        normalizedSentence = "cyst in right ovary measures 4x5x3 cm";
        result = results.get(1);
        assertEquals(normalizedSentence, result.getSentence());
        edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                switch (Integer.parseInt(edge.getMatchedValue())) {
                    case 4:
                        assertEquals(X_AXIS, edge.getDescriptor());
                        break;
                    case 5:
                        assertEquals(Y_AXIS, edge.getDescriptor());
                        break;
                    case 3:
                        assertEquals(Z_AXIS, edge.getDescriptor());
                }
                measurementEdgeCount++;
            }
        assertEquals(3, measurementEdgeCount);
    }
}
