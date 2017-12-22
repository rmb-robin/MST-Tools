package com.mst.model.sentenceProcessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.RecommandedSubjectAnnotator;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;

public class RecommandedSubjectAnnotatorImpl implements RecommandedSubjectAnnotator {

	private boolean isDefault(RecommendedTokenRelationship edge){
		return edge.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge);
	}

	public void annotate(SentenceDiscovery discovery){
		
		List<RecommendedTokenRelationship> relationships = discovery.getWordEmbeddings();
		for(int i =0;i<relationships.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(isDefault(recommandedTokenRelationship)){
				processSubjectCompliment(i, relationships);
			}
			
			if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.secondVerb))
				processSubject(i, relationships);
		}
	}
	
	private void processSubject(int index, List<RecommendedTokenRelationship> relationships){
		for(int i = index; i>=0;i--){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(i==0){
				recommandedTokenRelationship.getTokenRelationship().getFromToken().setPropertyValueType(PropertyValueTypes.Subject);
				return;
			}
			
			RecommendedTokenRelationship prevRecommandedTokenRelationship = relationships.get(i-1);
			
			if(!recommandedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(prevRecommandedTokenRelationship.getTokenRelationship().getFromToken().getToken())){
				recommandedTokenRelationship.getTokenRelationship().getFromToken().setPropertyValueType(PropertyValueTypes.Subject);
				return;
			}
		}
	}
	
	private boolean isPunc(RecommendedTokenRelationship relationship){
		return relationship.getTokenRelationship().getToToken().getPos()== PartOfSpeachTypes.PUNCTUATION;
	}
	
	private void setSubjectComplement(RecommendedTokenRelationship recommandedTokenRelationship){
		recommandedTokenRelationship.getTokenRelationship().getToToken().setPropertyValueType(PropertyValueTypes.SubjectComplement);
	}
	
	private void processSubjectCompliment(int index, List<RecommendedTokenRelationship> relationships){
	
		for(int i = index; i<relationships.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = relationships.get(i);
			if(!isDefault(recommandedTokenRelationship))return;
			

			if(i+1>=relationships.size()){
				if(isPunc(recommandedTokenRelationship)){
					setSubjectComplement(relationships.get(i-1));
				}
				return;
			}
			
			RecommendedTokenRelationship nextRecommandedTokenRelationship = relationships.get(i+1);
			if(!isDefault(nextRecommandedTokenRelationship))return;
			if(i+1==relationships.size()-1){
				if(isPunc(nextRecommandedTokenRelationship)){
					setSubjectComplement(recommandedTokenRelationship);
					return;
				}
			}

			if(!recommandedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(nextRecommandedTokenRelationship.getTokenRelationship().getFromToken().getToken())){
				setSubjectComplement(recommandedTokenRelationship);
				return;
			}
		
		}
	}
}
