package com.mst.model.metadataTypes;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.businessRule.QueryBusinessRule;
import java.util.List;

public class CreateSynonymQueryBusinessRuleType {
    private SentenceQueryInput input;
    private List<QueryBusinessRule.Rule> rulesApplied;

    public CreateSynonymQueryBusinessRuleType(SentenceQueryInput input, List<QueryBusinessRule.Rule> rulesApplied) {
        this.input = input;
        this.rulesApplied = rulesApplied;
    }

    public SentenceQueryInput getInput() {
        return input;
    }

    public List<QueryBusinessRule.Rule> getRulesApplied() {
        return rulesApplied;
    }
}
