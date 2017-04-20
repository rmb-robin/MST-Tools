package com.mst.interfaces;

import java.util.List;

import com.mst.model.sentenceProcessing.VerbProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;

public interface VerbProcessor {

	List<WordToken> process(List<WordToken> wordTokens, VerbProcessingInput verbProcessingInput) throws Exception;
}
