package loadData;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.Compliance;
import com.mst.model.discrete.FollowupDescriptor;
import com.mst.model.discrete.FollowupRecommendation;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.OR;
import static com.mst.model.businessRule.BusinessRule.RuleType.MODIFY_SENTENCE_QUERY_INPUT;
import static com.mst.model.businessRule.BusinessRule.RuleType.SENTENCE_PROCESSING;
import static com.mst.model.metadataTypes.ComplianceBucket.*;
import static com.mst.model.metadataTypes.ComplianceBucket.BucketType.*;
import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.EdgeNames.diseaseLocation;
import static com.mst.model.metadataTypes.EdgeNames.existence;
import static com.mst.model.metadataTypes.FollowupDescriptor.*;
import static com.mst.model.metadataTypes.FollowupDescriptor.ULTRASOUND;
import static com.mst.model.metadataTypes.MenopausalStatus.POSTMENOPAUSAL;
import static com.mst.model.metadataTypes.MenopausalStatus.PREMENOPAUSAL;
import static com.mst.model.metadataTypes.UnitOfMeasure.MONTHS;
import static com.mst.model.metadataTypes.UnitOfMeasure.YEARS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoadComplianceRule {
    private final String ORG_ID = "5972aedebde4270bc53b23e3";
    private BusinessRuleDao dao;

    public LoadComplianceRule() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault(SERVER, DATABASE);
        dao = new BusinessRuleDaoImpl(BusinessRule.class);
        dao.setMongoDatastoreProvider(provider);
    }

    @Test
    public void saveRule() {
        BusinessRule businessRule = new Compliance();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(SENTENCE_PROCESSING);
        Compliance rule;
        List<BusinessRule> rules = new ArrayList<>();
        Map<String, List<String>> edgesToMatch;
        List<Compliance.Bucket> buckets;
        Compliance.Bucket bucket;
        FollowupRecommendation followup;
        List<FollowupDescriptor> descriptors;
        FollowupDescriptor descriptor;

        // rule 1 Abdominal Aortic Aneurysm
        rule = new Compliance();
        rule.setRuleName("Abdominal Aortic Aneurysm");
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("aneurysm","aneurysms","dilation","dilations","dilatation","dilatations","distention","distentions","sac","sacs")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("abdominal","infrarenal","diaphragm","celiac","axis","sma","ima","suprarenal")));
        rule.setEdgesToMatch(edgesToMatch);
        buckets = new ArrayList<>();
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_1);
        bucket.setMinSize(2.6);
        bucket.setMaxSize(2.9);
        followup = new FollowupRecommendation();
        followup.setTime(5);
        followup.setUnitOfMeasure(YEARS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_2);
        bucket.setMinSize(3.0);
        bucket.setMaxSize(3.4);
        followup = new FollowupRecommendation();
        followup.setTime(3);
        followup.setUnitOfMeasure(YEARS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_3);
        bucket.setMinSize(3.5);
        bucket.setMaxSize(3.9);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_4);
        bucket.setMinSize(4.0);
        bucket.setMaxSize(4.4);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_5);
        bucket.setMinSize(4.0);
        bucket.setMaxSize(4.4);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(VASCULAR_CONSULTATION);
        followup.setFollowupDescriptors(new ArrayList<>(Collections.singletonList(descriptor)));
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_6);
        bucket.setMinSize(4.5);
        bucket.setMaxSize(5.4);
        followup = new FollowupRecommendation();
        followup.setTime(6);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_7);
        bucket.setMinSize(4.5);
        bucket.setMaxSize(5.4);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(VASCULAR_CONSULTATION);
        followup.setFollowupDescriptors(new ArrayList<>(Collections.singletonList(descriptor)));
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_8);
        bucket.setMinSize(5.5);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(VASCULAR_SURGEON);
        followup.setFollowupDescriptors(new ArrayList<>(Collections.singletonList(descriptor)));
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        rule.setBuckets(buckets);
        rules.add(rule);

        // rule 2 Incidental Thyroid Nodule
        rule = new Compliance();
        rule.setRuleName("Incidental Thyroid Nodule");
        edgesToMatch  = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","hypodensity","hypodensities","attenuation","attenuations")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        buckets = new ArrayList<>();
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_1);
        bucket.setMinAge(0);
        bucket.setMaxAge(18);
        bucket.setMinSize(0);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_2);
        bucket.setMinAge(19);
        bucket.setMaxAge(34);
        bucket.setMinSize(1);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_3);
        bucket.setMinAge(35);
        bucket.setMaxAge(100);
        bucket.setMinSize(1.5);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_4);
        bucket.setMinAge(19);
        bucket.setMaxAge(34);
        bucket.setMinSize(0);
        bucket.setMaxSize(0.9);
        followup = new FollowupRecommendation();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        followup.setFollowupDescriptors(new ArrayList<>(Collections.singletonList(descriptor)));
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_5);
        bucket.setMinAge(35);
        bucket.setMaxAge(100);
        bucket.setMinSize(0);
        bucket.setMaxSize(1.4);
        followup = new FollowupRecommendation();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        followup.setFollowupDescriptors(new ArrayList<>(Collections.singletonList(descriptor)));
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_7);
        bucket.setMinAge(19);
        bucket.setMaxAge(24);
        bucket.setMinSize(0);
        bucket.setMaxSize(0.9);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_8);
        bucket.setMinAge(35);
        bucket.setMaxAge(100);
        bucket.setMinSize(0);
        bucket.setMaxSize(1.4);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        rule.setBuckets(buckets);
        rules.add(rule);

        // rule 3 Enlarged Thyroid
        rule = new Compliance();
        rule.setRuleName("Enlarged Thyroid");
        edgesToMatch  = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        edgesToMatch.put(enlarged_finding_sites, new ArrayList<>(Arrays.asList("enlarge","enlarged","enlargement")));
        edgesToMatch.put(hetrogeneous_finding_sites, new ArrayList<>(Arrays.asList("prominent","heterogeneous","multinodular","multi-nodular")));
        rule.setEdgesToMatch(edgesToMatch);
        buckets = new ArrayList<>();
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_6);
        bucket.setMinAge(0);
        bucket.setMaxAge(100);
        followup = new FollowupRecommendation();
        followup.setOngoing(false);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SONOGRAPHY);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        rule.setBuckets(buckets);
        rules.add(rule);

        // rule 4 Ovarian Simple Cyst
        rule = new Compliance();
        rule.setRuleName("Ovarian Simple Cyst");
        edgesToMatch  = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","transmission","transmissions","attenuation","attenuations")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgesToMatch(edgesToMatch);
        buckets = new ArrayList<>();
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_1);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(0);
        bucket.setMaxSize(3);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_2);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(3.1);
        bucket.setMaxSize(5);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_3);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(5.1);
        bucket.setMaxSize(7);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_4);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(7.1);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(MAGNETIC_RESONANCE);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SURGICAL_EVALUATION);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_5);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(0);
        bucket.setMaxSize(1);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_6);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(1.1);
        bucket.setMaxSize(3);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(NO_FOLLOWUP);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_7);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(3.1);
        bucket.setMaxSize(7);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(COMPLIANCE);
        bucket.setBucketName(BUCKET_8);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(7.1);
        bucket.setMaxSize(100);
        followup = new FollowupRecommendation();
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(MAGNETIC_RESONANCE);
        descriptor.setLogicalOperator(OR);
        descriptors.add(descriptor);
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(SURGICAL_EVALUATION);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_9);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(0);
        bucket.setMaxSize(3);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_10);
        bucket.setMenopausalStatus(PREMENOPAUSAL);
        bucket.setMinSize(3.1);
        bucket.setMaxSize(5);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_11);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(0);
        bucket.setMaxSize(1);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Compliance.Bucket();
        bucket.setBucketType(NONCOMPLIANCE);
        bucket.setBucketName(BUCKET_12);
        bucket.setMenopausalStatus(POSTMENOPAUSAL);
        bucket.setMinSize(1.1);
        bucket.setMaxSize(3);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        descriptors = new ArrayList<>();
        descriptor = new FollowupDescriptor();
        descriptor.setDescriptor(ULTRASOUND);
        descriptors.add(descriptor);
        followup.setFollowupDescriptors(descriptors);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        rule.setBuckets(buckets);
        rules.add(rule);
        businessRule.setRules(rules);
        dao.delete(ORG_ID, Compliance.class.getName());
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
            if (rule instanceof Compliance) {
                List<BusinessRule> rules = rule.getRules();
                assertNotNull(rules);
                assertEquals(4, rules.size());
            }
        }
    }
}