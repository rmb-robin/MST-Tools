package test;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.AddEdgeToQueryResults;
import com.mst.model.businessRule.AddEdgeToQueryResults.*;
import com.mst.model.businessRule.BusinessRule;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.AddEdgeToQueryResults.Edge.LogicalOperator.AND;
import static com.mst.model.businessRule.AddEdgeToQueryResults.Edge.LogicalOperator.OR;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoadRuleAddEdgeToQueryResults {
    //private String orgId = "58c6f3ceaf3c420b90160803";
    private final String ORG_ID = "5972aedebde4270bc53b23e3"; //Test
    private final String TEST_SERVER = "10.0.129.218";
    private final String TEST_DATABASE_NAME = "test";

    @Test
    public void insert() {
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault(TEST_SERVER, TEST_DATABASE_NAME);
        BusinessRuleDao dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);

        BusinessRule businessRule = new BusinessRule();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(AddEdgeToQueryResults.class.getSimpleName());
        List<BusinessRule> rules = new ArrayList<>();
        AddEdgeToQueryResults rule;
        Edge edge;
        EdgeToAddValue edgeToAddValue;
        List<EdgeToAddValue> edgeToAddValues;
        List<Edge> specialEdges;
        Map<String, List<String>> edgesToMatch;

        // rule 0 for too small to characterize
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("Too Small To Characterize");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("TSTC");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("Too small to characterize");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setSearchSentenceForSpecialEdges(true);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new AddEdgeToQueryResults.EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".04");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        rule.setEdgesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 1 for small ovarian cyst
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("Small Measurement Modifier; Ovarian Cyst");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new AddEdgeToQueryResults.EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 2 for small thyroid nodule
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("Small Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 3 for large thyroid nodule
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("Large Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("disease modifier");
        edge.setEdgeValue("large");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(true);
        edgeToAddValue.setHasMaxRangeValue(true);
        edgeToAddValue.setMinRangeValue(0);
        edgeToAddValue.setMaxRangeValue(18);
        edgeToAddValue.setValue(".1");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(true);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setMinRangeValue(19);
        edgeToAddValue.setValue("1.5");
        edgeToAddValues.add(edgeToAddValue);
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 4 for physiologic, follicular, follicular-type, and dominant ovarian cyst
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("Physiologic, Follicular, Follicular-type, and Dominant Ovarian Cyst");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("dominant");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("physiologic");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("follicular");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 5 no measurement and no large or small modifier for thyroid nodule
        rule = new AddEdgeToQueryResults();
        rule.setRuleName("No Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeExists(false);
        edge.setEdgeName("simple cyst modifiers");
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeExists(false);
        edge.setEdgeName("disease modifier");
        edge.setEdgeValue("large");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setSearchSentenceForSpecialEdges(false);
        rule.setEdgeToAdd("measurement");
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put("existence", new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        edgesToMatch.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, businessRule.getRuleType()); //if updating an existing record, the dao would create and save a duplicate
        dao.save(businessRule);
    }

    @Test
    public void get() {
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(TEST_SERVER, TEST_DATABASE_NAME);
        BusinessRuleDao dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);

        BusinessRule businessRule = dao.get(ORG_ID, AddEdgeToQueryResults.class.getSimpleName());
        assertNotNull(businessRule);
        assertEquals(businessRule.getOrganizationId(), ORG_ID);
        assertEquals(businessRule.getRuleType(), AddEdgeToQueryResults.class.getSimpleName());

        List<BusinessRule> rules = businessRule.getRules();
        assertNotNull("Rule list is null;", rules);
        assertEquals(6, rules.size());

        for (int i = 0; i < rules.size(); ++i) {
            AddEdgeToQueryResults rule = (AddEdgeToQueryResults)rules.get(i);
            Map<String, List<String>> edgeValuesToMatch = rule.getEdgesToMatch();
            assertNotNull(edgeValuesToMatch);
            assertTrue(edgeValuesToMatch.size() >= 1);

            switch(i)
            {
                case 0:
                    assertEquals(rule.getRuleName(), "Too Small To Characterize");
                    break;
                case 1:
                    assertEquals(rule.getRuleName(), "Small Measurement Modifier; Ovarian Cyst");
                    assertEquals(1, rule.getEdgeToAddValues().size());
                    assertEquals(rule.getEdgeToAddValues().get(0).getValue(), ".9");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location") || entry.getKey().equals("simple cyst modifiers"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertEquals(7, entry.getValue().size());
                            assertEquals(entry.getValue().get(0), "adnexa");
                            assertEquals(entry.getValue().get(6), "paraovarian");
                        }
                    }
                    break;
                case 2:
                    assertEquals(rule.getRuleName(), "Small Measurement Modifier; Thyroid Nodule");
                    assertEquals(1, rule.getEdgeToAddValues().size());
                    assertEquals(rule.getEdgeToAddValues().get(0).getValue(), ".9");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location") || entry.getKey().equals("simple cyst modifiers"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertEquals(2, entry.getValue().size());
                            assertEquals(entry.getValue().get(0), "isthmus");
                            assertEquals(entry.getValue().get(1), "thyroid");
                        }
                    }
                    break;
                case 3:
                    assertEquals(rule.getRuleName(), "Large Measurement Modifier; Thyroid Nodule");
                    List<AddEdgeToQueryResults.EdgeToAddValue> edgeToAddValues = rule.getEdgeToAddValues();
                    assertEquals(2, edgeToAddValues.size());
                    AddEdgeToQueryResults.EdgeToAddValue edgeValue = edgeToAddValues.get(0);
                    assertTrue(edgeValue.isHasMinRangeValue());
                    assertTrue(edgeValue.isHasMaxRangeValue());
                    assertEquals(edgeValue.getMinRangeValue(), 0);
                    assertEquals(edgeValue.getMaxRangeValue(), 18);
                    assertEquals(edgeValue.getValue(), ".1");
                    edgeValue = edgeToAddValues.get(1);
                    assertTrue(edgeValue.isHasMinRangeValue());
                    assertFalse(edgeValue.isHasMaxRangeValue());
                    assertEquals(edgeValue.getMinRangeValue(), 19);
                    assertEquals(edgeValue.getValue(), "1.5");
                    for (Map.Entry<String, List<String>> entry : edgeValuesToMatch.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location") || entry.getKey().equals("disease modifier"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertEquals(2, entry.getValue().size());
                            assertEquals(entry.getValue().get(0), "isthmus");
                        }
                    }
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
