package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.Sentence;

public interface DiscreteDataBucketIdentifier {
	String getBucket(DiscreteData discreteData, List<Sentence> sentences,  DisceteDataComplianceDisplayFields fields);
}
