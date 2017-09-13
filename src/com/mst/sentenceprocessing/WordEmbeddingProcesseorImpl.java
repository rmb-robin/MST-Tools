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
	private String bothVerbs = "verb-verb";
	private String defaultEdge ="token-token";
	private String firstPrep = "prep+1";
	private String secondPrep = "prep-1";
	private String verbPrep = "verb-prep";
	private String firstConjunction = "conj+1";
	private String secondConjunction = "conj-1";
	
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
		//if(first.isVerb())&second.isIN())  - changed the order - please check
			//return verbpPrep;
		//if(first.isVerb())&second.isVerb())
			//return bothVerbs
		if(first.isVerb()) 
			return firstVerb;
		if(second.isVerb())
			return secondVerb;
		if(first.isIN())
			return firstPrep;
		if(second.isIN())
			return secondPrep;		
		//if(first.isConjunction())
			//return firstConjunction
		//if(second.isConjunction())
			//return secondConjunction			
		
		return defaultEdge;
	}
	
	
	
}
