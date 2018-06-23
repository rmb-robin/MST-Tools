package com.mst.testcases;


import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.requests.SentenceTextRequest;
import org.junit.Test;

import java.util.*;

import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static org.junit.Assert.*;

public class SecondLargestMeasurementProcessingTest {
    private BaseUtility baseUtility;

    public SecondLargestMeasurementProcessingTest() {
        baseUtility = new BaseUtility();
        baseUtility.setOrgId("5972aedebde4270bc53b23e3");
    }

    @Test
    public void testRule0() {        // rule 0 two measurements (AP and Transverse)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm ap.", 35, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testRule1() {        // rule 1 two measurements (AP and Length)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm ap and 3.2 cm in length.", 35, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"1.1");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testRule2() {        // rule 2 two measurements (Transverse and Length)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm transverse and 3.2 cm in length.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"1.1");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testRule3() {        // rule 3 three measurements (AP, Transverse, and Length)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm ap, 5.3 cm transverse and 3.2 cm in length.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"5.3");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testRule4() {        // rule 4 two measurements (x and y)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm x 3.2 cm.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }

    @Test
    public void testRule5() {        // rule 5 three measurements (x, y, z)
        SentenceQueryInput input = baseUtility.getSentenceQueryInput("cyst", "ovary", "1", "9", MEDIAN);
        SentenceTextRequest request = baseUtility.getSentenceTextRequest("cyst in ovary measures 1.1 cm x 5.3 cm x 3.2 cm.", 37, "F");
        List<SentenceQueryResult> results = baseUtility.getResults(input, request);
        List<SentenceQueryEdgeResult> edges = results.get(0).getSentenceQueryEdgeResults();
        boolean testResult = baseUtility.testResults(edges, measurement, SECOND_LARGEST,"3.2");
        assertNotNull(input);
        assertNotNull(request);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(testResult);
    }
}
