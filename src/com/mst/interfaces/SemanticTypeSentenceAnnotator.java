package com.mst.interfaces;

import java.util.List;
import java.util.Map;

import com.mst.model.WordToken; 
 
public interface SemanticTypeSentenceAnnotator {
	
	List<WordToken> annotate(List<WordToken> tokens,Map<String,String> semanticTypes);
}
