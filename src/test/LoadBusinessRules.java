package test;

import com.mst.dao.BusinessRuleDaoImpl;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.businessRule.*;
import com.mst.model.businessRule.AddEdgeToResult.*;
import com.mst.model.businessRule.Compliance.*;
import com.mst.model.discrete.FollowupDescriptor;
import com.mst.model.discrete.FollowupRecommendation;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;

import java.util.*;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.*;
import static com.mst.model.businessRule.BusinessRule.RuleType.*;
import static com.mst.model.businessRule.SecondLargestMeasurementProcessing.IdentifierType.*;
import static com.mst.model.metadataTypes.ComplianceBucketName.*;
import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.FollowupDescriptor.*;
import static com.mst.model.metadataTypes.MeasurementAnnotations.*;
import static com.mst.model.metadataTypes.MeasurementClassification.*;
import static com.mst.model.metadataTypes.MenopausalStatus.*;
import static com.mst.model.metadataTypes.UnitOfMeasure.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void saveComplianceProcessing() {
        BusinessRule businessRule = new Compliance();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(SENTENCE_PROCESSING);
        Compliance rule;
        List<BusinessRule> rules = new ArrayList<>();
        Map<String, List<String>> edgesToMatch;
        List<Bucket> buckets;
        Bucket bucket;
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
        bucket = new Bucket();
        bucket.setBucketName(BUCKET_1);
        bucket.setMinSize(2.6);
        bucket.setMaxSize(2.9);
        followup = new FollowupRecommendation();
        followup.setTime(5);
        followup.setUnitOfMeasure(YEARS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Bucket();
        bucket.setBucketName(BUCKET_2);
        bucket.setMinSize(3.0);
        bucket.setMaxSize(3.4);
        followup = new FollowupRecommendation();
        followup.setTime(3);
        followup.setUnitOfMeasure(YEARS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Bucket();
        bucket.setBucketName(BUCKET_3);
        bucket.setMinSize(3.5);
        bucket.setMaxSize(3.9);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Bucket();
        bucket.setBucketName(BUCKET_4);
        bucket.setMinSize(4.0);
        bucket.setMaxSize(4.4);
        followup = new FollowupRecommendation();
        followup.setTime(12);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Bucket();
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
        bucket = new Bucket();
        bucket.setBucketName(BUCKET_6);
        bucket.setMinSize(4.5);
        bucket.setMaxSize(5.4);
        followup = new FollowupRecommendation();
        followup.setTime(6);
        followup.setUnitOfMeasure(MONTHS);
        followup.setOngoing(true);
        bucket.setFollowupRecommendation(followup);
        buckets.add(bucket);
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
        bucket = new Bucket();
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
    public void saveAppendToQueryInput() {
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
    public void saveAddEdgeToQueryResults() {
        BusinessRule businessRule = new AddEdgeToResult();
        businessRule.setOrganizationId(ORG_ID);
        businessRule.setRuleType(MODIFY_SENTENCE_QUERY_RESULT);
        List<BusinessRule> rules = new ArrayList<>();
        AddEdgeToResult rule;
        Edge edge;
        EdgeToAddValue edgeToAddValue;
        List<EdgeToAddValue> edgeToAddValues;
        List<Edge> specialEdges;
        Map<String, List<String>> edgesToMatch;

        // rule 0 - too small to characterize
        rule = new AddEdgeToResult();
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
        rule.setEdgeToAdd(measurement);
        edgeToAddValue = new AddEdgeToResult.EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".04");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        rule.setEdgesToMatch(edgesToMatch);
        rules.add(rule);

        // rule 1 - small ovarian cyst
        rule = new AddEdgeToResult();
        rule.setRuleName("Small Measurement Modifier; Ovarian Cyst");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd(measurement);
        edgeToAddValue = new AddEdgeToResult.EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 2 - small thyroid nodule
        rule = new AddEdgeToResult();
        rule.setRuleName("Small Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd(measurement);
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 3 - large thyroid nodule
        rule = new AddEdgeToResult();
        rule.setRuleName("Large Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(diseaseModifier);
        edge.setEdgeValue("large");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd(measurement);
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
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 4 - physiologic, follicular, follicular-type, and dominant ovarian cyst
        rule = new AddEdgeToResult();
        rule.setRuleName("Physiologic, Follicular, Follicular-type, and Dominant Ovarian Cyst");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("dominant");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("physiologic");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(OR);
        edge.setEdgeExists(true);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("follicular");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setEdgeToAdd(measurement);
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian")));
        rule.setEdgesToMatch(edgesToMatch);
        rule.setSearchSentenceForSpecialEdges(false);
        rules.add(rule);

        // rule 5 - no measurement and no large or small modifier for thyroid nodule
        rule = new AddEdgeToResult();
        rule.setRuleName("No Measurement Modifier; Thyroid Nodule");
        specialEdges = new ArrayList<>();
        edge = new Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeExists(false);
        edge.setEdgeName(simpleCystModifiers);
        edge.setEdgeValue("small");
        specialEdges.add(edge);
        edge = new Edge();
        edge.setLogicalOperator(AND);
        edge.setEdgeExists(false);
        edge.setEdgeName(diseaseModifier);
        edge.setEdgeValue("large");
        specialEdges.add(edge);
        rule.setSpecialEdges(specialEdges);
        rule.setSearchSentenceForSpecialEdges(false);
        rule.setEdgeToAdd(measurement);
        edgeToAddValue = new EdgeToAddValue();
        edgeToAddValue.setHasMinRangeValue(false);
        edgeToAddValue.setHasMaxRangeValue(false);
        edgeToAddValue.setValue(".9");
        edgeToAddValues = new ArrayList<>(Collections.singletonList(edgeToAddValue));
        rule.setEdgeToAddValues(edgeToAddValues);
        edgesToMatch = new HashMap<>();
        edgesToMatch.put(existence, new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","mass","masses","nodule","nodules","hypodensity","attenuation","structure","structures")));
        edgesToMatch.put(diseaseLocation, new ArrayList<>(Arrays.asList("isthmus","thyroid")));
        rule.setEdgesToMatch(edgesToMatch);
        rules.add(rule);

        businessRule.setRules(rules);
        dao.delete(ORG_ID, AddEdgeToResult.class.getName());
        dao.save(businessRule);
    }

    @Test
    public void saveSecondLargestMeasurementProcessing() {
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
    public void getComplianceProcessingRules() {
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

    @Test
    public void getModifySentenceQueryInputRules() {
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

    @Test
    public void getModifySentenceQueryResultRules() {
        List<BusinessRule> businessRules = dao.get(ORG_ID, MODIFY_SENTENCE_QUERY_RESULT);
        assertNotNull(businessRules);
        for (BusinessRule rule : businessRules) {
            assertNotNull(rule);
            assertEquals(rule.getOrganizationId(), ORG_ID);
            assertEquals(rule.getRuleType(), MODIFY_SENTENCE_QUERY_RESULT);

            if (rule instanceof AddEdgeToResult) {
                List<BusinessRule> rules = rule.getRules();
                assertNotNull(rules);
                assertEquals(6, rules.size());
            }
        }
    }

    @Test
    public void getSecondLargestMeasurementProcessingRules() {
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
