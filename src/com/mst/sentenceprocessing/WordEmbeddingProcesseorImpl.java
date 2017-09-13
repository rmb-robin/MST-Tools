package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class WordEmbeddingProcesseorImpl implements WordEmbeddingProcessor {

	private TokenRelationshipFactory factory; 
	
	private String firstVerb = "verb+1";
	private String secondVerb = "verb-1";
	private String defaultEdge ="token-token";
	
	public WordEmbeddingProcesseorImpl() {
		factory = new TokenRelationshipFactoryImpl();
	}
	
	@Override
	public List<TokenRelationship> process(List<WordToken> tokens) {
		List<TokenRelationship> result = new ArrayList<>();
		
		for(int i =0;i<tokens.size();i++){
			if(i+1<tokens.size())
				result.add(getWordEmbedding(tokens.get(i), tokens.get(i+1)));
		}
		return result;
	}
	
	private TokenRelationship getWordEmbedding(WordToken first, WordToken second){
	    return factory.create(getEdgeName(first, second), EdgeTypes.related, first, second);
	}

	private String getEdgeName(WordToken first, WordToken second){
		if(first.isVerb()) 
			return firstVerb;
		if(second.isVerb())
			return secondVerb;

		return defaultEdge;
	}
	
	
	
}
