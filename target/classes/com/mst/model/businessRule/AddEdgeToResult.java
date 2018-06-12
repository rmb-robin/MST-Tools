package com.mst.model.businessRule;

import java.util.List;
import java.util.Map;

public class AddEdgeToResult extends BusinessRule {
    private String ruleName;
    private List<AddEdgeToResult.Edge> specialEdges;
    private boolean searchSentenceForSpecialEdges;
    private Map<String, List<String>> edgesToMatch;
    private String edgeToAdd;
    private List<EdgeToAddValue> edgeToAddValues;

    public AddEdgeToResult() {
        super(AddEdgeToResult.class.getSimpleName());
    }

    public static class Edge {
        private LogicalOperator logicalOperator;
        private boolean edgeExists;
        private String edgeName;
        private String edgeValue;

        public LogicalOperator getLogicalOperator() {
            return logicalOperator;
        }

        public void setLogicalOperator(LogicalOperator logicalOperator) {
            this.logicalOperator = logicalOperator;
        }

        public boolean isEdgeExists() {
            return edgeExists;
        }

        public void setEdgeExists(boolean edgeExists) {
            this.edgeExists = edgeExists;
        }

        public String getEdgeName() {
            return edgeName;
        }

        public void setEdgeName(String edgeName) {
            this.edgeName = edgeName;
        }

        public String getEdgeValue() {
            return edgeValue;
        }

        public void setEdgeValue(String edgeValue) {
            this.edgeValue = edgeValue;
        }
    }

    public static class EdgeToAddValue {
        private boolean hasMinRangeValue;
        private boolean hasMaxRangeValue;
        private int minRangeValue;
        private int maxRangeValue;
        private String value;

        public boolean isHasMinRangeValue() {
            return hasMinRangeValue;
        }

        public void setHasMinRangeValue(boolean hasMinRangeValue) {
            this.hasMinRangeValue = hasMinRangeValue;
        }

        public boolean isHasMaxRangeValue() {
            return hasMaxRangeValue;
        }

        public void setHasMaxRangeValue(boolean hasMaxRangeValue) {
            this.hasMaxRangeValue = hasMaxRangeValue;
        }

        public int getMinRangeValue() {
            return minRangeValue;
        }

        public void setMinRangeValue(int minRangeValue) {
            this.minRangeValue = minRangeValue;
        }

        public int getMaxRangeValue() {
            return maxRangeValue;
        }

        public void setMaxRangeValue(int maxRangeValue) {
            this.maxRangeValue = maxRangeValue;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public List<Edge> getSpecialEdges() {
        return specialEdges;
    }

    public void setSpecialEdges(List<Edge> specialEdges) {
        this.specialEdges = specialEdges;
    }

    public boolean isSearchSentenceForSpecialEdges() {
        return searchSentenceForSpecialEdges;
    }

    public void setSearchSentenceForSpecialEdges(boolean searchSentenceForSpecialEdges) {
        this.searchSentenceForSpecialEdges = searchSentenceForSpecialEdges;
    }

    public Map<String, List<String>> getEdgesToMatch() {
        return edgesToMatch;
    }

    public void setEdgesToMatch(Map<String, List<String>> edgesToMatch) {
        this.edgesToMatch = edgesToMatch;
    }

    public String getEdgeToAdd() {
        return edgeToAdd;
    }

    public void setEdgeToAdd(String edgeToAdd) {
        this.edgeToAdd = edgeToAdd;
    }

    public List<EdgeToAddValue> getEdgeToAddValues() {
        return edgeToAddValues;
    }

    public void setEdgeToAddValues(List<EdgeToAddValue> edgeToAddValues) {
        this.edgeToAddValues = edgeToAddValues;
    }
}
