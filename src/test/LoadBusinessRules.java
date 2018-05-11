package test;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.AddEdgeToQueryResults;
import com.mst.model.businessRule.AddEdgeToQueryResults.*;
import com.mst.model.businessRule.AppendToQueryInput;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.RemoveEdgeFromQueryResults;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR;
import static com.mst.model.businessRule.BusinessRule.LogicalOperator.AND;
import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR_NOT;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class LoadBusinessRules {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private BusinessRuleDao dao;

    public LoadBusinessRules() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);
    }

    @Test
    public void saveAppendToQueryInput() {
        BusinessRule businessRule = new BusinessRule();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(AppendToQueryInput.class.getSimpleName());
        List<BusinessRule> rules = new ArrayList<>();

        // rule 0 append OR no measurement
        AppendToQueryInput rule = new AppendToQueryInput();
        rule.setRuleName("Append no measurement");
        Map<String, List<String>> edgesToMatch  = new HashMap<>();
        edgesToMatch.put("measurement", new ArrayList<>());
        rule.setEdgesToMatch(edgesToMatch);
        rule.setEdgeToAppend("measurement");
        rule.setLogicalOperator(OR_NOT);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, businessRule.getRuleType());
        dao.save(businessRule);
    }

    @Test
    public void saveAddEdgeToQueryResults() {
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

        // rule 0 - too small to characterize
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

        // rule 1 - small ovarian cyst
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

        // rule 2 - small thyroid nodule
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

        // rule 3 - large thyroid nodule
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

        // rule 4 - physiologic, follicular, follicular-type, and dominant ovarian cyst
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

        // rule 5 - no measurement and no large or small modifier for thyroid nodule
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
        dao.delete(ORG_ID, businessRule.getRuleType());
        dao.save(businessRule);
    }

    @Test
    public void saveRemoveEdgeFromQueryResults() {
        BusinessRule businessRule = new BusinessRule();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(RemoveEdgeFromQueryResults.class.getSimpleName());
        List<BusinessRule> rules = new ArrayList<>();

        // rule 0 remove measurement if null
        RemoveEdgeFromQueryResults rule = new RemoveEdgeFromQueryResults();
        rule.setRuleName("Remove measurement if null");
        rule.setEdgeToRemove("measurement");
        rule.setRemoveIfNull(true);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, businessRule.getRuleType());
        dao.save(businessRule);
    }

    @Test
    public void getAppendToQueryInput() {
        BusinessRule businessRule = dao.get(ORG_ID, AppendToQueryInput.class.getSimpleName());
        assertNotNull(businessRule);
        assertEquals(businessRule.getOrganizationId(), ORG_ID);
        assertEquals(businessRule.getRuleType(), AppendToQueryInput.class.getSimpleName());
        List<BusinessRule> rules = businessRule.getRules();
        assertNotNull("Rule list is null;", rules);
        assertEquals(1, rules.size());
    }

    @Test
    public void getAddEdgeToQueryResults() {
        BusinessRule businessRule = dao.get(ORG_ID, AddEdgeToQueryResults.class.getSimpleName());
        assertNotNull(businessRule);
        assertEquals(businessRule.getOrganizationId(), ORG_ID);
        assertEquals(businessRule.getRuleType(), AddEdgeToQueryResults.class.getSimpleName());
        List<BusinessRule> rules = businessRule.getRules();
        assertNotNull("Rule list is null;", rules);
        assertEquals(6, rules.size());
    }

    @Test
    public void getRemoveEdgeFromQueryResults() {
        BusinessRule businessRule = dao.get(ORG_ID, RemoveEdgeFromQueryResults.class.getSimpleName());
        assertNotNull(businessRule);
        assertEquals(businessRule.getOrganizationId(), ORG_ID);
        assertEquals(businessRule.getRuleType(), RemoveEdgeFromQueryResults.class.getSimpleName());
        List<BusinessRule> rules = businessRule.getRules();
        assertNotNull("Rule list is null;", rules);
        assertEquals(1, rules.size());
    }
}
