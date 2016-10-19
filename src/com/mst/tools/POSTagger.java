package com.mst.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.util.Constants;
import com.mst.util.Props;
import com.mst.util.StanfordNLP;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POSTagger {

	private Map<String, String> translateMap = new HashMap<String, String>();
	private static MaxentTagger tagger = null;
	private VerbHelper verbHelper = new VerbHelper();
	
	private final Pattern PUNC = Pattern.compile(",|\\(|\\)|-|\\[|]|\\{|}|<|>|=");
	private final Pattern SHOWS = Pattern.compile("shows?", Pattern.CASE_INSENSITIVE);
	private final Pattern NOUN_OVERRIDES = Pattern.compile("ct|dexa", Pattern.CASE_INSENSITIVE);
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	//public final Pattern BRACKETS = Pattern.compile("\\[|\\]|\\{|\\}"); // Stanford POS does odd things with brackets
	
	public POSTagger() {
		try {
			init();
			
			if(tagger == null) {
				tagger = new MaxentTagger(Props.getProperty("stanford_maxent_path"));
			}
			
		} catch(Exception e) {
			logger.error("POSTagger(): {}", e);
		}
	}
	
	private void init() {
		translateMap.put(",", "comma");
		translateMap.put("#", "hash");
		translateMap.put(":", "colon");
	}
	
	public boolean identifyPartsOfSpeech(Sentence sentence) {
		boolean ret = true;
		
		try {
			ArrayList<WordToken> words = sentence.getWordList();
			
			if(words == null || words.isEmpty()) {
				ret = false;
			} else {
				StanfordNLP stanford = new StanfordNLP(tagger); // TODO will making this global improve performance?
				sentence.setWordList(processPOSOverrides(stanford.identifyPartsOfSpeech(words)));				
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPartsOfSpeech(): {}\n{}", e, sentence.getFullSentence());
		}
		return ret;
	}
	
 	private ArrayList<WordToken> processPOSOverrides(ArrayList<WordToken> words) {
		try {
			for(WordToken word : words) {
				WordToken prevWord = Constants.getToken(words, word.getPosition()-2);
				
				if(verbHelper.shouldOverride(word.getPosition()-1, words)) {
					word.setPOS(Constants.verbOverrides.get(word.getToken().toLowerCase()));
				} else if(PUNC.matcher(word.getToken()).matches()) { // Stanford tags these as NN
					word.setPOS(word.getToken()); // reset POS to match punctuation char
				} else if(word.getToken().equalsIgnoreCase("scan")) {
					// override the POS of scan (Stanford = VB) to NN if preceded by ST diap or token "bone"
					if(prevWord.getToken().equalsIgnoreCase("bone")) {   // equalsIgnoreCase("bone") is faster than match on "(?i)bone"
						word.setPOS("NN");
					}
				} else if(SHOWS.matcher(word.getToken()).matches()) {
					word.setPOS("VB"); // ensure that show and shows are tagged as a verb
				} else if(NOUN_OVERRIDES.matcher(word.getToken()).matches()) {
					word.setPOS("NN"); // certain tokens should always be nouns
				} else if(word.isVerb()) {
					if(!prevWord.isToPOS()) {
						// override tokens that Stanford possibly erroneously tags as verbs
						// must be a verb that ends in -ed or -ing and preceded by a preposition
						if(prevWord.isPreposition()) {
							if(word.getToken().endsWith("ed") || word.getToken().endsWith("ing"))
								word.setPOS("JJ");
							else
								word.setPOS("NN");
						} else if(prevWord.isDeterminerPOS() && word.getPOS().matches("VBN|VBG")) {
							word.setPOS("JJ");
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("processPOSOverrides(): {}", e);
		}
		
		return words;
	}
}