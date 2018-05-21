package com.mst.testcases;

import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryEdgeResult;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;
import test.SentenceQueryInputDao;

import java.util.List;

import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static org.junit.Assert.*;

public class SentenceQueryResultTest {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private SentenceQueryDao sentenceQueryDao;
    private SentenceQueryInputDao sentenceQueryInputDao;

    public SentenceQueryResultTest() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault mongoProvider = new MongoDatastoreProviderDefault(SERVER, DATABASE);
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);
        sentenceQueryInputDao = new SentenceQueryInputDao(SentenceQueryInput.class);
        sentenceQueryInputDao.setMongoDatastoreProvider(mongoProvider);
    }

    @Test
    public void testResultWithNoBusinessRulesApplied() {
        SentenceQueryInput input = sentenceQueryInputDao.getInput(ORG_ID);
        assertNotNull(input);
        assertEquals(ORG_ID, input.getOrganizationId());
        assertTrue(input.getDebug());

        List<SentenceQueryResult> results = sentenceQueryDao.getSentences(input);
        assertNotNull(results);
        assertEquals(2, results.size());

        String normalizedSentence = "lesion in right adnexum measuring 3 cm";
        SentenceQueryResult result = results.get(0);
        assertEquals(normalizedSentence, result.getSentence());
        List<SentenceQueryEdgeResult> edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        int measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                measurementEdgeCount++;
            }
        assertEquals(1, measurementEdgeCount);

        normalizedSentence = "lesion in left adnexum measures 3x4x2 cm";
        result = results.get(1);
        assertEquals(normalizedSentence, result.getSentence());
        edges = result.getSentenceQueryEdgeResults();
        assertNotNull(edges);
        assertTrue(!edges.isEmpty());
        measurementEdgeCount = 0;
        for (SentenceQueryEdgeResult edge : edges)
            if (edge.getEdgeName().equals(measurement)) {
                measurementEdgeCount++;
            }
        assertEquals(3, measurementEdgeCount); //failed actual is: 1
    }
}
