import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import org.junit.Test;
//import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mst.dao.SentenceCompareSummaryDao;
import com.mst.dao.SentenceDaoImpl;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.IterationDataRule;
import com.mst.model.sentenceProcessing.IterationRuleProcesserInput;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.test.SentenceCompareSummary;
import com.mst.model.test.SentenceEdgeCompare;
import com.mst.model.test.SingleSentenceCompare;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessingHardcodedMetaDataInputFactory;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.MongoDatastoreProviderDefault;
import com.mst.util.RecommandedTokenRelationshipUtil;
import com.mst.util.TokenRelationshipComparer;
import com.mst.util.TokenRelationshipUtil;

//import static org.junit.Assert.*;

public class SentenceDiscoveryTest {

    private class sentenceEdgeCompare {
        public double exactMatch;
        public double discoveryContainsAll;
    }

    // @Test
    public void run() throws Exception {
        SentenceTextRequest request = TestDataProvider.getSentenceTextRequest(createFullPath());
        // request.getDiscreteData().setOrganizationId("58ab6f9f96c2958294a1fdf0");
        List<Sentence> sentences = getSentences(request);
        List<SentenceDiscovery> discoveries = getSentenceDiscovery(request);
        this.assertProcess(sentences, discoveries);
        this.assertSummary(sentences, discoveries);
        this.assertSummaryToMongo(sentences, discoveries);
    }

    // @Test
    public void RunIterationRule() throws Exception {
        // SentenceTextRequest request = getRequest("she has a simple cyst.");
        SentenceTextRequest request = getRequest("left ovary measure 4.7x2.4x2.7 cm in size");
        // SentenceTextRequest request = getRequest("the left ovary measure
        // 4.7x2.4x2.7 cm in size");
        // request.getDiscreteData().setOrganizationId("58ab6f9f96c2958294a1fdf0");
        List<SentenceDiscovery> discoveries = getSentenceDiscovery(request);
        StringBuilder sb = new StringBuilder();
        List<TokenRelationship> r = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(discoveries.get(0).getWordEmbeddings());
        appendEdgesToFile(sb, r, false);
        System.out.println(sb.toString());
        request.setText("There is a dominant follicle within the right ovary measuring 2.1 x 1.7 cm");
        discoveries = getSentenceDiscovery(request);
    }

    private IterationRuleProcesserInput getIterationInput() {
        IterationRuleProcesserInput input = new IterationRuleProcesserInput();
        ArrayList<IterationDataRule> leftRules = new ArrayList<>();
        IterationDataRule rule = getIterationDataRule(WordEmbeddingTypes.verbPlus, 20, WordEmbeddingTypes.verbMinus);
        leftRules.add(rule);
        rule = getIterationDataRule(WordEmbeddingTypes.verbMinus, 30, WordEmbeddingTypes.verbMinus);
        leftRules.add(rule);
        rule = getIterationDataRule(WordEmbeddingTypes.verbMinus, 10, WordEmbeddingTypes.verbMinus);
        leftRules.add(rule);
        input.setLeftRules(leftRules);
        return input;
    }

    private SentenceTextRequest getRequest(String text) {
        SentenceTextRequest request = TestDataProvider.getSentenceTextRequest(createFullPath());
        request.setText(text);
        request.setConvertMeasurements(true);
        return request;
    }

    private IterationDataRule getIterationDataRule(String endRelationship, int points, String startRelationship) {
        IterationDataRule rule = new IterationDataRule();
        rule.setPointValue(points);
        rule.setEdgeNameTolookfor(endRelationship);
        rule.setStartRelationship(startRelationship);
        return rule;
    }

    // @Test
    public void runOldEnvVsNew() throws Exception {
        SentenceTextRequest request = TestDataProvider.getSentenceTextRequest(createFullPath());
        request.getDiscreteData().setOrganizationId("58ab6f9f96c2958294a1fdf0");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(createOutputPath("sentencetextresult.txt")), request);
        List<SentenceDb> oldSentences = processAndGetSentences(request, "http://10.12.128.100:8080/mst-sentence-service/webapi/sentence/savetext", "10.12.128.98", false);
        List<SentenceDb> newSentences = processAndGetSentences(request, "http://10.0.4.163:8080/mst-sentence-service/webapi/sentence/savetext", "10.0.129.219", false);
        assertEdges(oldSentences, newSentences, false);
    }

    private List<SentenceDb> processAndGetSentences(SentenceTextRequest request, String ip, String dbIp, boolean needInsert) throws Exception {
        if (needInsert) {
            String endPoint = ip;
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writeValueAsString(request);
            callPOSTService(endPoint, body);
        }
        SentenceDao dao = new SentenceDaoImpl();
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault();
        MongoConnectionEntity connection = new MongoConnectionEntity();
        connection.setDatabaseName("test");
        connection.setIpAddress(dbIp);
        provider.set(connection);
        dao.setMongoDatastoreProvider(provider);
        return dao.getByOrgId(request.getDiscreteData().getOrganizationId());
    }

    private String callPOSTService(String endpoint, String body) {
        String ret = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter streamWriter = new OutputStreamWriter(conn.getOutputStream());
            streamWriter.write(body);
            streamWriter.flush();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
            String response = null;
            StringBuffer buffer = new StringBuffer();
            while ((response = br.readLine()) != null) {
                buffer.append(response);
            }
            ret = conn.getResponseCode() + "~" + buffer.toString();
        } catch (Exception e) {
            Exception t = e;
            System.out.println(e.getMessage());
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        return ret;
    }

    private List<SentenceDiscovery> getSentenceDiscovery(SentenceTextRequest request) throws Exception {
        SentenceDiscoveryProcessorImpl discoveryProcesser = new SentenceDiscoveryProcessorImpl();
        SentenceProcessingMetaDataInput input = new SentenceDiscoveryProcessingHardcodedMetaDataInputFactory().create();
        discoveryProcesser.setMetadata(input);
        List<SentenceDiscovery> discoveries = discoveryProcesser.process(request);
        return discoveries;
    }

    private List<Sentence> getSentences(SentenceTextRequest request) throws Exception {
        SentenceProcessingControllerImpl sentenceProcesser = new SentenceProcessingControllerImpl();
        sentenceProcesser.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
        return sentenceProcesser.processText(request).getSentences();
    }

    private String createFullPath() {
        return System.getProperty("user.dir") + File.separator + "testData" + File.separator + "Sentence_SentenceDiscoveryTesting" + File.separator + "sentences.txt";
    }

    private String createOutputPath(String fileName) {
        return System.getProperty("user.dir") + File.separator + "testData" + File.separator + "Sentence_SentenceDiscoveryTesting" + File.separator + fileName;
    }

    private void assertProcess(List<Sentence> sentences, List<SentenceDiscovery> discoveries) throws Exception {
        // assertEquals(sentences.size(), discoveries.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            SentenceDiscovery discovery = discoveries.get(i);
            List<TokenRelationship> discoveryEdges = getNamedEdges(discovery);
            sb.append("Sentence : " + sentence.getNormalizedSentence());
            sb.append(System.getProperty("line.separator"));
            sb.append("Discovery: " + discovery.getNormalizedSentence());
            if (TokenRelationshipComparer.areCollectionsSame(sentence.getTokenRelationships(), discoveryEdges, true)) {
                sb.append(System.getProperty("line.separator"));
                sb.append("Edges Match");
                appendEndOfSentenceToFile(sb);
                continue;
            }
            sb.append(System.getProperty("line.separator"));
            sb.append("Sentence Count: " + sentence.getTokenRelationships().size());
            sb.append(System.getProperty("line.separator"));
            appendEdgesToFile(sb, sentence.getTokenRelationships(), false);
            sb.append("Discovery Count: " + discoveryEdges.size());
            sb.append(System.getProperty("line.separator"));
            appendEdgesToFile(sb, discoveryEdges, true);
            appendEndOfSentenceToFile(sb);
        }
        String fileName = createOutputPath("sentencesresult.txt");
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(sb.toString());
        printWriter.close();
    }

    private void sbAppendWithLineBreak(String value, StringBuilder sb) {
        sb.append(value);
        sb.append(System.getProperty("line.separator"));
    }

    private void assertSummary(List<Sentence> sentences, List<SentenceDiscovery> discoveries) throws Exception {
        StringBuilder sb = new StringBuilder();
        sbAppendWithLineBreak("Sentence Count" + sentences.size(), sb);
        List<String> edges = TestDataProvider.readLines(createOutputPath("secondaryedges.txt"));
        edges.add(0, EdgeNames.existence);
        Map<String, sentenceEdgeCompare> edgesByMatchingCount = new HashMap<>();
        for (String edge : edges) {
            edgesByMatchingCount.put(edge, new sentenceEdgeCompare());
        }
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            SentenceDiscovery discovery = discoveries.get(i);
            List<TokenRelationship> discoveryTokenRelationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(discovery.getWordEmbeddings());
            Map<String, List<TokenRelationship>> sentenceEdges = TokenRelationshipUtil.getMapByEdgeName(sentence.getTokenRelationships(), false);
            Map<String, List<TokenRelationship>> discoveryEdges = TokenRelationshipUtil.getMapByEdgeName(discoveryTokenRelationships, true);
            for (Entry<String, sentenceEdgeCompare> entry : edgesByMatchingCount.entrySet()) {
                String edge = entry.getKey();
                if (!sentenceEdges.containsKey(edge) && discoveryEdges.containsKey(edge)) {
                    continue;
                }
                if (sentenceEdges.containsKey(edge) && !discoveryEdges.containsKey(edge)) {
                    continue;
                }
                if (!sentenceEdges.containsKey(edge) && !discoveryEdges.containsKey(edge)) {
                    entry.getValue().exactMatch += 1;
                    continue;
                }
                List<TokenRelationship> sentenceEdgesForEdgeName = sentenceEdges.get(edge);
                List<TokenRelationship> discoveryEdgesForEdgeName = discoveryEdges.get(edge);
                if (TokenRelationshipComparer.areCollectionsSame(sentenceEdgesForEdgeName, discoveryEdgesForEdgeName,
                        true)) {
                    entry.getValue().exactMatch += 1;
                    continue;
                }
                if (TokenRelationshipComparer.doesCollectionB_HaveAllofA(sentenceEdgesForEdgeName,
                        discoveryEdgesForEdgeName)) {
                    entry.getValue().discoveryContainsAll += 1;
                }
            }
        }
        for (Entry<String, sentenceEdgeCompare> entry : edgesByMatchingCount.entrySet()) {
            sbAppendWithLineBreak("Edge Name: " + entry.getKey(), sb);
            sbAppendWithLineBreak("Exact Match: " + entry.getValue().exactMatch, sb);
            sbAppendWithLineBreak("Discovery Contains All Sentence Edges:  " + entry.getValue().discoveryContainsAll, sb);
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        }
        String fileName = createOutputPath("sentenceSummary.txt");
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(sb.toString());
        printWriter.close();
    }

    private void assertSummaryToMongo(List<Sentence> sentences, List<SentenceDiscovery> discoveries) throws Exception {
        StringBuilder sb = new StringBuilder();
        SentenceCompareSummary summary = new SentenceCompareSummary();
        summary.setTotalSentencesProcessed(sentences.size());
        for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            SentenceDiscovery discovery = discoveries.get(i);
            SingleSentenceCompare sentenceCompare = createSingleSentenceCompare(sentence.getNormalizedSentence(), discovery.getNormalizedSentence());
            List<TokenRelationship> discoveryTokenRelationships = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(discovery.getWordEmbeddings());
            List<TokenRelationship> discoveryEdges = getNamedEdges(discovery);
            if (TokenRelationshipComparer.areCollectionsSame(sentence.getTokenRelationships(), discoveryEdges, true)) {
                summary.setTotalSentencesMatched(summary.getTotalSentencesMatched() + 1);
                continue;
            } else {
                summary.setTotalSentenceMismatched(summary.getTotalSentenceMismatched() + 1);
            }
            Map<String, List<TokenRelationship>> sentenceEdgesByEdgeName = TokenRelationshipUtil.getMapByEdgeName(sentence.getTokenRelationships(), false);
            Map<String, List<TokenRelationship>> discoveryEdgesByEdgeName = TokenRelationshipUtil.getMapByEdgeName(discoveryTokenRelationships, true);
            for (Entry<String, List<TokenRelationship>> entry : sentenceEdgesByEdgeName.entrySet()) {
                List<String> sentenceToFroms = getUniqueToFrom(entry.getValue());
                if (!discoveryEdgesByEdgeName.containsKey(entry.getKey())) {
                    SentenceEdgeCompare edgeCompare = createSentenceEdgeCompare(entry.getKey(), sentenceToFroms.size(), 0, sentenceToFroms, null);
                    sentenceCompare.getDismatchedEdges().add(edgeCompare);
                    continue;
                }
                List<TokenRelationship> discoveryRelations = discoveryEdgesByEdgeName.get(entry.getKey());
                List<String> tofroms = getUniqueToFrom(discoveryRelations);
                SentenceEdgeCompare edgeCompare = createSentenceEdgeCompare(entry.getKey(), sentenceToFroms.size(), tofroms.size(), sentenceToFroms, tofroms);
                if (TokenRelationshipComparer.compareCollectionWithSameEdgeName(entry.getValue(), discoveryRelations)) {
                    sentenceCompare.getMatchingEdges().add(edgeCompare);
                } else {
                    sentenceCompare.getDismatchedEdges().add(edgeCompare);
                }
            }
            summary.getMismatchedSentences().add(sentenceCompare);
        }
        SentenceCompareSummaryDao dao = new SentenceCompareSummaryDao();
        dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
        dao.save(summary);
    }

    private List<String> getUniqueToFrom(List<TokenRelationship> tokenRelationship) {
        Map<String, List<TokenRelationship>> map = TokenRelationshipUtil.getMapByToFrom(tokenRelationship);
        return new ArrayList<>(map.keySet());
    }

    private SentenceEdgeCompare createSentenceEdgeCompare(String edgeName, int sentenceAOccurance, int sentenceBOcurrance, List<String> sentenceAToFrom, List<String> sentenceBtoFrom) {
        SentenceEdgeCompare compare = new SentenceEdgeCompare();
        compare.setEdgeName(edgeName);
        compare.setSentenceAOccurance(sentenceAOccurance);
        compare.setSentenceBOccurance(sentenceBOcurrance);
        compare.setSentenceAToFrom(sentenceAToFrom);
        compare.setSentenceBToFrom(sentenceBtoFrom);
        return compare;
    }

    private SingleSentenceCompare createSingleSentenceCompare(String sentenceA, String sentenceB) {
        SingleSentenceCompare compare = new SingleSentenceCompare();
        compare.setSentenceA(sentenceA);
        compare.setSentenceB(sentenceB);
        return compare;
    }

    private void assertEdges(List<SentenceDb> old, List<SentenceDb> newSentences, boolean useSecondAsDiscovery) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < old.size(); i++) {
            SentenceDb sentence = old.get(i);
            SentenceDb discovery = newSentences.get(i);
            sb.append("Sentence : " + sentence.getNormalizedSentence());
            sb.append(System.getProperty("line.separator"));
            sb.append("Sentence (New) : " + discovery.getNormalizedSentence());
            if (TokenRelationshipComparer.areCollectionsSame(sentence.getTokenRelationships(), discovery.getTokenRelationships(), useSecondAsDiscovery)) {
                sb.append(System.getProperty("line.separator"));
                sb.append("Edges Match");
                appendEndOfSentenceToFile(sb);
                continue;
            }
            sb.append(System.getProperty("line.separator"));
            sb.append("Sentence Count: " + sentence.getTokenRelationships().size());
            sb.append(System.getProperty("line.separator"));
            appendEdgesToFile(sb, sentence.getTokenRelationships(), false);
            sb.append("Sentence (New) Count: " + discovery.getTokenRelationships().size());
            sb.append(System.getProperty("line.separator"));
            appendEdgesToFile(sb, discovery.getTokenRelationships(), true);
            appendEndOfSentenceToFile(sb);
        }
        String fileName = createOutputPath("sentencesresult.txt");
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(sb.toString());
        printWriter.close();
    }

    private void appendEdgesToFile(StringBuilder sb, List<TokenRelationship> edges, boolean isnamed) {
        for (TokenRelationship relationship : edges) {
            String name = relationship.getEdgeName();
            if (isnamed && !relationship.getEdgeName().equals(EdgeNames.existence))
                name = relationship.getNamedEdge();
            sb.append("EdgeName: " + name);
            sb.append(System.getProperty("line.separator"));
            sb.append("Source:" + relationship.getSource());
            sb.append(System.getProperty("line.separator"));
            sb.append("  From: " + relationship.getFromToken().getToken());
            sb.append(System.getProperty("line.separator"));
            sb.append("  To: " + relationship.getToToken().getToken());
            sb.append(System.getProperty("line.separator"));
        }
        sb.append(System.getProperty("line.separator"));
    }

    private void appendEndOfSentenceToFile(StringBuilder sb) {
        sb.append(System.getProperty("line.separator"));
        sb.append("*************************************************");
        sb.append(System.getProperty("line.separator"));
    }

    private List<TokenRelationship> getNamedEdges(SentenceDiscovery discovery) {
        List<TokenRelationship> result = new ArrayList<>();
        for (RecommendedTokenRelationship rt : discovery.getWordEmbeddings()) {
            if (rt.getTokenRelationship().getNamedEdge() != null) {
                result.add(rt.getTokenRelationship());
                continue;
            }
            if (rt.getTokenRelationship().getEdgeName().equals(EdgeNames.existence))
                result.add(rt.getTokenRelationship());
        }
        return result;
    }
}
