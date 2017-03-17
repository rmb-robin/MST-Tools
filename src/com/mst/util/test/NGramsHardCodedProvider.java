package com.mst.util.test;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.NGramsProvider;
import com.mst.model.gentwo.NGramsModifierEntity;

public class NGramsHardCodedProvider implements NGramsProvider {

	public List<NGramsModifierEntity> getNGrams() {
		List<NGramsModifierEntity> result = new ArrayList<>();
		result.add(getEntity("follow up","follow-up"));
		result.add(getEntity("up to date","current"));
		result.add(getEntity("caught up","caught-up"));
		result.add(getEntity("status post","after"));
		result.add(getEntity("time senstive","urgent"));
		result.add(getEntity("add up","compute"));
		result.add(getEntity("prior to","before"));
		result.add(getEntity("as a result of","because of"));
		result.add(getEntity("due to","because of"));
		result.add(getEntity("ultrasound guided biopsy","ultrasound-guided-biopsy"));
		result.add(getEntity("Bone marrow biopsy","Bone-marrow-biopsy"));
		return result;
	}
	
	private NGramsModifierEntity getEntity(String original, String modified){
		NGramsModifierEntity entity = new NGramsModifierEntity();
		entity.setOriginalStatement(original);
		entity.setModifiedStatement(modified);
		return entity;
	}

}
