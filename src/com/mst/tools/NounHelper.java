package com.mst.tools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class NounHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private int nounPhraseCount = 0;
		
	public boolean identifyNounPhrases(ArrayList<WordToken> words) {
		// requires POS-tagged list of WordTokens
		boolean ret = true;
		if(words == null) {
			//identifyPartsOfSpeech(wordList);
		}
		
		int oldHeadIndex = -1;
		int headIndex = 0;
		try {
			//TODO don't mark as head if surrounded by parens?
			for(int i=words.size()-1; i >= 0; i--) {
				if(words.get(i).isNoun()) {
					if(headIndex == 0) {
						headIndex = i;
					} else {
						words.get(headIndex).setNounPhraseHead(true);
						words.get(i).setNounPhraseModifier(true);
						if(oldHeadIndex != headIndex) {
							nounPhraseCount++;
							oldHeadIndex = headIndex;
						}
					}
				} else if(words.get(i).isAdjective() || words.get(i).isAdverb() || words.get(i).isPunctuation()) {
					if(headIndex > 0) {
						words.get(headIndex).setNounPhraseHead(true);
						words.get(i).setNounPhraseModifier(true);
						if(oldHeadIndex != headIndex) {
							nounPhraseCount++;
							oldHeadIndex = headIndex;
						}
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
	
	public int getNounPhraseCount() {
		return nounPhraseCount;
	}
	
	public ArrayList<WordToken> identifyNounPhrasesLegacy(ArrayList<WordToken> words) {
	
		if(words == null) {
			//identifyPartsOfSpeech(wordList);
		}
		
		int headIndex = 0;
		try {
			//TODO don't mark as head if surrounded by parens?
			for(int i=words.size()-1; i >= 0; i--) {
				if(words.get(i).getPOS().matches("NN|NNS")) {
					if(headIndex == 0) {
						headIndex = i;
					} else {
						words.get(headIndex).setNounPhraseHead(true);
						words.get(i).setNounPhraseModifier(true);
					}
				} else if(words.get(i).getPOS().matches("JJ|RB|" + Constants.PUNC)) {
					if(headIndex > 0) {
						words.get(headIndex).setNounPhraseHead(true);
						words.get(i).setNounPhraseModifier(true);
					}
				} else {
					headIndex = 0;
				}
			}
		} catch(Exception e) {
			System.out.println("Error in identifyNounPhrases(): " + e.toString());
			Gson gson = new Gson();
			System.out.println(gson.toJson(words));
		}
		return words;
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
	
//	public String getNPAnnotatedSentence(String keyword, String extractionTerm, boolean annotate) throws Exception {
//		if(taggedWordList == null) {
//			throw new Exception("Please execute tagSentence() before attempting to getNPAnnotatedSentence().");
//		}
//		
//		return getNPAnnotatedSentence(keyword, extractionTerm, taggedWordList, annotate);
//	}
}
