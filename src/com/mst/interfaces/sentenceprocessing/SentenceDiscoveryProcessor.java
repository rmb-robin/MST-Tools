package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public interface SentenceDiscoveryProcessor {

	void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput);
	List<SentenceDiscovery> process(RecommandationRequest request);
}
