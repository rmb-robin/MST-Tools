package test;

import com.mst.dao.QueryBusinessRuleDaoImpl;
import com.mst.model.businessRule.QueryBusinessRule;
import com.mst.model.metadataTypes.QueryBusinessRuleTypes;
import com.mst.util.MongoDatastoreProviderDefault;
import javafx.util.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueryBusinessRuleCreateSynonym {
    @Test
    public void insert(){
        MongoDatastoreProviderDefault provider = new MongoDatastoreProviderDefault();
        QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
        dao.setMongoDatastoreProvider(provider);

        // business rule for small ovarian cyst
        QueryBusinessRule rule = new QueryBusinessRule();
        rule.setOrganizationId("58c6f3ceaf3c420b90160803");
        rule.setRuleName("Small Measurement Modifier - Ovarian Cyst");
        rule.setRuleType(QueryBusinessRuleTypes.CREATE_SYNONYM);
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","structure","structures")));
        rule.setEdgeName("measurement");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList(".1")));
        rule.setSynonymousEdge("disease modifier");
        rule.setSynonymousValue("small");
        List<Pair<String, List<String>>> edges = new ArrayList<>();
        edges.add(new Pair<>("existence", new ArrayList<>()));
        edges.add(new Pair<>("disease location", new ArrayList<>(Arrays.asList("adnexa","adnexal","adnexum","ovarian","ovaries","ovary","paraovarian"))));
        rule.setEdgeValuesToMatch(edges);
        dao.save(rule);

        // business rule for small thyroid nodule
        rule = new QueryBusinessRule();
        rule.setOrganizationId("58c6f3ceaf3c420b90160803");
        rule.setRuleName("Small Measurement Modifier - Thyroid Nodule");
        rule.setRuleType(QueryBusinessRuleTypes.CREATE_SYNONYM);
        rule.setQueryTokens(new ArrayList<>(Arrays.asList("cyst","cysts","lesion","lesions","masses","masses", "nodule", "nodules", "hypodensity", "attenuation")));
        rule.setEdgeName("measurement");
        rule.setEdgeValues(new ArrayList<>(Collections.singletonList(".1")));
        rule.setSynonymousEdge("disease modifier");
        rule.setSynonymousValue("small");
        edges = new ArrayList<>();
        edges.add(new Pair<>("existence", new ArrayList<>()));
        edges.add(new Pair<>("disease location", new ArrayList<>(Arrays.asList("isthmus", "thyroid"))));
        rule.setEdgeValuesToMatch(edges);
        dao.save(rule);
    }

    @Test
    public void get(){
        MongoDatastoreProviderDefault provider = new  MongoDatastoreProviderDefault();
        QueryBusinessRuleDaoImpl dao = new QueryBusinessRuleDaoImpl();
        dao.setMongoDatastoreProvider(provider);

        //TODO needs to return multiple rules
        //QueryBusinessRule rule = dao.get("58c6f3ceaf3c420b90160803", QueryBusinessRuleTypes.CREATE_SYNONYM);
    }
}
