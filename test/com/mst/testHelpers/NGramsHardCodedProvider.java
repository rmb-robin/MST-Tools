package com.mst.testHelpers;

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
		
		result.add(getEntity("can have","can-have"));
		result.add(getEntity("did be","did-be"));
		result.add(getEntity("did have","did-have"));
		result.add(getEntity("am having","am-having"));
		result.add(getEntity("are having",	"are-having"));
		result.add(getEntity("be having", "be-having"));
		result.add(getEntity("been having","been-having"));
		result.add(getEntity("has had","has-had"));
		result.add(getEntity("have had","have-had"));
		result.add(getEntity("having had","having-had"));
		result.add(getEntity("is having", "is-having"));
		result.add(getEntity("was having",	"was-having"));
		result.add(getEntity("were being","were-being"));
		result.add(getEntity("were having",	"were-having"));
		result.add(getEntity("can be having","can-be-having"));
		result.add(getEntity("can have been","can-have-been"));
		result.add(getEntity("can have had","can-have-had"));
		result.add(getEntity("could be having",	"could-be-having"));
		result.add(getEntity("could have had","could-have-had"));
		result.add(getEntity("had been being","had-been-being"));
		result.add(getEntity("had been having","had-been-having"));
		result.add(getEntity("has been being",	"has-been-being"));
		result.add(getEntity("has been having","has-been-having"));
		result.add(getEntity("have been being","have-been-being"));
		result.add(getEntity("have been having","have-been-having"));
		result.add(getEntity("may be having","may-be-having"));
		result.add(getEntity("may have had","may-have-had"));
		result.add(getEntity("might be having",	"might-be-having"));
		result.add(getEntity("might have had","might-have-had"));
		result.add(getEntity("must be having","must-be-having"));
		result.add(getEntity("must have had","must-have-had"));
		result.add(getEntity("shall be having","shall-be-having"));
		result.add(getEntity("shall have been","shall-have-been"));
		result.add(getEntity("shall have had","shall-have-had"));
		result.add(getEntity("should be having","should-be-having"));
		result.add(getEntity("should have had","should-have-had"));
		result.add(getEntity("will be having","will-be-having"));
		result.add(getEntity("would be having","would-be-having"));
		result.add(getEntity("am being","am-being"));
		result.add(getEntity("are being","are-being"));
		result.add(getEntity("can be","can-be"));
		result.add(getEntity("could be","could-be"));
		result.add(getEntity("could have","could-have"));
		result.add(getEntity("had been","had-been"));
		result.add(getEntity("had had","had-had"));
		result.add(getEntity("has been","has-been"));
		result.add(getEntity("have been","have-been"));
		result.add(getEntity("having been","having-been"));
		result.add(getEntity("is being","is-being"));
		result.add(getEntity("may be","may-be"));
		result.add(getEntity("may have","may-have"));
		result.add(getEntity("might be","might-be"));
		result.add(getEntity("might have","might-have"));
		result.add(getEntity("must be","must-be"));
		result.add(getEntity("must have","must-have"));
		result.add(getEntity("shall be","shall-be"));
		result.add(getEntity("shall have","shall-have"));
		result.add(getEntity("should be","should-be"));
		result.add(getEntity("should have","should-have"));
		result.add(getEntity("was being","was-being"));
		result.add(getEntity("will be","will-be"));
		result.add(getEntity("will have","will-have"));
		result.add(getEntity("would be","would-be"));
		result.add(getEntity("would have","would-have"));
		result.add(getEntity("could have been","could-have-been"));
		result.add(getEntity("may have been","may-have-been"));
		result.add(getEntity("might have been","might-have-been"));
		result.add(getEntity("must have been","must-have-been"));
		result.add(getEntity("should have been","should-have-been"));
		result.add(getEntity("will have been","will-have-been"));
		result.add(getEntity("would have been","would-have-been"));
		result.add(getEntity("would have had","would-have-had"));
		result.add(getEntity("will have had","will-have-had"));
		return result;
	}
	
	private NGramsModifierEntity getEntity(String original, String modified){
		NGramsModifierEntity entity = new NGramsModifierEntity();
		entity.setOriginalStatement(original);
		entity.setModifiedStatement(modified);
		return entity;
	}

}
