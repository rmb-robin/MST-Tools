package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.dao.SentenceDiscoveryDaoImpl;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.IcdTenRequest;
import com.mst.model.requests.IcdTenSentenceInstance;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.util.MongoDatastoreProviderDefault;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class IDCTenProcesser {

	
	private SentenceDiscoveryProcessorImpl discoveryProcessorImpl = new SentenceDiscoveryProcessorImpl();
	private TokenRelationshipFactoryImpl factoryImpl = new TokenRelationshipFactoryImpl();
	private SentenceDiscoveryDaoImpl dao = new SentenceDiscoveryDaoImpl();
	
	public IDCTenProcesser(SentenceProcessingMetaDataInput input)
	{
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		discoveryProcessorImpl.setMetadata(input);
	}
	
	public void processAndSave(IcdTenRequest request) throws Exception{
		List<SentenceDiscovery> discoveries = new ArrayList<>();
		
		for(IcdTenSentenceInstance  sentenceInstance: request.getInstances()){
			SentenceTextRequest s = createRequest(sentenceInstance);
			SentenceDiscovery discovery = discoveryProcessorImpl.process(s).get(0);
			appendIcdEdge(sentenceInstance.getIcdCode(), discovery);
			discoveries.add(discovery);
		}
		dao.saveCollection(discoveries);
	}
	
	private void appendIcdEdge(String icdEdge, SentenceDiscovery discovery){
		//******************************************************************************************************************************************************
		String highestValueEdge=null;
		int TokVal =0;
		List<RecommendedTokenRelationship> embeddedWords = discovery.getWordEmbeddings();
		for (int i =0; i<embeddedWords.size(); i++) {
			RecommendedTokenRelationship recommendedTokenRelationship = embeddedWords.get(i);
			TokenRelationship relationship = recommendedTokenRelationship.getTokenRelationship();
			if(relationship.getFromToken().getTokenValue()>relationship.getToToken().getTokenValue() && relationship.getFromToken().getTokenValue()>TokVal){
				highestValueEdge = relationship.getEdgeName(); 	//Tried but this caused typeMisMatch: highestValueToken = relationship.getFromToken();
				TokVal = relationship.getFromToken().getTokenValue();				
			}
			else if(relationship.getToToken().getTokenValue()>relationship.getFromToken().getTokenValue() && relationship.getToToken().getTokenValue()>TokVal){
				highestValueEdge = relationship.getEdgeName(); 	//Tried but this caused typeMisMatch: highestValueToken = relationship.getFromToken();
				TokVal = relationship.getToToken().getTokenValue();
			}
						
		}
		
		
		//*****************************************************************************************************************************************************
		//RecommendedTokenRelationship relationship = 
		//	 RecommandedTokenRelationshipUtil.getByEdgeName(discovery.getWordEmbeddings(),EdgeNames.existence);
		
		RecommendedTokenRelationship relationship = 
				 RecommandedTokenRelationshipUtil.getByEdgeName(discovery.getWordEmbeddings(),highestValueEdge );
		
		if(relationship==null) return; 
		
		WordToken toToken = new WordToken();
        //was: token.setToken(icdEdge);
		toToken.setToken(icdEdge);
		
		WordToken fromToken = relationship.getTokenRelationship().getToToken();
		

		RecommendedTokenRelationship newEdge = 
				this.factoryImpl.createRecommendedRelationship(EdgeNames.hasICD, EdgeTypes.related, fromToken, toToken, this.getClass().getName());

	
		newEdge.getTokenRelationship().setNamedEdge(newEdge.getTokenRelationship().getEdgeName());
		
		discovery.getWordEmbeddings().add(newEdge);
		
	}
	
	private SentenceTextRequest createRequest(IcdTenSentenceInstance instance){
		SentenceTextRequest request = new SentenceTextRequest();
		request.setText(instance.getSentence());
		return request;
	}
	
	
}
