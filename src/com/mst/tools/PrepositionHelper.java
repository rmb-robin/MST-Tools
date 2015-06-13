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

	public boolean identifyPrepPhrases(ArrayList<WordToken> words) {
		boolean ret = true;
		
		// TODO I suspect that there are some logic problems with this code (identifying OBJ, etc)
		//List<String> defaultPrepositionList = Arrays.asList("after","although","among","as","at","before","between","by","during",
		//													"for","from","in","of","on","over","per","than","that","to","while",
		//													"with","within","without");
		if(words == null) {
			//identifyPartsOfSpeech(wordList);
		}
		
		try {
			List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
			
			for(int i=0; i < words.size(); i++) {
				WordToken currentWord = words.get(i);
				
				// if token in prep list and NOT part of an infinitive phrase
				//if(defaultPrepositionList.contains(currentWord.getToken().toLowerCase()) && !currentWord.isInfinitiveHead()) {
				if(currentWord.matchesPrepositionConstant() && !currentWord.isInfinitiveHead()) {
					// loop through remaining words in the sentence
					for(int j=i+1; j < words.size(); j++) {
						WordToken nextWord = words.get(j);
						
						// end PP on preposition, verb, or DP head
						if(nextWord.isPreposition() || nextWord.isVerb())
							break;
						else if(nextWord.getToken().matches(";|\\.")) // a semicolon or period always stops the phrase
							break;
						else if(nextWord.getToken().equals(",")) // a comma stops the phrase
							// unless it is preceded and followed by an adjective
							if(!(words.get(j-1).isAdjectivePOS()) && words.get(j+1).isAdjectivePOS())
								break;
						
						// no breaks; add index of current word to list of comprising tokens
						// ## logic above this line EXCLUDES the token that caused the break ##
						comprisingTokenIndex.add(j);
						// ## logic below this line INCLUDES the token that caused the break ##
						
						// stop on a noun unless the following token is a coordinating conjunction or noun
						// size() check to avoid OutOfBounds exception
						//if(nextWord.isNoun() && j < words.size()-1 &&
						//   !words.get(j+1).getPOS().matches("CC|NN|NNS"))
						//	break;	
						
						try {
							if(nextWord.isNounPOS() && !words.get(j+1).getPOS().matches("^(CC|NN(S|P|PS)?)$"))
								break;
						} catch(IndexOutOfBoundsException e) { }
					}
	
					if(comprisingTokenIndex.size() > 0) {
						// prepend initial matched preposition
						comprisingTokenIndex.add(0, i);
						//boolean runOnce = true;
						
						// loop through list of indexes that make up the prep phrase
						for(int j=0; j < comprisingTokenIndex.size(); j++) {
							int index = comprisingTokenIndex.get(j);
													
							if(j == comprisingTokenIndex.size()-1) { // last item in list
								do {
									words.get(index).setPrepPhraseObject(true);
									index--;
									//if(runOnce) {
									//	prepPhraseCount++;
									//	runOnce = false;
									//}
								} while(words.get(index).getPOS().matches("^(CC|,|NN(S|P|PS)?)$"));
								
							} else {
								// set as prep phrase member
								words.get(index).setPrepPhraseMember(true);
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
	
	// TODO: can this be simplified?
	public boolean identifyPrepPhrases(Sentence sentence) {
		boolean ret = true;

		try {
			List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
			
			for(int i=0; i < sentence.getWordList().size(); i++) {
				WordToken thisWord = sentence.getWordList().get(i);
				
				// if token in prep list and NOT part of an infinitive phrase
				if(thisWord.matchesPrepositionConstant() && !thisWord.isInfinitiveHead()) {
					// loop through remaining words in the sentence
					for(int j=i+1; j < sentence.getWordList().size(); j++) {
						WordToken nextWord = sentence.getWordList().get(j);
						
						// end PP on preposition, verb, or DP head
						if(nextWord.isPreposition() || nextWord.isVerb())
							break;
						if(nextWord.isDependentPhraseBegin() && j-i > 1)
							// next word is a dep phrase signal and at least one token separates the preposition from the dp signal
							break;
						else if(nextWord.getToken().matches(";|\\.|\\)")) // a semicolon or period always stops the phrase
							break;
						else if(nextWord.isConjunctionPOS()) { // a a conjuction not followed by a noun/number
							if(!sentence.getWordList().get(j+1).getPOS().matches("^(CD|JJ|NN(S|P|PS)?)$")) {
								// deal with Oxford comma followed by a CC (remove comma hanging off the end)
								if(sentence.getWordList().get(comprisingTokenIndex.get(comprisingTokenIndex.size()-1)).getPOS().matches(",")) {
									comprisingTokenIndex.remove(comprisingTokenIndex.get(comprisingTokenIndex.size()-1));
								}
								break;
							}
						} else if(nextWord.getToken().matches(",")) { // a comma stops the phrase
							// unless it is preceded and followed by an adjective
							//if(!(sentence.getWordList().get(j-1).isAdjectivePOS()) && sentence.getWordList().get(j+1).isAdjectivePOS())
							// prep prhase is allowed to continue if comma is follwed by a noun, number, or adjective (in the latter case only if an adjective also preceeds the comma)
							// TODO what about CC?
							//if(!(sentence.getWordList().get(j-1).isAdjectivePOS() && sentence.getWordList().get(j+1).isAdjectivePOS()) || 
							//	!sentence.getWordList().get(j+1).getPOS().matches("^(CD|NN(S|P|PS)?)$"))
							//	break;
							if(!sentence.getWordList().get(j+1).getPOS().matches("^(CD|CC|JJ|NN(S|P|PS)?)$"))
								break;
						}
						// no breaks; add index of current word to list of comprising tokens
						// ## logic above this line EXCLUDES the token that caused the break ##
						comprisingTokenIndex.add(j);
						// ## logic below this line INCLUDES the token that caused the break ##
						
						// stop on a noun/number...	
						try {
							if(nextWord.isNounPOS() || nextWord.isNumericPOS()) {
								// unless the following token is a coordinating conjunction, noun, number, or comma
								if(!sentence.getWordList().get(j+1).getPOS().matches("^(CD|CC|,|NN(S|P|PS)?)$"))
									break;
							}
						} catch(IndexOutOfBoundsException e) { }
					}
	
					if(comprisingTokenIndex.size() > 0) {
						// prepend initial matched preposition
						//comprisingTokenIndex.add(0, i);
						sentence.getWordList().get(i).setPrepPhraseBegin(true);
						sentence.getWordList().get(i).setPrepPhraseMember(true);
						
						// loop through list of indexes that make up the prep phrase
//						for(int j=0; j < comprisingTokenIndex.size(); j++) {
//							int index = comprisingTokenIndex.get(j);
//													
//							if(j == comprisingTokenIndex.size()-1) { // last item in list
//								do {
//									sentence.getWordList().get(index).setPrepPhraseObject(true);
//									index--;
//									// TODO this loop may no longer be important as long as the final token is marked as the Object
//								} while(sentence.getWordList().get(index).getPOS().matches("^(CC|,|NN(S|P|PS)?)$") && index >= comprisingTokenIndex.get(0));
//								
//							} else {
//								// set as prep phrase member
//								sentence.getWordList().get(index).setPrepPhraseMember(true);
//								if(j == 0) {
//									sentence.getWordList().get(index).setPrepPhraseBegin(true);
//								}
//							}
//						}
						
						// this is a little convoluted.
						// three booleans make up a PP token: ppBegin, ppMember, ppObj
						// the final token will only have ppObj set = true (ppMember will = false). This is used in other routines to determine the end of the PP.
						// the first token (the preposition) will have ppBegin = true and ppMember = true.
						// all non-final members of the PP will have ppMember = true.
						// all PP objects will have ppMember = true and ppObj = true.
						
						for(int j=0; j < comprisingTokenIndex.size(); j++) {
							WordToken ppWord = sentence.getWordList().get(comprisingTokenIndex.get(j));
							
							if(j == comprisingTokenIndex.size()-1) { // last item in list
								ppWord.setPrepPhraseObject(true);
							} else {
								ppWord.setPrepPhraseMember(true);
								if(ppWord.isNounPOS() || ppWord.isNumericPOS()) {
									// set as prep phrase member (nouns, cardinal numbers only)
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
				if(i < words.size()-1 && !words.get(i+1).getToken().matches(Constants.PUNC)) {
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
	

	
	
//	public List<PrepPhraseToken> identifyPrepPhrasesLegacy(ArrayList<WordToken> words) {
//		// Attempt to mimic Eric's output for prep phrase identification. Not used by the camel processes.
//		/*
//		Homebrew prepositional phrase identifier.  Should be superseded by an effective chunker or constituency parser.
//
//		Algorithm:
//		1) Identify prepositions in a sentence.  This can be done either from a list of prepositions passed to the __init__ method,
//		    or by identifying every word token tagged as IN or TO (if preposition_list == None).
//		2) Start a phrase which currently contains only the preposition.
//		3) For each token succeeding the preposition:
//		   a) If the token is a verb, stop and return the phrase, excluding the verb.
//		   b) If the token is a preposition, stop and return the phrase, excluding the preposition.
//		   c) If the token is a comma or semicolon, stop and return the phrase, excluding the token, UNLESS it is a comma and both
//		       the token immediately before and after are adjectives.
//		   d) If the token is a noun, stop and return the phrase INCLUDING the noun,
//		   e) UNLESS the token after the noun is another noun or a coordinating conjunction, in which case include the noun or conjunction
//		       and continue the phrase.
//		4) Identify the token preceding the prepositional phrase and attach it to the returned annotation - some consumers are only
//		    interested in phrases which follow e.g. a noun or adverb.
//		*/
//		// TODO I suspect that there are some logic problems with this code (identifying OBJ, etc)
//		// Camel doesn't at this point care about the PrePhraseToken list, only the boolean tags added to taggedWordList
//		List<PrepPhraseToken> prepPhraseTokens = new ArrayList<PrepPhraseToken>();
//		
//		List<String> defaultPrepositionList = Arrays.asList("after","although","among","as","at","before","between","by","during",
//															"for","from","in","of","on","over","per","than","that","to","while",
//															"with","within","without");
//		if(words == null) {
//			//identifyPartsOfSpeech(wordList);
//		}
//		
//		List<Integer> comprisingTokenIndex = new ArrayList<Integer>();
//		
//		for(int i=0; i < words.size(); i++) {
//			WordToken currentWord = words.get(i);
//			
//			if(defaultPrepositionList.contains(currentWord.getToken().toLowerCase())) {
//				// loop through remaining words in the sentence
//				for(int j=i+1; j < words.size(); j++) {
//					String nextWord = words.get(j).getToken();
//					String nextPOS = words.get(j).getPOS(); 
//					
//					// break on preposition or verb
//					if(nextPOS.matches("IN|TO|VB.*"))
//						break;
//					// a semicolon always stops the phrase
//					else if(nextWord.equals(";"))
//						break;
//					// a comma stops the phrase
//					else if(nextWord.equals(","))
//						// unless it is preceded and followed by an adjective
//						if(!(words.get(j-1).getPOS().equals("JJ") && words.get(j+1).getPOS().equals("JJ")))
//							break;
//					
//					// no breaks; add index of current word to list of comprising tokens
//					comprisingTokenIndex.add(j);
//					
//					// stop on a noun unless the following token is NOT coordinating conjunction or other noun
//					// size() check to avoid OutOfBounds exception
//					if(nextPOS.matches("NN|NNS") && j < words.size()-1 &&
//					    !words.get(j+1).getPOS().matches("CC|NN|NNS"))
//						break;		
//				}
//
//				if(comprisingTokenIndex.size() > 0) {
//					List<String> comprisingTokens = new ArrayList<String>();
//					StringBuilder value = new StringBuilder();
//					
//					// prepend initial matched preposition
//					comprisingTokenIndex.add(0, i);
//					
//					// loop through list of indexes that make up the prep phrase
//					for(int j=0; j < comprisingTokenIndex.size(); j++) {
//						int index = comprisingTokenIndex.get(j);
//						
//						value.append(words.get(index).getToken()).append(" "); // ex. "from their local environment"
//						comprisingTokens.add(words.get(index).getToken() + "/" + words.get(index).getPOS()); // ex. ["from/IN", "their/PRP$", "local/JJ", "environment/NN"]
//						
//						if(j == comprisingTokenIndex.size()-1) { // last item in list
//							// tag OBJs of prep phrase if conjunction or comma
//							do {
//								words.get(index).setPrepPhraseObject(true);
//								index--;
//							} while(words.get(index).getPOS().matches("CC|,"));
//							
//							// if final word is a noun, tag as OBJ
//							// TODO potential issue if more than one OBJ exists after the CC
//							if(words.get(index).getPOS().matches("NN|NNS"))
//								words.get(index).setPrepPhraseObject(true);
//							
//						} else {
//							// set as prep phrase member (for grouping with brackets)
//							words.get(index).setPrepPhraseMember(true);
//						}
//					}
//					
//					//int begin = currentWord.getBegin();
//					//int end = begin + value.length()-1; // account for trailing space
//					// set preceding token if not at beginning of sentence
//					//String precedingToken = i>0 ? taggedWordList.get(i-1).getToken() + "/" + taggedWordList.get(i-1).getPOS() : "";
//					
//					//prepPhraseTokens.add(new PrepPhraseToken(begin, end, currentWord.getToken(), "TODO", value.toString().trim(), precedingToken, comprisingTokens));
//					comprisingTokenIndex.clear();
//				}
//			}
//		}
//		
////		for(PrepPhraseToken ppt : prepPhraseTokens) {
////			System.out.println("preposition_token: " + ppt.getToken());
////			System.out.println("begin: " + ppt.getBegin());
////			System.out.println("end: " + ppt.getEnd());
////			System.out.println("preceding_token: " + ppt.getPrecedingToken());
////			System.out.println("value: " + ppt.getValue());
////			String cts = "";
////			for(String ct : ppt.getComprisingTokens()) {
////				cts += " " + ct;
////			}
////			System.out.println("comprising tokens: " + cts.trim() + "\n");
////		}
//		
//		return prepPhraseTokens;
//	}
}
