package com.mst.filter;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.filter.NotAndAllRequestFactory;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryInstance;
import com.mst.model.metadataTypes.EdgeNames;

public class NotAndAllRequestFactoryImpl implements NotAndAllRequestFactory {

	@Override
	public SentenceQueryInput create(SentenceQueryInput input) {
		SentenceQueryInstance instance =  input.getSentenceQueryInstances().get(0);
		input.getSentenceQueryInstances().addAll(0,createPermitations(instance));
		instance.setAppender("andnotall");
		List<String> newTokens = new ArrayList<>();
		newTokens.add("$$");
		instance.setTokens(newTokens);
		return input;
	}
	
	
	private List<SentenceQueryInstance> createPermitations(SentenceQueryInstance andNotAllInstance){
		List<EdgeQuery> edgeQueries = andNotAllInstance.getEdges();
		EdgeQuery existenceEdge = findExistenceEdge(edgeQueries);
		if(existenceEdge!=null)
			edgeQueries.remove(existenceEdge);
	
		int maxNumberOfEdges = edgeQueries.size()-1;
		List<SentenceQueryInstance> result; 
		if(maxNumberOfEdges<=1)
			result = processTwoOrLess(edgeQueries,andNotAllInstance,maxNumberOfEdges,existenceEdge);
		else 
			result = processTwoOrMore(edgeQueries,andNotAllInstance,maxNumberOfEdges,existenceEdge);
		edgeQueries.add(existenceEdge);
		return result;
	}

	private List<SentenceQueryInstance> processTwoOrMore(List<EdgeQuery> edgeQueries,SentenceQueryInstance andNotAllInstance,int maxNumberOfEdges,	EdgeQuery existenceEdge){
		List<SentenceQueryInstance> result = new ArrayList<>();
		for(int i =0; i<edgeQueries.size();i++){
			for(int j = i+1;j<edgeQueries.size();j++){
				SentenceQueryInstance instance = new SentenceQueryInstance();
				instance.setTokens(andNotAllInstance.getTokens());
				if(i>0)
					instance.setAppender("or");
				instance.getEdges().add(edgeQueries.get(i));
				int k = j;
				while(instance.getEdges().size()< maxNumberOfEdges){
					instance.getEdges().add(edgeQueries.get(k));
					k+=1;
				}
				instance.getEdges().add(existenceEdge);
				result.add(instance);
			}
		}	
		return result;
	} 
	
	private List<SentenceQueryInstance> processTwoOrLess(List<EdgeQuery> edgeQueries,SentenceQueryInstance andNotAllInstance,int maxNumberOfEdges,	EdgeQuery existenceEdge){
		List<SentenceQueryInstance> result = new ArrayList<>();
		for(int i =0; i<edgeQueries.size();i++){
			SentenceQueryInstance instance = new SentenceQueryInstance();
			instance.setTokens(andNotAllInstance.getTokens());
			if(i>0)
				instance.setAppender("or");
			instance.getEdges().add(edgeQueries.get(i));
			
			int j=i+1;
			
			while(instance.getEdges().size()< maxNumberOfEdges){
				instance.getEdges().add(edgeQueries.get(j));
				j+=1;
			}
			instance.getEdges().add(existenceEdge);
			result.add(instance);
		}
		return result;
	}
	
	
		
	private EdgeQuery findExistenceEdge(List<EdgeQuery> edgeQueries){
		for(EdgeQuery edgeQuery: edgeQueries){
			if(edgeQuery.getName().equals(EdgeNames.existence)) 
				return edgeQuery;
			
			if(edgeQuery.getName().equals(EdgeNames.existenceNo))
				return edgeQuery;
		}
		return null;
	}
	
	
	

	
	

}
