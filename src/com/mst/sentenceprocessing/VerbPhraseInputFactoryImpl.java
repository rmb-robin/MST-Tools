package com.mst.sentenceprocessing;

import java.util.HashSet;

import com.mst.interfaces.sentenceprocessing.VerbPhraseInputFactory;
import com.mst.model.sentenceProcessing.VerbPhraseInput;

public class VerbPhraseInputFactoryImpl implements VerbPhraseInputFactory {

	private HashSet<String> firstWordSubjects;
	private HashSet<String> stTypes;
	
	public VerbPhraseInput create() {
		VerbPhraseInput input = new VerbPhraseInput();
		
		firstWordSubjects = new HashSet<>();
		stTypes = new HashSet<>();
		
		firstWordSubjects.add("there");
		firstWordSubjects.add("this");
		firstWordSubjects.add("it");
		
		stTypes.add("DYSN");
		stTypes.add("proc"); 
		stTypes.add("drugpr");		
		stTypes.add("cardinal number"); 
		stTypes.add("bpoc"); 
		
		input.setFirstWordSubjects(firstWordSubjects);
		input.setStTypes(stTypes);
		return input;	
	}
}
