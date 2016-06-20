package com.mst.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
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
				if(verbHelper.shouldOverride(word.getPosition()-1, words)) {
					word.setPOS(Constants.verbOverrides.get(word.getToken().toLowerCase()));
				}
			}
		} catch(Exception e) {
			logger.error("processPOSOverrides(): {}", e);
		}
		
		return words;
	}
}