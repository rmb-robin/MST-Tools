package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class RecommendedNounPhraseProcesser extends RelationshipProcessorBase {

	private RelationshipInput input;
	private TokenRelationshipFactory factory; 
	
	public RecommendedNounPhraseProcesser(){
		factory = new TokenRelationshipFactoryImpl();
	}

	public void setNamedEdges(List<RecommandedTokenRelationship> edges, RelationshipInput input) {
		setrelationshipMaps(input.getRelationshipMappings());
		this.input = input;
		for(RecommandedTokenRelationship recommandedTokenRelationship: edges){
			processSingleEdge(recommandedTokenRelationship);
		}
	}

	public RecommandedNounPhraseResult process(List<RecommandedTokenRelationship> embeddedwords){
		
		Map<Integer, RecommandedTokenRelationship> wordEmbeddingsByIndex = getFilteredWordEmbeddings(embeddedwords);
		int iterator = 0;
		int prevIndex=0;
		int beginNounPhraseIndex=0; 
		int endNounPhraseIndex=0; 
		Map<Integer, Integer> nounPhraseIndexes = new HashMap<Integer,Integer>();
		for(Entry<Integer,RecommandedTokenRelationship> entry : wordEmbeddingsByIndex.entrySet()){
			TokenRelationship tokenRelationship = entry.getValue().getTokenRelationship();
			 
			if(iterator==0 || entry.getKey()-prevIndex>1){
				tokenRelationship.getFromToken().setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
				beginNounPhraseIndex = entry.getKey();
			}
			if(!wordEmbeddingsByIndex.containsKey(entry.getKey()+1)) {
				tokenRelationship.getToToken().setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
				endNounPhraseIndex = entry.getKey();
				if(!nounPhraseIndexes.containsKey(beginNounPhraseIndex))
					nounPhraseIndexes.put(beginNounPhraseIndex, endNounPhraseIndex);
				//setVerifiedStatus(beginNounPhraseIndex, endNounPhraseIndex, embeddedwords);
			}	
			if(iterator == wordEmbeddingsByIndex.size()-1) break;
			prevIndex = entry.getKey();
			iterator+=1;
		}	
		RecommandedNounPhraseResult nounPhraseResult = new RecommandedNounPhraseResult();
		nounPhraseResult.setRecommandedTokenRelationships(embeddedwords);
		nounPhraseResult.setNounPhraseIndexes(nounPhraseIndexes);
		return nounPhraseResult;
	}
	
	private RecommandedTokenRelationship create(RecommandedTokenRelationship from, RecommandedTokenRelationship to){
		RecommandedTokenRelationship newRelationship = new RecommandedTokenRelationship();
		TokenRelationship tokenRelationship = factory.create(WordEmbeddingTypes.defaultEdge, EdgeTypes.related, from.getTokenRelationship().getFromToken(),to.getTokenRelationship().getToToken());
		newRelationship.setTokenRelationship(tokenRelationship);
		return newRelationship;
	}
	
	
	public List<RecommandedTokenRelationship> addEdges(List<RecommandedTokenRelationship> edges, RelationshipInput input){
 
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		this.input = input;
		for(int i =0; i<edges.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = edges.get(i);
			if(!recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))continue; 
			RelationshipMapping mapping = findMapping(recommandedTokenRelationship);
			if(mapping!=null) continue; 
			result.addAll(findPermitation(i, recommandedTokenRelationship, edges));
		}
		return result;
	}
	
	private List<RecommandedTokenRelationship> findPermitation(int fromIndex,RecommandedTokenRelationship recommandedTokenRelationship,List<RecommandedTokenRelationship> edges ){
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		for(int i =fromIndex+1; i<edges.size();i++){
			RecommandedTokenRelationship iterating = edges.get(i);
			if(!iterating.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))return  result;
			RecommandedTokenRelationship newRelationship = create(recommandedTokenRelationship,iterating);
			RelationshipMapping mapping = findMapping(newRelationship);
			if(mapping==null) continue; 
			newRelationship.getTokenRelationship().setNamedEdge(mapping.getNamedEdgeName());
			result.add(newRelationship);
		}
		return result;
	}
	
	private Map<Integer,RecommandedTokenRelationship> getFilteredWordEmbeddings(List<RecommandedTokenRelationship> wordEmbeddings){
		Map<Integer,RecommandedTokenRelationship> result = new HashMap<>();
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship =  wordEmbeddings.get(i);
			TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
			String type = tokenRelationship.getEdgeName();
			if(type.equals(WordEmbeddingTypes.defaultEdge) || type.equals(WordEmbeddingTypes.secondPrep) ||
					type.equals(WordEmbeddingTypes.secondVerb))
					result.put(i, recommandedTokenRelationship);
		}
		return result;
	}
	
	private RelationshipMapping findMapping(RecommandedTokenRelationship recommandedTokenRelationship){
		for(RelationshipMapping mapping :input.getRelationshipMappings()){
			if(!isWordTokenMatchToRelationship(mapping.getIsFromSemanticType(),false,mapping.getFromToken(), recommandedTokenRelationship.getTokenRelationship().getFromToken())) continue; 
			if(isWordTokenMatchToRelationship(mapping.getIsToSemanticType(),false,mapping.getToToken(), recommandedTokenRelationship.getTokenRelationship().getToToken())) {
				return mapping;
			 
			}
		}
		return null;
	}
	
	
	private void processSingleEdge(RecommandedTokenRelationship recommandedTokenRelationship){
		RelationshipMapping mapping = findMapping(recommandedTokenRelationship);
		if(mapping==null) return;
		recommandedTokenRelationship.getTokenRelationship().setNamedEdge(mapping.getNamedEdgeName());
	}
}
