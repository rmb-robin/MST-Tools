package test;

import com.mst.dao.QueryBusinessRuleDaoImpl;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.QueryBusinessRule.Rule.Edge.LogicalOperator.*;
import static org.junit.Assert.*;

public class QueryBusinessRuleCreateSynonym {
    //private String orgId = "58c6f3ceaf3c420b90160803"; //Production DB
    private String orgId = "5972aedebde4270bc53b23e3"; //Test DB

    @Test
    public void insert(){
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault();
        QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
        dao.setMongoDatastoreProvider(provider);

        QueryBusinessRule queryRule = new QueryBusinessRule();
        queryRule.setOrganizationId(orgId);
        queryRule.setRuleType(QueryBusinessRuleTypes.CREATE_SYNONYM);
        List<QueryBusinessRule.Rule> rules = new ArrayList<>();

        // rule 0 for small ovarian cyst
        QueryBusinessRule.Rule rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Small Measurement Modifier; Ovarian Cyst");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        QueryBusinessRule.Rule.Edge edge = new QueryBusinessRule.Rule.Edge();
        edge.setEdgeNameExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        rule.setAddEdgesToQuery(false);
        edge.setSynonymousEdge("measurement");
        QueryBusinessRule.Rule.Edge.SynonymousEdgeValue synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        List<QueryBusinessRule.Rule.Edge.SynonymousEdgeValue> synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        rule.setEdges(new ArrayList<>(Collections.singletonList(edge)));
        Map<String, List<String>> edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 1 for small thyroid nodule
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Small Measurement Modifier; Thyroid Nodule");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setEdgeNameExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        rule.setAddEdgesToQuery(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        rule.setEdges(new ArrayList<>(Collections.singletonList(edge)));
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 2 for large thyroid nodule
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Large Measurement Modifier; Thyroid Nodule");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setEdgeNameExists(true);
        edge.setEdgeName("disease modifier");
        edge.setEdgeValue("large");
        rule.setAddEdgesToQuery(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(true);
        synonymousEdgeValue.setHasMaxRangeValue(true);
        synonymousEdgeValue.setMinRangeValue(0);
        synonymousEdgeValue.setMaxRangeValue(18);
        synonymousEdgeValue.setSynonymousValue(".1cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(true);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setMinRangeValue(19);
        synonymousEdgeValue.setSynonymousValue("1.5cm");
        synonymousEdgeValues.add(synonymousEdgeValue);
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        rule.setEdges(new ArrayList<>(Collections.singletonList(edge)));
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 3 for too small to characterize
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Too Small To Characterize");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        List<QueryBusinessRule.Rule.Edge> edges = new ArrayList<>();
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeNameExists(false);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("TSTC");
        rule.setAddEdgesToQuery(true);
        edge.setEdgeNumeric(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".4mm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        edges.add(edge);
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeNameExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("Too small to characterize");
        edge.setEdgeNumeric(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".4mm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        edges.add(edge);
        rule.setEdges(edges);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 4 for physiologic, follicular, follicular-type, and dominant ovarian cyst
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Physiologic, Follicular, Follicular-type, and Dominant Ovarian Cyst");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setEdgeNameExists(false);
        edge.setEdgeName("measurement");
        rule.setAddEdgesToQuery(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        rule.setEdges(new ArrayList<>(Collections.singletonList(edge)));
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        edgesToMatch.put("simple cyst modifiers", new ArrayList<>(Arrays.asList("dominant","physiologic","follicular")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 5 no measurement and no large or small modifier for thyroid nodule
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("No Measurement Modifier; Thyroid Nodule");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        edges = new ArrayList<>();
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setLogicalOperator(NOT_APPLICABLE);
        edge.setEdgeNameExists(false);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        rule.setAddEdgesToQuery(false);
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        edges.add(edge);
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeNameExists(false);
        edge.setEdgeName("disease modifier");
        edge.setEdgeValue("large");
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        edges.add(edge);
        edge = new QueryBusinessRule.Rule.Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeNameExists(false);
        edge.setEdgeName("measurement");
        edge.setSynonymousEdge("measurement");
        synonymousEdgeValue = new QueryBusinessRule.Rule.Edge.SynonymousEdgeValue();
        synonymousEdgeValue.setHasMinRangeValue(false);
        synonymousEdgeValue.setHasMaxRangeValue(false);
        synonymousEdgeValue.setSynonymousValue(".9cm");
        synonymousEdgeValues = new ArrayList<>(Collections.singletonList(synonymousEdgeValue));
        edge.setSynonymousEdgeValues(synonymousEdgeValues);
        edges.add(edge);
        rule.setEdges(edges);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>());
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edgesToMatch);
        rules.add(rule);

        queryRule.setRules(rules);
        dao.delete(orgId, QueryBusinessRuleTypes.CREATE_SYNONYM); //if updating an existing record, the dao would create and save a duplicate
        dao.save(queryRule);
    }

    @Test
    public void get(){
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
        QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
        dao.setMongoDatastoreProvider(provider);

        QueryBusinessRule queryRule = dao.get(orgId, QueryBusinessRuleTypes.CREATE_SYNONYM);
        assertNotNull(queryRule);
        assertEquals(queryRule.getOrganizationId(), orgId);
        assertEquals(queryRule.getRuleType(), QueryBusinessRuleTypes.CREATE_SYNONYM);

        List<QueryBusinessRule.Rule> rules = queryRule.getRules();
        assertNotNull("Rule list is null;", rules);
        assertEquals(6, rules.size());

        for (int i = 0; i < rules.size(); ++i) {
            QueryBusinessRule.Rule rule = rules.get(i);
            List<String> tokens = rule.getQueryTokens();
            List<QueryBusinessRule.Rule.Edge> edges = rule.getEdges();
            Map<String, List<String>> edgeValuesToMatch = rule.getEdgeValuesToMatch();
            assertNotNull(tokens);
            assertNotNull(edges);
            assertNotNull(edgeValuesToMatch);
            assertTrue(tokens.size() >= 1);
            assertTrue(edges.size() >= 1);
            assertTrue(edgeValuesToMatch.size() >= 1);

            switch(i)
            {
                case 0:
                    assertEquals(rule.getRuleName(), "Small Measurement Modifier; Ovarian Cyst");
                    assertEquals(tokens.size(), 6);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(5), "structures");
                    assertEquals(1, edges.size());
                    QueryBusinessRule.Rule.Edge edge = edges.get(0);
                    assertEquals(edge.getEdgeName(), "simple cyst modifiers");
                    assertEquals(edge.getEdgeValue(), "small");
                    assertEquals(1, edge.getSynonymousEdgeValues().size());
                    assertEquals(edge.getSynonymousEdgeValues().get(0).getSynonymousValue(), ".9cm");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                            assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                            if (entry.getKey().equals("disease location")) {
                                assertEquals(entry.getKey(), "disease location");
                                assertEquals(7, entry.getValue().size());
                                assertEquals(entry.getValue().get(0), "adnexa");
                                assertEquals(entry.getValue().get(6), "paraovarian");
                            }
                    }
                    break;
                case 1:
                    assertEquals(rule.getRuleName(), "Small Measurement Modifier; Thyroid Nodule");
                    assertEquals(tokens.size(), 10);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(9), "attenuation");
                    assertEquals(1, edges.size());
                    edge = edges.get(0);
                    assertEquals(edges.get(0).getEdgeName(), "simple cyst modifiers");
                    assertEquals(edge.getEdgeValue(), "small");
                    assertEquals(1, edge.getSynonymousEdgeValues().size());
                    assertEquals(edge.getSynonymousEdgeValues().get(0).getSynonymousValue(), ".9cm");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertEquals(2, entry.getValue().size());
                            assertEquals(entry.getValue().get(0), "isthmus");
                            assertEquals(entry.getValue().get(1), "thyroid");
                        }
                    }
                    break;
                case 2:
                    assertEquals(rule.getRuleName(), "Large Measurement Modifier; Thyroid Nodule");
                    assertEquals(tokens.size(), 10);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(9), "attenuation");
                    assertEquals(1, edges.size());
                    edge = edges.get(0);
                    assertEquals(edges.get(0).getEdgeName(), "disease modifier");
                    assertEquals(edge.getEdgeValue(), "large");
                    List<QueryBusinessRule.Rule.Edge.SynonymousEdgeValue> synonymousEdgeValues = edge.getSynonymousEdgeValues();
                    assertEquals(2, synonymousEdgeValues.size());
                    QueryBusinessRule.Rule.Edge.SynonymousEdgeValue edgeValue = synonymousEdgeValues.get(0);
                    assertTrue(edgeValue.isHasMinRangeValue());
                    assertTrue(edgeValue.isHasMaxRangeValue());
                    assertEquals(edgeValue.getMinRangeValue(), 0);
                    assertEquals(edgeValue.getMaxRangeValue(), 18);
                    assertEquals(edgeValue.getSynonymousValue(), ".1cm");
                    edgeValue = synonymousEdgeValues.get(1);
                    assertTrue(edgeValue.isHasMinRangeValue());
                    assertFalse(edgeValue.isHasMaxRangeValue());
                    assertEquals(edgeValue.getMinRangeValue(), 19);
                    assertEquals(edgeValue.getSynonymousValue(), "1.5cm");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertEquals(2, entry.getValue().size());
                            assertEquals(entry.getValue().get(0), "isthmus");
                        }
                    }
                    break;
                case 3:
                    assertEquals(rule.getRuleName(), "Too Small To Characterize");
                    break;
                case 4:
                    assertEquals(rule.getRuleName(), "Physiologic, Follicular, Follicular-type, and Dominant Ovarian Cyst");
                    break;
                case 5:
                    assertEquals(rule.getRuleName(), "No Measurement Modifier; Thyroid Nodule");
                    break;
                default:
                    break;
            }
        }
    }
}
