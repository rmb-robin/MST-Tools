package com.mst.model.businessRule;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AppendToInput extends BusinessRule {
    private String ruleName;
    private Map<String, List<String>> edgesToMatch;
    private String edgeToAppend;
    private HashSet<String> edgeToAppendValues;
    private boolean isNumeric;
    private LogicalOperator logicalOperator;

    public AppendToInput() {
        super(AppendToInput.class.getSimpleName());
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

    public String getEdgeToAppend() {
        return edgeToAppend;
    }

    public void setEdgeToAppend(String edgeToAppend) {
        this.edgeToAppend = edgeToAppend;
    }

    public HashSet<String> getEdgeToAppendValues() {
        return edgeToAppendValues;
    }

    public void setEdgeToAppendValues(HashSet<String> edgeToAppendValues) {
        this.edgeToAppendValues = edgeToAppendValues;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}
