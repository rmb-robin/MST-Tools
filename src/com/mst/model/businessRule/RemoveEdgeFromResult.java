package com.mst.model.businessRule;

import java.util.List;
import java.util.Map;

public class RemoveEdgeFromResult extends BusinessRule {
    private String ruleName;
    private Map<String, List<String>> edgesToMatch;
    private String edgeToRemove;
    private List<String> edgeToRemoveValues;
    private boolean removeIfNull;

    public RemoveEdgeFromResult() {
        super(RemoveEdgeFromResult.class.getName());
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Map<String, List<String>> getEdgesToMatch() {
        return edgesToMatch;
    }

    public void setEdgesToMatch(Map<String, List<String>> edgesToMatch) {
        this.edgesToMatch = edgesToMatch;
    }

    public String getEdgeToRemove() {
        return edgeToRemove;
    }

    public void setEdgeToRemove(String edgeToRemove) {
        this.edgeToRemove = edgeToRemove;
    }

    public List<String> getEdgeToRemoveValues() {
        return edgeToRemoveValues;
    }

    public void setEdgeToRemoveValues(List<String> edgeToRemoveValues) {
        this.edgeToRemoveValues = edgeToRemoveValues;
    }

    public boolean isRemoveIfNull() {
        return removeIfNull;
    }

    public void setRemoveIfNull(boolean removeIfNull) {
        this.removeIfNull = removeIfNull;
    }
}
