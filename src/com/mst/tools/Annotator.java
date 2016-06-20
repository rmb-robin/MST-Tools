package com.mst.tools;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.mst.model.Sentence;
import com.mst.model.SentenceToken;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class Annotator {

	private final Pattern PUNC = Pattern.compile(",|\\(|\\)|-");
	private final Pattern PROPN = Pattern.compile("^[A-Z][a-z]+");
	private final Pattern SHOWS = Pattern.compile("shows?", Pattern.CASE_INSENSITIVE);
	private final Pattern NOUN_OVERRIDES = Pattern.compile("ct|dexa", Pattern.CASE_INSENSITIVE);
	private final String ST_PREFIX = "st:";
	private final String STVB_PREFIX = "st:vb:";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// The purpose of this class/method is to ensure that the annotation steps proceed in the 
	// correct order (primarily for the sake of the verb classes).
	//
	// It mimics the Camel process, for bench testing purposes. It's also used by the web utility.
	public ArrayList<Sentence> annotate(String text, boolean useStanfordPOS) {
		
		ArrayList<Sentence> output = new ArrayList<Sentence>();
		
		String clientId = null;
		String id = null;
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

				annotateSentence(sentence);
				output.add(sentence);
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
		POSTagger tagger = new POSTagger();
		DependentPhraseHelper dep = new DependentPhraseHelper();
		
		try {
			//logger.info("Beginning annotation for ID: " + sentence.getId());
			// begin annotation process
			// 1. get parts of speech
			if(tagger.identifyPartsOfSpeech(sentence)) {
				for(WordToken word : sentence.getWordList()) {
					WordToken prevWord = new WordToken();
					
					try {
						prevWord = sentence.getWordList().get(word.getPosition()-2);
					} catch(IndexOutOfBoundsException  e) { }
					
					// 1a. POS overrides
					// TODO get POS overrides into POSTagger
					if(PUNC.matcher(word.getToken()).matches()) { // Stanford tags these as NN
						word.setPOS(word.getToken()); // reset POS to match punctuation char
					} else if(word.getToken().equalsIgnoreCase("scan")) {
						// override the POS of scan (Stanford = VB) to NN if preceded by ST diap or token "bone"
						if(prevWord.getToken().equalsIgnoreCase("bone") ||  // equalsIgnoreCase("bone") is faster than match on "(?i)bone"
						   (prevWord.getSemanticType() != null && prevWord.getSemanticType().equalsIgnoreCase("diap"))) {
							word.setPOS("NN");
						}
					} else if(SHOWS.matcher(word.getToken()).matches()) {
						word.setPOS("VB"); // ensure that show and shows are tagged as a verb
					} else if(NOUN_OVERRIDES.matcher(word.getToken()).matches()) {
						word.setPOS("NN"); // certain tokens should always be nouns
					} else if(word.isVerb()) {
						// override tokens that Stanford possibly erroneously tags as verbs
						// must be a verb that ends in -ed or -ing and preceded by a preposition
						if(prevWord.isPreposition()) {
							if(word.getToken().endsWith("ed") || word.getToken().endsWith("ing"))
								word.setPOS("JJ");
							else
								word.setPOS("NN");
						}
					}
					
					// 2. get semantic types from Redis
					String val;
					try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
						val = jedis.get((word.isVerb() ? STVB_PREFIX : ST_PREFIX) + word.getToken().toLowerCase());
					}
					
					// 2a. semantic type overrides/failovers
					if(val == null) {
						// if token is a noun that begins with an uppercase letter followed by a lowercase letter and the preceding token is a salutation
						if(word.isNounPOS() && PROPN.matcher(word.getToken()).matches())
							if(Constants.SALUTATION_REGEX.matcher(prevWord.getToken()).matches())
								val = Constants.semanticTypes.get("[proper noun]");
						else if(Constants.AGE_REGEX.matcher(word.getToken()).matches())
							val = Constants.semanticTypes.get("[age]");
						else if(word.isNumericPOS())
							if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
								val = Constants.semanticTypes.get("[date]");
							else if(Constants.TIME_REGEX.matcher(word.getToken()).matches())
								val = Constants.semanticTypes.get("[time]");
							else
								val = Constants.semanticTypes.get("[number]");
						else if(Constants.DATE_REGEX_ST.matcher(word.getToken()).matches())
							// Stanford tags dates as CD. this is a fail-safe in case a format comes across that Stanford tags as something else.
							val = Constants.semanticTypes.get("[date]");
						else if(Constants.TNM_STAGING_REGEX_NO_SPACE.matcher(word.getToken()).matches())
							val = Constants.semanticTypes.get("[tnm]");
						else if(Constants.MEASUREMENT_REGEX.matcher(word.getToken()).matches())
							val = Constants.semanticTypes.get("[number]");
						else if(word.getToken().matches("(?i)AM|PM")) {
							if(Constants.TIME_REGEX.matcher(prevWord.getToken()).matches())
								val = Constants.semanticTypes.get("[time]");
						}
					}
					word.setSemanticType(val);
				}
				
				// beginning dep phrases must be defined before prep phrases
				dep.processBeginningBoundaries(sentence);
				
				// infinitive phrase logic relies on POS
				// verbs.identifyInfinitivePhrases(sentence);
	
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
				// verbs.identifyPrepositionalVerbs(sentence);
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
