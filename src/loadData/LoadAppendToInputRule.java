package loadData;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.AppendToInput;
import com.mst.model.businessRule.BusinessRule;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR_NOT;
import static com.mst.model.businessRule.BusinessRule.RuleType.MODIFY_SENTENCE_QUERY_INPUT;
import static com.mst.model.metadataTypes.EdgeNames.measurement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoadAppendToInputRule {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private BusinessRuleDao dao;

    public LoadAppendToInputRule() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);
    }

    @Test
    public void saveRule() {
        BusinessRule businessRule = new AppendToInput();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(MODIFY_SENTENCE_QUERY_INPUT);
        List<BusinessRule> rules = new ArrayList<>();

        // rule 1 append OR no measurement
        AppendToInput rule = new AppendToInput();
        rule.setRuleName("Append no measurement");
        Map<String, List<String>> edgesToMatch  = new HashMap<>();
        edgesToMatch.put(measurement, new ArrayList<>());
        rule.setEdgesToMatch(edgesToMatch);
        rule.setEdgeToAppend(measurement);
        rule.setLogicalOperator(OR_NOT);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, AppendToInput.class.getName());
        dao.save(businessRule);
    }

    @Test
    public void getRule() {
        List<BusinessRule> businessRules = dao.get(ORG_ID, MODIFY_SENTENCE_QUERY_INPUT);
        assertNotNull(businessRules);
        for (BusinessRule rule : businessRules) {
            assertNotNull(rule);
            assertEquals(rule.getOrganizationId(), ORG_ID);
            assertEquals(rule.getRuleType(), MODIFY_SENTENCE_QUERY_INPUT);
            if (rule instanceof AppendToInput) {
                List<BusinessRule> rules = rule.getRules();
                assertNotNull(rules);
                assertEquals(1, rules.size());
            }
        }
    }

}
