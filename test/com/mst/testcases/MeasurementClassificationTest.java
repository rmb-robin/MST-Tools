package com.mst.testcases;

import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.requests.SentenceTextRequest;
import org.junit.Test;

import java.util.*;

import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MeasurementClassificationTest {
    private BaseUtility baseUtility;

    public MeasurementClassificationTest() {
        baseUtility = new BaseUtility();
    }

    @Test
    public void testLargest() {
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", LARGEST);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, LARGEST,"5.3");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSmallest() {
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", SMALLEST);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SMALLEST,"1.1");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testMedian() {
        baseUtility.setOrgId("5b072a6a18b65e3ace2edcdf");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.", 34, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, MEDIAN,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testMean() {
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 5 cm x 2 cm x 2 cm.", 33, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);  List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, MEAN,"3.0");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }
}
