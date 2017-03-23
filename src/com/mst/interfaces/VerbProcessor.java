package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.VerbProcessingInput;

public interface VerbProcessor {

	List<WordToken> process(List<WordToken> wordTokens, VerbProcessingInput verbProcessingInput) throws Exception;
}
