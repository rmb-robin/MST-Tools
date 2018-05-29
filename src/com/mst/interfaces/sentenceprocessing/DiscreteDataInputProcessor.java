package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.Sentence;

public interface DiscreteDataInputProcessor {
	DiscreteData processDiscreteData(DiscreteData discreteData, List<Sentence> sentences, String resultType);
	DiscreteData processDiscreteData(DiscreteData discreteData, String resultType);
}
