package com.mst.tools;

import java.util.ArrayList;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;

import com.mst.model.Sentence;
import com.mst.model.SentenceToken;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class Annotator {

	private final Pattern PUNC = Pattern.compile(",|\\(|\\)|-");
	private final Pattern PROPN = Pattern.compile("^[A-Z][a-z]+");
	private final Pattern SHOWS = Pattern.compile("shows?", Pattern.CASE_INSENSITIVE);
	
	// The purpose of this class/method is to ensure that the annotation steps proceed in the 
	// correct order (primarily for the sake of the verb classes).
	//
	// It mimics the Camel process, for bench testing purposes. It's also used by the web utility.
	public ArrayList<Sentence> annotate(String text, boolean useStanfordPOS) {
		
		ArrayList<Sentence> output = new ArrayList<Sentence>();
		
		String clientId = "test";
		String id = "test";
		Constants.Source source = Constants.Source.UNKNOWN; 
		
		try {
			Tokenizer t = new Tokenizer();
			SentenceCleaner cleaner = new SentenceCleaner();
			
			ArrayList<SentenceToken> sentenceTokens = t.splitSentences(text);
			int position = 0;
			
			for(SentenceToken sentenceToken : sentenceTokens) {
				String cs = cleaner.cleanSentence(sentenceToken.getToken());
				Sentence sentence = new Sentence(id, position++, t.splitWords(cs));
				
				sentence.setPractice(clientId);
				sentence.setSource(source.toString());
				sentence.setFullSentence(cs);
				sentence.setOrigSentence(sentenceToken.getToken());
				
//				// begin annotation process
//				if(tagger.identifyPartsOfSpeech(sentence)) {
//					int idx = 0;
//					
//					for(WordToken word : sentence.getWordList()) {
//						// POS overrides
//						if(PUNC.matcher(word.getToken()).matches()) { // Stanford tags these as NN
//							word.setPOS(word.getToken());
//						} else if(word.getToken().equalsIgnoreCase("scan")) {
//							try {
//								// override the POS of scan (Stanford = VB) to NN if preceded by ST diap or token "bone"
//								if(sentence.getWordList().get(idx-1).getToken().equalsIgnoreCase("bone") ||  // equalsIgnoreCase("bone") is faster than match on "(?i)bone"
//										sentence.getWordList().get(idx-1).getSemanticType().equalsIgnoreCase("diap")) {
//									word.setPOS("NN");
//								}
//							} catch(Exception e) { }
//						} else if(SHOWS.matcher(word.getToken()).matches()) {
//							word.setPOS("VB");
//						}
//						
//						idx++;
//						
//						// set semantic type; possible override
//						String val;// = jedis.get("st:"+word.getToken().toLowerCase());
//						try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
//							val = jedis.get((word.isVerb() ? "st:vb:" : "st:") + word.getToken().toLowerCase());
//						}
//						
//						if(val == null) {
//							if(word.isNounPOS() && PROPN.matcher(word.getToken()).matches())
//								val = Constants.semanticTypes.get("[proper noun]");
//							else if(Constants.AGE_REGEX.matcher(word.getToken()).matches())
//								val = Constants.semanticTypes.get("[age]");
//							else if(word.isNumericPOS())
//								if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
//									val = Constants.semanticTypes.get("[date]");
//								else
//									val = Constants.semanticTypes.get("[number]");
//							else if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
//								// Stanford tags dates as CD. this is a fail-safe in case a format comes across that Stanford tags as something else.
//								val = Constants.semanticTypes.get("[date]");
//						}
//						word.setSemanticType(val);						
//					}
//					
//					// beginning dep phrases must be defined before prep phrases
//					dep.processBeginningBoundaries(sentence);
//					
//					// infinitive phrase logic relies on POS
////					verbs.identifyInfinitivePhrases(sentence);
//	
//					nouns.identifyNounPhrases(sentence);
//					// prep phrase logic ignores prepositions that have been identified as infinitive head
//					preps.identifyPrepPhrases(sentence);
//					
//					// after prep phrase logic, unset potential dependent phrase heads
//					dep.unsetBeginningBoundaries(sentence);
//					
//					dep.processEndingBoundaries2(sentence);
//					
//					// requires POS
//					verbs.identifyModalAuxiliaryVerbs2(sentence);
//					// VOB requires POS and prep phrases and modal aux
//					verbs.identifyVerbsOfBeing2(sentence);
//					// LV requires POS, prep phrases, VOB, modal aux
//					verbs.identifyLinkingVerbs(sentence);
//					// PrepV requires POS, prep phrases
////					verbs.identifyPrepositionalVerbs(sentence);
//					// AV requires POS, VOB, LV, InfV, PrepV (basically, if it's a verb but none of the other types, it's an action verb)
//					// modal aux used in identifying the subject
//					verbs.identifyActionVerbs(sentence);
					
					annotateSentence(sentence);
					output.add(sentence);
//				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public void annotateSentence(Sentence sentence) {
		VerbHelper verbs = new VerbHelper();
		NounHelper nouns = new NounHelper();
		PrepositionHelper preps = new PrepositionHelper();
		POSTagger tagger = new POSTagger(true);
		DependentPhraseHelper dep = new DependentPhraseHelper();
		
		try {
			// begin annotation process
			if(tagger.identifyPartsOfSpeech(sentence)) {
				int idx = 0;
				
				for(WordToken word : sentence.getWordList()) {
					// POS overrides
					// TODO get POS overrides into POSTagger
					if(PUNC.matcher(word.getToken()).matches()) { // Stanford tags these as NN
						word.setPOS(word.getToken());
					} else if(word.getToken().equalsIgnoreCase("scan")) {
						try {
							// override the POS of scan (Stanford = VB) to NN if preceded by ST diap or token "bone"
							if(sentence.getWordList().get(idx-1).getToken().equalsIgnoreCase("bone") ||  // equalsIgnoreCase("bone") is faster than match on "(?i)bone"
							   sentence.getWordList().get(idx-1).getSemanticType().equalsIgnoreCase("diap")) {
								word.setPOS("NN");
							}
						} catch(Exception e) { }
					} else if(SHOWS.matcher(word.getToken()).matches()) {
						word.setPOS("VB");
					}
					
					idx++;
					
					// set semantic type; possible override
					String val;// = jedis.get("st:"+word.getToken().toLowerCase());
					try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
						val = jedis.get((word.isVerb() ? "st:vb:" : "st:") + word.getToken().toLowerCase());
					}
					
					if(val == null) {
						if(word.isNounPOS() && PROPN.matcher(word.getToken()).matches())
							val = Constants.semanticTypes.get("[proper noun]");
						else if(Constants.AGE_REGEX.matcher(word.getToken()).matches())
							val = Constants.semanticTypes.get("[age]");
						else if(word.isNumericPOS())
							if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
								val = Constants.semanticTypes.get("[date]");
							else
								val = Constants.semanticTypes.get("[number]");
						else if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
							// Stanford tags dates as CD. this is a fail-safe in case a format comes across that Stanford tags as something else.
							val = Constants.semanticTypes.get("[date]");
					}
					word.setSemanticType(val);						
				}
				
				// beginning dep phrases must be defined before prep phrases
				dep.processBeginningBoundaries(sentence);
				
				// infinitive phrase logic relies on POS
	//			verbs.identifyInfinitivePhrases(sentence);
	
				nouns.identifyNounPhrases(sentence);
				// prep phrase logic ignores prepositions that have been identified as infinitive head
				preps.identifyPrepPhrases(sentence);
				
				// after prep phrase logic, unset potential dependent phrase heads
				dep.unsetBeginningBoundaries(sentence);
				
				dep.processEndingBoundaries2(sentence);
				
				// requires POS
				verbs.identifyModalAuxiliaryVerbs2(sentence);
				// VOB requires POS and prep phrases and modal aux
				verbs.identifyVerbsOfBeing2(sentence);
				// LV requires POS, prep phrases, VOB, modal aux
				verbs.identifyLinkingVerbs(sentence);
				// PrepV requires POS, prep phrases
	//			verbs.identifyPrepositionalVerbs(sentence);
				// AV requires POS, VOB, LV, InfV, PrepV (basically, if it's a verb but none of the other types, it's an action verb)
				// modal aux used in identifying the subject
				verbs.identifyActionVerbs(sentence);
			}
		} catch(Exception e) {
			e.printStackTrace();
			sentence = null;
		}
	}
}
