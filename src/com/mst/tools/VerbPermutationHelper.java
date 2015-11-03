package com.mst.tools;

import com.mst.model.VerbQualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerbPermutationHelper {

	// all of these lists were patterned after Jan's spreadsheet here: https://docs.google.com/spreadsheets/d/1AVZ_wVXN7JPpaCRztQfh5u66cUwzTXkoTjSgoWnh_40/edit#gid=0
	// there may be a better way to do this but it works and was quick to develop!
	
	List<String> helpingVerbs = Arrays.asList("am", "is", "are", "was", "were", "had", "has", "have", "did", "do", "does", "done");
	List<String> modalVerbs = Arrays.asList("will", "shall", "may", "might", "can", "could", "should", "must");
	List<String> doubleHelpingModalVerbs = Arrays.asList("can have","did have","am having","are having","be having","been having","has had","have had","having had","is having","was having","were being","were having","will be","should be","would be","could be","had been","has been","have been","having been","may be","might be","must be","shall be");
	List<String> doubleHelpingModalPlusAVVerbs = Arrays.asList("am being","are being","can be","could be","could have","had been","had had","has been","have been","having been","is being","may be","may have","might be","might have","must be","must have","shall be","shall have","should be","should have","was being","will be","will have","would be","would have");
	List<String> tripleModalVerbs = Arrays.asList("can be having","can have been","can have had","could be having","could have had","had been being","had been having","has been being","has been having","have been being","have been having","may be having","may have had","might be having","might have had","must be having","must have had","shall be having","shall have been","shall have had","should be having","should have had","will be having","will have had","would be having","would have had","could have been","may have been","might have been","must have been","should have been","will have been","would have been");
	List<String> tripleModalPlusAVVerbs = Arrays.asList("could have been","may have been","might have been","must have been","should have been","will have been","would have been","might have had","must have had","would have had","should have had","will have had","may have had");
	List<VerbQualifier> singleHelping = new ArrayList<>();
	// the "Plus AV" lists will have every action verb in the input file appended to them and semantic types, qualifiers generated accordingly
	List<VerbQualifier> singleHelpingPlusAV = new ArrayList<>();
	List<VerbQualifier> singleModalPlusAV = new ArrayList<>();
	List<VerbQualifier> doubleHelpingModalPlusAV = new ArrayList<>();
	List<VerbQualifier> tripleModalPlusAV = new ArrayList<>();

	String delim = "_";
	// semantic types to link to all verb semantic types
	List<String> companionSTs = Arrays.asList("drugpr_Admin of Drug",
											  "drugoc_Admin of Drug",
											  "neop_Clinical Finding",
											  "neop-abn_Clinical Finding",
											  "neop-can_Clinical Finding",
											  "neop-les_Clinical Finding",
											  "neop-mets_Clinical Finding",
											  "neop-tum_Clinical Finding",
											  "dysn_Clinical Finding",
											  "sympto_Clinical Finding",
											  "prbymeth_Procedure By Method",
											  "patien_Subject",
											  "propn_Subject",
											  "sociid_Subject",
											  "provid_Subject",
											  "diap_Diagnostic Procedure",
											  "tempor_Known Event Date",
											  "number_Absolute Value",
											  "qlco_General Value"
											  );
	
	public VerbPermutationHelper() {
		// setup various lists to be consulted later
		// # singleHelping
		for(String verb : helpingVerbs) {
			if(verb.matches("am|is|are|has|do|does"))
				singleHelping.add(new VerbQualifier(verb, verb, "present", null, null, null));
			else if(verb.matches("was|were|had|have|did|done"))
				singleHelping.add(new VerbQualifier(verb, verb, "past", null, null, null));
		}
		
		// # singleHelpingPlusAV
		for(String verb : helpingVerbs) {
			if(verb.matches("am|is|are"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, null, null, "present"));
			else if(verb.matches("was|were"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, null, "past", "past"));
			else if(verb.matches("had|have"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, null, "past", null));
			else if(verb.matches("has"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, null, "recent past", null));
			else if(verb.matches("did|do|does"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, "present", null, null));
			else if(verb.matches("done"))
				singleHelpingPlusAV.add(new VerbQualifier(verb, null, null, "past", null, null));
		}
		
		// # singleModalPlusAV
		for(String verb : modalVerbs) {
			if(verb.matches("will"))
				singleModalPlusAV.add(new VerbQualifier(verb, null, null, "future", null, null));
			else if(verb.matches("shall|may|might|can|could|would|should"))
				singleModalPlusAV.add(new VerbQualifier(verb, null, null, "possibility", null, null));
			else if(verb.matches("must"))
				singleModalPlusAV.add(new VerbQualifier(verb, null, null, "obligation", null, null));
		}
		
		// # doubleHelpingModalPlusAV
		for(String verb : doubleHelpingModalPlusAVVerbs) {
			if(verb.matches("am being|are being|is being|was being"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "present", null));
			else if(verb.matches("may be|might be|can be|could be|shall be|should be"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "possibility", "possibility"));
			else if(verb.matches("may have|could have|might have|would be|would have|shall have|should have"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "possibility", null));
			else if(verb.matches("had been"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "past", "past"));
			else if(verb.matches("had had"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "past", null));
			else if(verb.matches("has been|have been"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "recent past", "recent past"));
			else if(verb.matches("must be"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "obligation", "obligation"));
			else if(verb.matches("must have"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "obligation", null));
			else if(verb.matches("was being"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "present", null));
			else if(verb.matches("will have"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "future", null));
			else if(verb.matches("will be"))
				doubleHelpingModalPlusAV.add(new VerbQualifier(verb, null, null, null, "future", "future"));
		}
		
		// # tripleModalPlusAV
		for(String verb : tripleModalPlusAVVerbs) {
			if(verb.matches("must have been"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "obligation-past", "obligation-past"));
			else if(verb.matches("must have had"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "obligation-past", null));
			else if(verb.matches("may have been|might have been|should have been|would have been|could have been"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "possibility-past", "possibility-past"));
			else if(verb.matches("will have been"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "future-present", "future-present"));
			else if(verb.matches("might have had|would have had|may have had|should have had"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "possibility-past", null));
			else if(verb.matches("will have had"))
				tripleModalPlusAV.add(new VerbQualifier(verb, null, null, null, "future-past", null));
		}
	}

	public List<String> getCompanionSTs() {
		return this.companionSTs;
	}
	
	public void permuteVerb(String verb, Set<String> uniqueSTQ, List<String[]> semanticTypes) {
		//Set<String> uniqueSTQ = new HashSet<>(); // unique combos of semantic type and qualifier
		//List<String[]> semanticTypes = new ArrayList<>();
		
		String[] verbs = verb.split("\\s*,\\s*"); // inf, past part, present part, -s
		
		// single helping plus AV
		for(VerbQualifier vq : singleHelpingPlusAV) {
			if(vq.inf != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[0], verbs[0] + delim + vq.inf });
				uniqueSTQ.add(verbs[0] + delim + vq.inf);
			}
			if(vq.pastPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[1], verbs[0] + delim + vq.pastPart });
				uniqueSTQ.add(verbs[0] + delim + vq.pastPart);
			} 
			if(vq.presentPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[2], verbs[0] + delim + vq.presentPart });
				uniqueSTQ.add(verbs[0] + delim + vq.presentPart);
			}
		}
		
		// single modal plus AV
		for(VerbQualifier vq : singleModalPlusAV) {
			if(vq.inf != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[0], verbs[0] + delim + vq.inf });
				uniqueSTQ.add(verbs[0] + delim + vq.inf);
			}
		}
		
		// double helping/modal plus AV
		for(VerbQualifier vq : doubleHelpingModalPlusAV) {
			if(vq.pastPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[1], verbs[0] + delim + vq.pastPart });
				uniqueSTQ.add(verbs[0] + delim + vq.pastPart);
			} 
			if(vq.presentPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[2], verbs[0] + delim + vq.presentPart });
				uniqueSTQ.add(verbs[0] + delim + vq.presentPart);
			}
		}
		
		// triple helping/modal plus AV
		for(VerbQualifier vq : tripleModalPlusAV) {
			if(vq.pastPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[1], verbs[0] + delim + vq.pastPart });
				uniqueSTQ.add(verbs[0] + delim + vq.pastPart);
			} if(vq.presentPart != null) {
				semanticTypes.add(new String[] { vq.verb + " " + verbs[2], verbs[0] + delim + vq.presentPart });
				uniqueSTQ.add(verbs[0] + delim + vq.presentPart);
			}
		}
		
		// verbs alone (no helping, modal, or compound)
		semanticTypes.add(new String[] { verbs[0], verbs[0] + delim + "present" });
		uniqueSTQ.add(verbs[0] + delim + "present");

		// past (-ed)
		semanticTypes.add(new String[] { verbs[1], verbs[0] + delim + "past" });
		uniqueSTQ.add(verbs[0] + delim + "past");

		// gerund (-ing)
		semanticTypes.add(new String[] { verbs[2], verbs[0] + delim + "present" });
		
		// present (with ending -s) ex. starts
		semanticTypes.add(new String[] { verbs[3], verbs[0] + delim + "present" });
		
	}
}
