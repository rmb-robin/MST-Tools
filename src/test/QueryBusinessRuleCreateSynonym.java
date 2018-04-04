package test;

import com.mst.dao.QueryBusinessRuleDaoImpl;
import com.mst.model.businessRule.DiscreteDataType;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.DiscreteDataType.DataType.NUMERIC_RANGE;
import static org.junit.Assert.*;

public class QueryBusinessRuleCreateSynonym {
    private String orgId = "58c6f3ceaf3c420b90160803";

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
        rule.setEdgeNameExists(true);
        rule.setEdgeName("disease modifier");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList("small")));
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".1cm")));
        rule.setDiscreteDataToMatch(new ArrayList<>());
        Map<String, List<String>> edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 1 for small thyroid nodule
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Small Measurement Modifier; Thyroid Nodule");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        rule.setEdgeNameExists(true);
        rule.setEdgeName("disease modifier");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList("small")));
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".1cm")));
        rule.setDiscreteDataToMatch(new ArrayList<>());
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 2 for large thyroid nodule age 0-18
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Large Measurement Modifier; Thyroid Nodule; Age 0-18");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        rule.setEdgeNameExists(true);
        rule.setEdgeName("disease modifier");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList("large")));
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".1cm")));
        DiscreteDataType patientAge = new DiscreteDataType();
        patientAge.setName("patientAge");
        patientAge.setDataType(NUMERIC_RANGE);
        patientAge.setMinRangeValue("0");
        patientAge.setMaxRangeValue("18");
        List<DiscreteDataType> discreteData = new ArrayList<>(Collections.singletonList(patientAge));
        rule.setDiscreteDataToMatch(discreteData);
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 3 for large thyroid nodule age > 18
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Large Measurement Modifier; Thyroid Nodule; Age > 18");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        rule.setEdgeNameExists(true);
        rule.setEdgeName("disease modifier");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList("large")));
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList("1.5cm")));
        patientAge = new DiscreteDataType();
        patientAge.setName("patientAge");
        patientAge.setDataType(NUMERIC_RANGE);
        patientAge.setMinRangeValue("19");
        patientAge.setMaxRangeValue("150");
        discreteData = new ArrayList<>(Collections.singletonList(patientAge));
        rule.setDiscreteDataToMatch(discreteData);
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 4 for too small to characterize
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Too Small To Characterize");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        rule.setEdgeNameExists(true);
        rule.setEdgeName("disease modifier");
        rule.setEdgeValues(new ArrayList<>(Arrays.asList("Too small to characterize","TSTC")));
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".4mm")));
        rule.setDiscreteDataToMatch(new ArrayList<>());
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 5 for thyroid nodule no measurement
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("No Measurement Modifier; Thyroid Nodule");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        rule.setEdgeNameExists(false);
        rule.setEdgeName("measurement");
        rule.setEdgeValues(new ArrayList<>());
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".9cm")));
        rule.setDiscreteDataToMatch(new ArrayList<>());
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("disease location", new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgeValuesToMatch(edges);
        rules.add(rule);

        // rule 6 for physiologic, follicular, follicular-type, and dominant ovarian cyst
        rule = new QueryBusinessRule.Rule();
        rule.setRuleName("Physiologic, Follicular, Follicular-type, and Dominant Ovarian Cyst");
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        rule.setEdgeNameExists(false);
        rule.setEdgeName("measurement");
        rule.setEdgeValues(new ArrayList<>());
        rule.setSynonymousEdge("measurement");
        rule.setSynonymousValues(new ArrayList<>(Collections.singletonList(".9cm")));
        rule.setDiscreteDataToMatch(new ArrayList<>());
        edges = new HashMap<>();
        edges.put("existence", new ArrayList<>());
        edges.put("simple cyst modifiers", new ArrayList<>(Arrays.asList("dominant","physiologic","follicular")));
        edges.put("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgeValuesToMatch(edges);
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
        assertTrue(rules.size() == 7);

        for (int i = 0; i < rules.size(); ++i) {
            QueryBusinessRule.Rule rule = rules.get(i);
            List<String> tokens = rule.getQueryTokens();
            List<String> synonymousValues = rule.getSynonymousValues();
            List<DiscreteDataType> discreteData = rule.getDiscreteDataToMatch();
            Map<String, List<String>> edges = rule.getEdgeValuesToMatch();

            assertNotNull(tokens);
            if (i != 5 && i != 6) {
                assertTrue(rule.isEdgeNameExists());
                assertEquals(rule.getEdgeName(), "disease modifier");
                assertEquals(rule.getSynonymousEdge(), "measurement");
                assertNotNull(synonymousValues);
                assertTrue(synonymousValues.size() > 0);
            }
            assertNotNull(edges);
            assertTrue(edges.size() >= 1);

            switch(i)
            {
                case 0:
                    assertEquals(rule.getRuleName(), "Small Measurement Modifier; Ovarian Cyst");
                    assertEquals(tokens.size(), 6);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(5), "structures");
                    assertEquals(rule.getEdgeValues().get(0), "small");
                    assertEquals(synonymousValues.get(0), ".1cm");
                    for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                            assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                            if (entry.getKey().equals("disease location")) {
                                assertEquals(entry.getKey(), "disease location");
                                assertTrue(entry.getValue().size() == 7);
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
                    assertEquals(rule.getEdgeValues().get(0), "small");
                    assertEquals(synonymousValues.get(0), ".1cm");
                    for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertTrue(entry.getValue().size() == 2);
                            assertEquals(entry.getValue().get(0), "isthmus");
                            assertEquals(entry.getValue().get(1), "thyroid");
                        }
                    }
                    break;
                case 2:
                    assertEquals(rule.getRuleName(), "Large Measurement Modifier; Thyroid Nodule; Age 0-18");
                    assertEquals(tokens.size(), 10);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(9), "attenuation");
                    assertEquals(rule.getEdgeValues().get(0), "large");
                    assertEquals(synonymousValues.get(0), ".1cm");
                    assertNotNull(discreteData);
                    assertTrue(discreteData.size() == 1);
                    for (DiscreteDataType dataType : discreteData) {
                        assertEquals(dataType.getName(), "patientAge");
                        assertEquals(dataType.getDataType(), NUMERIC_RANGE);
                        assertEquals(dataType.getMinRangeValue(), "0");
                        assertEquals(dataType.getMaxRangeValue(), "18");
                    }
                    for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertTrue(entry.getValue().size() == 2);
                            assertEquals(entry.getValue().get(0), "isthmus");
                        }
                    }
                    break;
                case 3:
                    assertEquals(rule.getRuleName(), "Large Measurement Modifier; Thyroid Nodule; Age > 18");
                    assertEquals(tokens.size(), 10);
                    assertEquals(tokens.get(0), "cyst");
                    assertEquals(tokens.get(9), "attenuation");
                    assertEquals(rule.getEdgeValues().get(0), "large");
                    assertEquals(synonymousValues.get(0), "1.5cm");
                    assertNotNull(discreteData);
                    assertTrue(discreteData.size() == 1);
                    for (DiscreteDataType dataType : discreteData) {
                        assertEquals(dataType.getName(), "patientAge");
                        assertEquals(dataType.getDataType(), NUMERIC_RANGE);
                        assertEquals(dataType.getMinRangeValue(), "19");
                        assertEquals(dataType.getMaxRangeValue(), "150");
                    }
                    for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                        assertTrue(entry.getKey().equals("existence") || entry.getKey().equals("disease location"));
                        if (entry.getKey().equals("disease location")) {
                            assertEquals(entry.getKey(), "disease location");
                            assertTrue(entry.getValue().size() == 2);
                            assertEquals(entry.getValue().get(1), "thyroid");
                        }
                    }
            }
        }
    }
}
