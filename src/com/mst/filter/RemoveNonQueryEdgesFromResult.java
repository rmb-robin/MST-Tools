package com.mst.filter;

import com.mst.model.SentenceQuery.*;

import java.util.List;
import java.util.ListIterator;

import static com.mst.model.metadataTypes.EdgeNames.measurement;

public class RemoveNonQueryEdgesFromResult {
    public void process(SentenceQueryInput input, List<SentenceQueryResult> sentenceQueryResults) {
        List<SentenceQueryInstance> instances = input.getSentenceQueryInstances();
        for (SentenceQueryResult queryResult : sentenceQueryResults) {
            List<SentenceQueryEdgeResult> results = queryResult.getSentenceQueryEdgeResults();
            ListIterator<SentenceQueryEdgeResult> itr = results.listIterator();
            while (itr.hasNext()) {
                SentenceQueryEdgeResult result = itr.next();
                String edgeName = result.getEdgeName();
                String value = result.getMatchedValue();
                boolean edgeFoundInInput = false;
                for (SentenceQueryInstance instance : instances) {
                    List<EdgeQuery> edges = instance.getEdges();
                    if (isEdgeFoundInInput(edges, edgeName, value)) {
                        edgeFoundInInput = true;
                        break;
                    }
                }
                if (!edgeFoundInInput)
                    itr.remove();
            }
        }
    }

    private boolean isEdgeFoundInInput(List<EdgeQuery> input, String edgeName, String value) {
        if (input != null && !input.isEmpty())
            for (EdgeQuery edge : input) {
                if (edge != null && edge.getName() != null && edge.getName().equals(edgeName)) {
                    if (edge.getValues() != null && !edge.getValues().isEmpty() && (edge.getValues().contains(value) || (edge.getName().equals(measurement) && value != null))) {
                        return true;
                    }
                }
            }
        return false;
    }
}
