package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TextInput;

public interface SentenceProcessingController {

	void setMetadata(SentenceProcessingMetaDataInput sentenceProcessingMetaDataInput);
	List<Sentence> processSentences(List<String> sentenceTexts) throws Exception;
	List<Sentence> processText(TextInput input) throws Exception;
}
