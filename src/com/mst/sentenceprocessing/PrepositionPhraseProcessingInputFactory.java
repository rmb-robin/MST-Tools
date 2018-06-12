package com.mst.sentenceprocessing;

import java.util.HashSet;

import com.mst.model.sentenceProcessing.PrepositionPhraseProcessingInput;

public class PrepositionPhraseProcessingInputFactory {

	public PrepositionPhraseProcessingInput create(){
		PrepositionPhraseProcessingInput phraseProcessingInput = new PrepositionPhraseProcessingInput();
		
		HashSet<String> punctuations = new HashSet<>();
		HashSet<String> stLookups = new HashSet<>();
	
		punctuations.add(";");
		punctuations.add(".");
		
		stLookups.add("DYSN");
		stLookups.add("drugpr");
		stLookups.add("proc");
		stLookups.add("bpoc");
		
		phraseProcessingInput.setPunctuations(punctuations);
		phraseProcessingInput.setStLookups(stLookups);
		phraseProcessingInput.setRange(7);
		return phraseProcessingInput;
	}
}
