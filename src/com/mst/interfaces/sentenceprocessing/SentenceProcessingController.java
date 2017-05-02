package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public interface SentenceProcessingController {

	void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput);
	List<Sentence> processSentences(SentenceRequest sentenceRequesr) throws Exception;
	List<Sentence> processText(SentenceTextRequest request) throws Exception;
}
