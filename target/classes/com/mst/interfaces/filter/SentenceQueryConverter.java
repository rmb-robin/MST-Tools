package com.mst.interfaces.filter;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public interface SentenceQueryConverter {

	SentenceQueryInstance getSTQueryInstance(SentenceQueryInput input);
}
