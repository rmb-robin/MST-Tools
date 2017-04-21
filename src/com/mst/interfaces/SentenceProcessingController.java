package com.mst.interfaces;

import java.util.List;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public interface SentenceProcessingController {

	void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput);
	List<Sentence> processSentences(List<String> sentenceTexts) throws Exception;
}
