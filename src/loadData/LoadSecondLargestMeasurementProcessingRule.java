package loadData;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.SecondLargestMeasurementProcessing;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mst.model.businessRule.BusinessRule.RuleType.SENTENCE_PROCESSING;
import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType.MEASUREMENT_ANNOTATION;
import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType.MEASUREMENT_CLASSIFICATION;
import static com.mst.model.metadataTypes.MeasurementAnnotations.AP;
import static com.mst.model.metadataTypes.MeasurementAnnotations.LENGTH;
import static com.mst.model.metadataTypes.MeasurementAnnotations.TRANSVERSE;
import static com.mst.model.metadataTypes.MeasurementClassification.LARGEST;
import static com.mst.model.metadataTypes.MeasurementClassification.MEDIAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoadSecondLargestMeasurementProcessingRule {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private BusinessRuleDao dao;

    public LoadSecondLargestMeasurementProcessingRule() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);
    }

    @Test
    public void saveRule() {
        BusinessRule businessRule = new SecondLargestMeasurementProcessing();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(SENTENCE_PROCESSING);
        List<BusinessRule> rules = new ArrayList<>();

        // rule 0 two measurements (AP and Transverse)
        SecondLargestMeasurementProcessing rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("2 measurements - AP and Transverse");
        rule.setNumberDimensions(2);
        rule.setAxisAnnotations(new ArrayList<>(Arrays.asList(AP, TRANSVERSE)));
        rule.setSecondLargestIdentifier(LARGEST);
        rule.setIdentifierType(MEASUREMENT_CLASSIFICATION);
        rules.add(rule);

        // rule 1 two measurements (AP and Length)
        rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("2 measurements - AP and Length");
        rule.setNumberDimensions(2);
        rule.setAxisAnnotations(new ArrayList<>(Arrays.asList(AP, LENGTH)));
        rule.setSecondLargestIdentifier(AP);
        rule.setIdentifierType(MEASUREMENT_ANNOTATION);
        rules.add(rule);

        // rule 2 two measurements (Transverse and Length)
        rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("2 measurements - Transverse and Length");
        rule.setNumberDimensions(2);
        rule.setAxisAnnotations(new ArrayList<>(Arrays.asList(TRANSVERSE, LENGTH)));
        rule.setSecondLargestIdentifier(TRANSVERSE);
        rule.setIdentifierType(MEASUREMENT_ANNOTATION);
        rules.add(rule);

        // rule 3 three measurements (AP, Transverse, and Length)
        rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("3 measurements - AP, Transverse, and Length");
        rule.setNumberDimensions(3);
        rule.setAxisAnnotations(new ArrayList<>(Arrays.asList(AP, TRANSVERSE, LENGTH)));
        rule.setSecondLargestIdentifier(LARGEST);
        rule.setIdentifierType(MEASUREMENT_CLASSIFICATION);
        rule.setLargestBetweenAnnotations(new ArrayList<>(Arrays.asList(AP, TRANSVERSE)));
        rules.add(rule);

        // rule 4 two measurements (x and y)
        rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("2 measurements - x and y");
        rule.setNumberDimensions(2);
        rule.setSecondLargestIdentifier(LARGEST);
        rule.setIdentifierType(MEASUREMENT_CLASSIFICATION);
        rules.add(rule);

        // rule 5 three measurements (x, y, z)
        rule = new SecondLargestMeasurementProcessing();
        rule.setRuleName("3 measurements - x, y, and z");
        rule.setNumberDimensions(3);
        rule.setSecondLargestIdentifier(MEDIAN);
        rule.setIdentifierType(MEASUREMENT_CLASSIFICATION);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, SecondLargestMeasurementProcessing.class.getName());
        dao.save(businessRule);
    }

    @Test
    public void getRule() {
        List<BusinessRule> businessRules = dao.get(ORG_ID, SENTENCE_PROCESSING);
        assertNotNull(businessRules);
        for (BusinessRule rule : businessRules) {
            assertNotNull(rule);
            assertEquals(rule.getOrganizationId(), ORG_ID);
            assertEquals(rule.getRuleType(), SENTENCE_PROCESSING);
            if (rule instanceof SecondLargestMeasurementProcessing) {
                List<BusinessRule> rules = rule.getRules();
                assertNotNull(rules);
                assertEquals(6, rules.size());
            }
        }
    }
}
