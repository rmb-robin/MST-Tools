package com.mst.interfaces.sentenceprocessing;

import java.util.List;
import java.util.Map;

import com.mst.model.sentenceProcessing.WordToken; 
 
public interface SemanticTypeSentenceAnnotator {
	
	List<WordToken> annotate(List<WordToken> tokens,Map<String,String> semanticTypes);
}
