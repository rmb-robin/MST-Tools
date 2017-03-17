package com.mst.util.test;

import org.junit.Test;

import com.mst.model.gentwo.NounRelationship;
import com.mst.model.gentwo.NounRelationshipInput;

public class NounRelationshipProcessorTest {

	@Test
	public void process(){
		NounRelationshipInputProviderFileImpl fileImpl = new NounRelationshipInputProviderFileImpl();
		NounRelationshipInput input = fileImpl.get("f_related",7);
	
		
	}
	
	
	
	
	
	
	
	
	private void WriteInput(NounRelationshipInput input ){
		for(NounRelationship r : input.getNounRelationships()){
			System.out.println(r.getFromToken());
			System.out.println(r.getIsFromSemanticType());
		
			System.out.println(r.getToToken());
			System.out.println(r.getIsToSemanticType());

			System.out.println(r.getMaxDistance());
			System.out.println(r.getEdgeName());
			System.out.println("**************************************");
		}
	}
	
}
