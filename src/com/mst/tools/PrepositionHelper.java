package com.mst.tools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class PrepositionHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public boolean identifyPrepPhrases(Sentence sentence) {
		boolean ret = true;

		try {
			List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
			
			for(int i=0; i < sentence.getModifiedWordList().size(); i++) {
				WordToken thisWord = sentence.getModifiedWordList().get(i);
				
				boolean containsCC = false; // used in determining if multiple objects should be allowed
				
				// if token in prep list and NOT part of an infinitive phrase
				if(thisWord.matchesPrepositionConstant() && !thisWord.isInfinitiveHead()) {
					// loop through remaining words in the sentence
					for(int j=i+1; j < sentence.getModifiedWordList().size(); j++) {
						WordToken nextWord = sentence.getModifiedWordList().get(j);
						
						// end PP on preposition, verb, or DP head
						if(nextWord.isPreposition() || nextWord.isVerb())
							break;
						if(nextWord.isDependentPhraseBegin() && j-i > 1)
							// next word is a dep phrase signal and at least one token separates the preposition from the dp signal
							break;
						else if(nextWord.getToken().matches(";|\\.|\\)")) // a semicolon or period always stops the phrase
							break;
						else if(nextWord.isConjunctionPOS()) { // a a conjuction not followed by a noun/number
							try {
								if(!sentence.getModifiedWordList().get(j+1).getPos().matches("^(CD|JJ|NN(S|P|PS)?)$")) {
									// deal with Oxford comma followed by a CC (remove comma hanging off the end)
									if(sentence.getModifiedWordList().get(comprisingTokenIndex.get(comprisingTokenIndex.size()-1)).getPos().matches(",")) {
										comprisingTokenIndex.remove(comprisingTokenIndex.get(comprisingTokenIndex.size()-1));
									}
									break;
								}
							} catch(IndexOutOfBoundsException e) { }
						} else if(nextWord.getToken().matches(",")) { // a comma stops the phrase
							try {
								// if it is NOT followed by one of these POS...
								if(!sentence.getModifiedWordList().get(j+1).getPos().matches("^(CD|CC|JJ|NN(S|P|PS)?)$"))
									break;
							} catch(IndexOutOfBoundsException e) { }
						}
						// no breaks; add index of current word to list of comprising tokens
						// ## logic above this line EXCLUDES the token that caused the break ##
						comprisingTokenIndex.add(j);
						if(nextWord.isConjunctionPOS())
							containsCC = true;
						
						// ## logic below this line INCLUDES the token that caused the break ##
						
						// stop on a noun/number...
						if(nextWord.isNounPOS() || nextWord.isNumericPOS()) {
							try {
								// if it is NOT followed by one of these POS...
								if(!sentence.getModifiedWordList().get(j+1).getPos().matches("^(CD|CC|,|NN(S|P|PS)?)$"))
									break;
							} catch(IndexOutOfBoundsException e) { }
						} 
					}
	
					if(comprisingTokenIndex.size() > 0) {
						sentence.getModifiedWordList().get(i).setPrepPhraseBegin(true);
						sentence.getModifiedWordList().get(i).setPrepPhraseMember(true);
						
						// this is a little convoluted.
						// three booleans make up a PP token: ppBegin, ppMember, ppObj
						// the final token will only have ppObj set = true (ppMember will = false). This combo used in other routines to determine the end of the PP.
						// the first token (the preposition) will have ppBegin = true and ppMember = true.
						// all non-final members of the PP will have ppMember = true.
						// all PP objects will have ppMember = true and ppObj = true.
						
						// loop through list of indexes that make up the prep phrase
						for(int j=0; j < comprisingTokenIndex.size(); j++) {
							WordToken ppWord = sentence.getModifiedWordList().get(comprisingTokenIndex.get(j));
							
							if(j == comprisingTokenIndex.size()-1) { // last item in list
								ppWord.setPrepPhraseObject(true);
							} else {
								ppWord.setPrepPhraseMember(true);
								if(containsCC && (ppWord.isNounPOS() || ppWord.isNumericPOS())) {
									// set as prep phrase object (nouns, cardinal numbers only)
									ppWord.setPrepPhraseObject(true);
								}
							}
						}
						
						comprisingTokenIndex.clear();
					}
				}
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyPrepPhrases(): {}", e);
		}
		
		return ret;
	}
	
	// this is legacy and not currently used
	public String[] getPPAnnotatedSentence(String keyword, String extractionTerm, ArrayList<WordToken> words) {
		StringBuilder markup = new StringBuilder();
		StringBuilder orig = new StringBuilder();
		boolean showOpenBracket = true;
		
		try {
			for(int i=0; i < words.size(); i++) {
				String prefix = "/";
				
				orig.append(words.get(i).getToken());
				
				if(words.get(i).isPrepPhraseMember() && showOpenBracket){
					markup.append("[");
					showOpenBracket = false;
				}
				markup.append(words.get(i).getToken());
				
				if(keyword != null && words.get(i).getToken().matches(keyword.concat("(.*)"))) {
					markup.append(prefix).append("KW");
					prefix = "+";
				}
				
				if(extractionTerm != null && words.get(i).getToken().matches(extractionTerm)) {
					markup.append(prefix).append("EXT");
					prefix = "+";
				}
				
				if(words.get(i).isPrepPhraseObject()) {
					markup.append(prefix).append("OBJ");
					if(!words.get(i).isPrepPhraseMember()) {
						markup.append("]");
						showOpenBracket = true;
					}
				}
				
				if(i < words.size()-1 && !Constants.PUNC.matcher(words.get(i+1).getToken()).matches()) {
					markup.append(" ");
					orig.append(" ");
				}
			}
		} catch(Exception e) {
			logger.error("getPPAnnotatedSentence(): {}", e);
			markup.append("*** Unable to build PP-annotated sentence. See error log for details. ***");
		}

		String[] ret = { markup.toString(), orig.toString() }; 
		
		return ret;
	}
}
