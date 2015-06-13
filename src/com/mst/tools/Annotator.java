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
	// It mimics the Camel process, for bench testing purposes.
	public ArrayList<Sentence> annotate(String text, boolean useStanfordPOS) {
		
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		
		String clientId = "X";
		String id = "dep_test";
		Constants.Source source = Constants.Source.UNKNOWN; 
		
		try {
			Tokenizer t = new Tokenizer();
			VerbHelper verbs = new VerbHelper();
			NounHelper nouns = new NounHelper();
			PrepositionHelper preps = new PrepositionHelper();
			POSTagger tagger;
			if(useStanfordPOS)
				tagger = new POSTagger(true);
			else
				tagger = new POSTagger();
			SentenceCleaner cleaner = new SentenceCleaner();
			//MetaMapWrapper mm = new MetaMapWrapper();
			DependentPhraseHelper dep = new DependentPhraseHelper();
			
			ArrayList<SentenceToken> stList = t.splitSentences(text);
			int position = 0;
			for(SentenceToken st : stList) {
				String cs = cleaner.cleanSentence(st.getToken());
				Sentence sentence = new Sentence(id, position++, t.splitWords(cs));
				
				sentence.setPractice(clientId);
				sentence.setSource(source.toString());
				sentence.setFullSentence(cs);
				
				// begin annotation process
				if(tagger.identifyPartsOfSpeech(sentence)) {
					int idx = 0;
					
					//mm.getSemanticTypes(words, text);
					for(WordToken word : sentence.getWordList()) {
						// set semantic type; possible override
						String val = Constants.semanticTypes.get(word.getToken().toLowerCase());
						if(val == null) {
							if(word.isNounPOS() && word.getToken().matches("^[A-Z][a-z]+"))
								val = Constants.semanticTypes.get("[proper noun]");
							else if(word.getToken().matches(Constants.AGE_REGEX))
								val = Constants.semanticTypes.get("[age]");
							else if(word.isNumericPOS())
								if(word.getToken().matches(Constants.DATE_REGEX_ST))
									val = Constants.semanticTypes.get("[date]");
								else
									val = Constants.semanticTypes.get("[number]");
							else if(word.getToken().matches(Constants.DATE_REGEX_ST))
								// Stanford tags dates as CD. this is a fail-safe in case a format comes across that Stanford tags as something else.
								val = Constants.semanticTypes.get("[date]");
						}
						word.setSemanticType(val);
						
						// POS overrides
						if(word.getToken().matches(",|\\(|\\)|-")) { // Stanford tags these as NN
							word.setPOS(word.getToken());
						} else if(word.getToken().matches("(?i)scan")) {
							try {
								if(sentence.getWordList().get(idx-1).getToken().matches("bone") ||
								   sentence.getWordList().get(idx-1).getSemanticType().matches("diap")) {
									// override the POS of scan (Stanford: VB) to NN if preceded by ST diap or token "bone"
									word.setPOS("NN");
								}
							} catch(Exception e) { }
						}
						
						idx++;
					}
					
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
					
					// requires POS
					verbs.identifyModalAuxiliaryVerbs(sentence);
					// VOB requires POS and prep phrases and modal aux
					verbs.identifyVerbsOfBeing(sentence);
					// LV requires POS, prep phrases, VOB, modal aux
					verbs.identifyLinkingVerbs(sentence);
					// PrepV requires POS, prep phrases
					verbs.identifyPrepositionalVerbs(sentence);
					// AV requires POS, VOB, LV, InfV, PrepV (basically, if it's a verb but none of the other types, it's an action verb)
					// modal aux used in identifying the subject
					verbs.identifyActionVerbs(sentence);
										
					sentences.add(sentence);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return sentences;
	}
}
