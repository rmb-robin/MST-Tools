package com.mst.tools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.Sentence;
import com.mst.model.WordToken;

public class NounHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	public boolean identifyNounPhrases(Sentence sentence) {
		// requires POS-tagged list of WordTokens
		boolean ret = true;
		
		int headIndex = 0;
		try {
			//TODO don't mark as head if surrounded by parens?
			for(int i=sentence.getWordList().size()-1; i >= 0; i--) {
				WordToken word = sentence.getWordList().get(i);
				if(word.isNounPOS() && !word.isPunctuation()) { // added isPunc because Stanford was tagging ( and ) as NN. This may catch other Stanford POS tagging of punc chars.
					if(headIndex == 0) {
						headIndex = i;
					} else {
						sentence.getWordList().get(headIndex).setNounPhraseHead(true);
						sentence.getWordList().get(i).setNounPhraseModifier(true);
					}
				//} else if((word.isAdjectivePOS() || word.isAdverbPOS() || word.isPunctuation() || word.isNumericPOS() || word.isPronounPOS()) && !word.getToken().equalsIgnoreCase(",")) {
				} else if((word.isAdjectivePOS() || word.isAdverbPOS() || word.isNumericPOS() || word.isPronounPOS())) { // SRD 7/17/15 removed all punc because parens were being included in two-token noun phrases
					// SRD 7/10/15 - testing a fix for ex. "Followed for metastatic prostate cancer, urethral stricture, and BPH."
					if(headIndex > 0) {
						sentence.getWordList().get(headIndex).setNounPhraseHead(true);
						sentence.getWordList().get(i).setNounPhraseModifier(true);
					}
				} else {
					headIndex = 0;
				}
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyNounPhrases() {}", e);
		}
		return ret;
	}
	
	// somewhat legacy/deprecated
	// used by the old JSP website in an attempt to mimic one of Eric's reports
	public String getNPAnnotatedSentence(String keyword, String extractionTerm, ArrayList<WordToken> words, boolean annotate) {
		StringBuilder sb = new StringBuilder();
		boolean showOpenBracket = true;
		String PUNC_ALLOW_PARENS = "!|\"|#|\\$|%|&|'|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
		
		try {
			// TODO KW comes after HEAD?
			// TODO assign OBJ to NP-annotated
			for(int i=0; i < words.size(); i++) {
				if(annotate) {
					String prefix = "/";
					
					if(words.get(i).isNounPhraseModifier() && showOpenBracket) {
						sb.append("{");
						showOpenBracket = false;
					}
					
					sb.append(words.get(i).getToken());
					
					if(keyword != null && words.get(i).getToken().matches(keyword.concat("(.*)"))) {
						sb.append(prefix).append("KW");
						prefix = "+";
					}
					
					if(extractionTerm != null && words.get(i).getToken().matches(extractionTerm)) {
						sb.append(prefix).append("EXT");
						prefix = "+";
					}
					
					if(words.get(i).isNounPhraseHead()) {
						sb.append(prefix).append("HEAD}");
						showOpenBracket = true;
					}
				} else {
					// pass annotate == false to return the sentence with no markup
					sb.append(words.get(i).getToken());
				}
				
				if(i < words.size()-1 
						&& !words.get(i+1).getToken().matches(PUNC_ALLOW_PARENS)
						&& !words.get(i).getToken().matches("\\("))
					sb.append(" ");
			}
		} catch(Exception e) {
			logger.error("getNPAnnotatedSentence(): {}", e);
			sb.append("*** Unable to build NP-annotated sentence. See error log for details. ***");
		}
		
		return sb.toString();
	}
}
