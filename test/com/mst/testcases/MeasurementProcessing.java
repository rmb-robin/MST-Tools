package com.mst.testcases;

import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.requests.SentenceRequest;
import org.junit.Test;

import java.util.List;

import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static org.junit.Assert.*;

public class MeasurementProcessing {
    private BaseUtility baseUtility;

    public MeasurementProcessing() {
        baseUtility = new BaseUtility();
    }

    @Test
    public void testLargest() {
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", LARGEST);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.",true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
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
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.", true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
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
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.",true,34, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
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
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 5 cm x 2 cm x 2 cm.",true,33, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, MEAN,"3.0");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRule0() {        // rule 0 two measurements (AP and Transverse)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm ap.",true,35, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRuleRule1() {        // rule 1 two measurements (AP and Length)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm ap and 3.2 cm in length.",true,35, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"1.1");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRuleRule2() {        // rule 2 two measurements (Transverse and Length)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm in length.",true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"1.1");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRuleRule3() {        // rule 3 three measurements (AP, Transverse, and Length)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm ap, 5.3 cm transverse and 3.2 cm in length.",true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"5.3");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRuleRule4() {        // rule 4 two measurements (x and y)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm x 3.2 cm.",true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testSecondLargestRuleRule5() {        // rule 5 three measurements (x, y, z)
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceRequest request = baseUtility.getSentenceRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.",true,37, "F");
        List<SentenceQueryResult> results = baseUtility.getQueryResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }
}
