package com.mst.tools;

import java.util.ArrayList;

import com.mst.model.Sentence;
import com.mst.model.SentenceToken;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class Annotator {

	// The purpose of this class/method is to ensure that the annotation steps proceed in the 
	// correct order (primarily for the sake of the verb classes).
	//
	// It loosely mimics the Camel process, for bench testing purposes.
	public ArrayList<Sentence> annotate(String text) {
		
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		
		String clientId = "X";
		String id = "dep_test";
		Constants.Source source = Constants.Source.UNKNOWN; 
		
		try {
			Tokenizer t = new Tokenizer();
			VerbHelper verbs = new VerbHelper();
			NounHelper nouns = new NounHelper();
			PrepositionHelper preps = new PrepositionHelper();
			POSTagger tagger = new POSTagger();
			SentenceCleaner cleaner = new SentenceCleaner();
			//MetaMapWrapper mm = new MetaMapWrapper();
			DependentPhraseHelper dep = new DependentPhraseHelper();
			
			ArrayList<SentenceToken> stList = t.splitSentences(text);
			int position = 0;
			for(SentenceToken st : stList) {
				String cs = cleaner.cleanSentence(st.getToken());
				Sentence sentence = new Sentence(id, position++, t.splitWords(cs));
				sentence.setClientId(clientId);
				sentence.setSource(source.toString());
				sentence.setFullSentence(cs);
				
				// begin annotation process
				tagger.identifyPartsOfSpeech(sentence);
				
				// beginning dep phrases must be defined before prep phrases
				dep.processBeginningBoundaries(sentence);
				
				// infinitive phrase logic relies on POS
				verbs.identifyInfinitivePhrases(sentence);

				nouns.identifyNounPhrases(sentence);
				// prep phrase logic ignores prepositions that have been identified as infinitive head
				preps.identifyPrepPhrases(sentence);
				
				// after prep phrase logic, unset potential dependent phrase heads
				dep.unsetBeginningBoundaries(sentence);
				
				dep.processEndingBoundaries2(sentence);
				
				// VOB requires POS and prep phrases
				verbs.identifyVerbsOfBeing(sentence);
				// LV requires POS, prep phrases, VOB
				verbs.identifyLinkingVerbs(sentence);
				// PrepV requires POS, prep phrases
				verbs.identifyPrepositionalVerbs(sentence);
				// requires POS
				verbs.identifyModalAuxiliaryVerbs(sentence);
				// AV requires POS, VOB, LV, InfV, PrepV (basically, if it's a verb but none of the other types, it's an action verb)
				// modal aux used in identifying the subject
				verbs.identifyActionVerbs(sentence);
				
				// TODO can ST occur anywhere? 
				//mm.getSemanticTypes(words, text);
				
				sentences.add(sentence);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return sentences;
	}
}
