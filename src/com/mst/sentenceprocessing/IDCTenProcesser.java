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
	
	/**
	 * Has a section added to the method appendIcdEdge to implement the highestValueEdge instead of the existance in order to implement the has-Icd edge.
	 * The code acquires the token relationship from the discovery word embeddings and utilizes a for loop to iterate and check through the entire size of 
	 * embeddedWords to determine the token with highest token rank. Once this is determined, it passes the highest value edge associated
	 * to the RecommandedTokenRelationshipUtil.getByEdgeName() method to set the has-Icd
	 * @param icdEdge
	 * @param discovery
	 */
	
	private void appendIcdEdge(String icdEdge, SentenceDiscovery discovery){
		
		//Rabhu added the following code to use the highestValueEdge instead of the existance in order to implement the has-Icd edge
		//******************************************************************************************************************************************************
		String highestValueEdge=null;
		int TokVal =0;
		String token = null;
		List<RecommendedTokenRelationship> embeddedWords = discovery.getWordEmbeddings();
		for (int i =0; i<embeddedWords.size(); i++) {
			RecommendedTokenRelationship recommendedTokenRelationship = embeddedWords.get(i);
			TokenRelationship relationship = recommendedTokenRelationship.getTokenRelationship();
			if(relationship.getFromToken().getTokenRanking()>relationship.getToToken().getTokenRanking() && relationship.getFromToken().getTokenRanking()>TokVal){
				highestValueEdge = relationship.getEdgeName(); 	//Tried but this caused typeMisMatch: highestValueToken = relationship.getFromToken();
				TokVal = relationship.getFromToken().getTokenRanking();
				token = relationship.getFromToken().getToken();
			}
			else if(relationship.getToToken().getTokenRanking()>relationship.getFromToken().getTokenRanking() && relationship.getToToken().getTokenRanking()>TokVal){
				highestValueEdge = relationship.getEdgeName(); 	//Tried but this caused typeMisMatch: highestValueToken = relationship.getFromToken();
				TokVal = relationship.getToToken().getTokenRanking();
				token = relationship.getToToken().getToken();
			}
						
		}
//		System.out.println();
//		System.out.println(highestValueEdge +" | " + token + " | " + TokVal);
		//*****************************************************************************************************************************************************
		//RecommendedTokenRelationship relationship = 
		//	 RecommandedTokenRelationshipUtil.getByEdgeName(discovery.getWordEmbeddings(),EdgeNames.existence);
		
		RecommendedTokenRelationship relationship = 
				 RecommandedTokenRelationshipUtil.getByEdgeName(discovery.getWordEmbeddings(),highestValueEdge );
		
		if(relationship==null) return; 
		
		WordToken toToken = new WordToken();
        //was: token.setToken(icdEdge);
		toToken.setToken(icdEdge);
		
		WordToken fromToken = relationship.getTokenRelationship().getFromToken();//was getToToken();
		
//		System.out.println(fromToken.getToken() + " | "+fromToken.getTokenRanking() +" | " + toToken.getToken() + " | " + toToken.getTokenRanking());
		

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
