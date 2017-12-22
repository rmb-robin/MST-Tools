package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.interfaces.sentenceprocessing.WordEmbeddingProcessor;
import com.mst.jsonSerializers.DeepCloner;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class WordEmbeddingProcesseorImpl implements WordEmbeddingProcessor {

	private TokenRelationshipFactory factory; 
	
	public WordEmbeddingProcesseorImpl() {
		factory = new TokenRelationshipFactoryImpl();
	}
	
	@Override
	public List<RecommendedTokenRelationship> process(List<WordToken> tokens) {
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		
		for(int i =0;i<tokens.size();i++){
			if(i+1<tokens.size())
				result.add(getWordEmbedding(tokens.get(i), tokens.get(i+1)));
		}
		return result;
	}
	
	private RecommendedTokenRelationship getWordEmbedding(WordToken first, WordToken second){
		WordToken firstCloned = (WordToken) DeepCloner.deepClone(first);
		WordToken secondCloned = (WordToken) DeepCloner.deepClone(second);
		return factory.createRecommendedRelationship(getEdgeName(first, second), EdgeTypes.related, firstCloned, secondCloned);
	}

	private String getEdgeName(WordToken first, WordToken second){
		
		String firstPos = "";
		if(first.getPos()!=null) 
			firstPos = first.getPos();
		
		String secondPos = "";
		if(second.getPos()!=null)
			secondPos = second.getPos();
		
		if(first.isVerb()&& secondPos.equals(PartOfSpeachTypes.IN)) 
			return WordEmbeddingTypes.verbPrep;
		if(first.isVerb() && second.isVerb())
			return WordEmbeddingTypes.bothVerbs;
		if(first.isVerb()) 
			return WordEmbeddingTypes.firstVerb;
		if(second.isVerb())
			return WordEmbeddingTypes.secondVerb;
		if(firstPos.equals(PartOfSpeachTypes.IN))
			return WordEmbeddingTypes.firstPrep;
		if(secondPos.equals(PartOfSpeachTypes.IN))
			return WordEmbeddingTypes.secondPrep;		
		
		if(firstPos.equals(PartOfSpeachTypes.CC))
			return WordEmbeddingTypes.firstConjunction;
		if(secondPos.equals(PartOfSpeachTypes.CC))
			return WordEmbeddingTypes.secondConjunction;	
		
		if(firstPos.equals(PartOfSpeachTypes.DP))
			return WordEmbeddingTypes.firstDependentSignal;
		if(secondPos.equals(PartOfSpeachTypes.DP))
			return WordEmbeddingTypes.secondDependentSignal;	
		return WordEmbeddingTypes.defaultEdge;
	}
}