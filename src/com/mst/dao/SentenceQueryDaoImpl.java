package com.mst.dao;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.MongoConnectionProvider;

public class SentenceQueryDaoImpl implements SentenceQueryDao  {
	
	private MongoDatastoreProvider datastoreProvider;
	
	public List<SentenceDb> getSentences(SentenceQueryInput input){
		List<SentenceDb> sentences = new ArrayList<>();
		Datastore datastore =  datastoreProvider.getDataStore();
		for(String token: input.getTokens()){
			Query<SentenceDb> query = datastore.createQuery(SentenceDb.class);
			 query
			 .field("origSentence").contains(token)
			 .field("tokenRelationships.edgeName").equal(input.getEdgeName());
			 List<SentenceDb> currentSentences = query.asList();
			 sentences.addAll(filterForToken(currentSentences, token, input.getEdgeName()));
		}
		return sentences;
	}	

	private List<SentenceDb> filterForToken(List<SentenceDb> sentences, String token, String edgeName){
		List<SentenceDb> result = new ArrayList<>();
		
		for(SentenceDb sentenceDb : sentences){
			for(TokenRelationship relation: sentenceDb.getTokenRelationships()){
				if(shouldAddTokenFromRelationship(relation,token, edgeName))
				{	result.add(sentenceDb);
					break;
				}
			}
		}
		return result;
	}
	
	private boolean shouldAddTokenFromRelationship(TokenRelationship relation, String token, String edgeName){
		if(!relation.getEdgeName().equals(edgeName)) return false;
		if(relation.getFromToken().getToken().equals(token)) return true;
		if(relation.getToToken().getToken().equals(token)) return true;
		return false;
	}

	@Override
	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		this.datastoreProvider = provider;
		
	}
}