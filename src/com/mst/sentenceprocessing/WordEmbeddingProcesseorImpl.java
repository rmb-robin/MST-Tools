package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
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
		
		String firstPos = "";
		if(first.getPos()!=null) 
			firstPos = first.getPos();
		
		String secondPos = "";
		if(second.getPos()!=null)
			secondPos = second.getPos();
		
		if(first.isVerb()&& secondPos.equals(PartOfSpeachTypes.IN)) 
			return verbPrep;
		if(first.isVerb() && second.isVerb())
			return bothVerbs;
		if(first.isVerb()) 
			return firstVerb;
		if(second.isVerb())
			return secondVerb;
		if(firstPos.equals(PartOfSpeachTypes.IN))
			return firstPrep;
		if(secondPos.equals(PartOfSpeachTypes.IN))
			return secondPrep;		
		
		if(firstPos.equals(PartOfSpeachTypes.CC))
			return firstConjunction;
		if(secondPos.equals(PartOfSpeachTypes.CC))
			return secondConjunction;			
		
		return defaultEdge;
	}
	
	
	
	
}