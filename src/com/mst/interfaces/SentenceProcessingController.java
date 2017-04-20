package com.mst.interfaces;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public interface SentenceProcessingController {

	void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput);
	Sentence ProcessSentence(String sentenceText) throws Exception;
}
