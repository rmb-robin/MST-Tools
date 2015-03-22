package com.mst.tools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.util.Constants;

//https://docs.google.com/a/medsearchtech.com/document/d/1fbbUH7wNuxgnLjalCk6LlKTTfy1912FdtuNYw5MQwis/edit

public class DependentPhraseHelper {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean corefAndConj = false;
	private boolean beginningBoundaries = false;
	private boolean unsetBeginningBoundaries = false;
	
	public boolean processBeginningBoundaries(Sentence sentence) {
		boolean ret = true;

		try {
			ArrayList<WordToken> words = sentence.getWordList();
			
			if(identifyCorefAndConjAdverb(sentence)) {
				for(int i=0; i < words.size(); i++) {
					WordToken thisToken = words.get(i); 
					if(!(thisToken.isCorefernece() || thisToken.isConjunctiveAdverb() || thisToken.isAdjective())) {
						if(i == 0) {
							if(thisToken.getToken().matches(Constants.INTERSECTION_PREPOSITIONS_AND_DEPENDENT)) {
								// dependent signal or prep phrase begins the sentence. determined later???
								words.get(i).setDependentPhraseBegin(Constants.DependentPhraseClass.BEGINS_SENTENCE);
								words.get(i).setDependentPhraseMember(true);
								sentence.getMetadata().addSimpleMetadataValue("depSignalBeginsSentencePrep", true);
							} else if(thisToken.getToken().matches(Constants.DEPENDENT_SIGNALS)) {
								// dependent signal begins the sentence
								words.get(i).setDependentPhraseBegin(Constants.DependentPhraseClass.BEGINS_SENTENCE);
								words.get(i).setDependentPhraseMember(true);
								sentence.getMetadata().addSimpleMetadataValue("depSignalBeginsSentence", true);
							}
						} else if(i < words.size()-1) {
							WordToken prevToken = words.get(i-1);
							WordToken nextToken = words.get(i+1);

							if(!nextToken.getToken().matches("\\.")) {
								if(thisToken.getToken().matches(Constants.DEPENDENT_SIGNALS)) {
									if(prevToken.getToken().matches(",")) {
										// dependent signal precedes a comma
										words.get(i).setDependentPhraseBegin(Constants.DependentPhraseClass.PRECEDED_BY_COMMA);
										sentence.getMetadata().addSimpleMetadataValue("depSignalPrecededByComma", true);
									} else if(nextToken.isVerb()) {
										// dependent signal followed by a verb
										words.get(i).setDependentPhraseBegin(Constants.DependentPhraseClass.FOLLOWED_BY_VERB);
										sentence.getMetadata().addSimpleMetadataValue("depSignalFollwedByVerb", true);
									} else {
										// dependent signal is elsewhere in the sentence
										words.get(i).setDependentPhraseBegin(Constants.DependentPhraseClass.OTHER);
										sentence.getMetadata().addSimpleMetadataValue("depSignalOther", true);

										//This lesion was present on the CT scan of 10/24/2009 and at that time measured about 1.5 x 1.6 cm.
									}
									words.get(i).setDependentPhraseMember(true);
								}
							}
						}
					}
				}
				
				sentence.setWordList(words);
			}
			
		} catch(Exception e) {
			ret = false;
			logger.error("processBeginningBoundaries(): {}", e);
		}
	
		beginningBoundaries = ret;
		return ret;
	}
	
	public boolean unsetBeginningBoundaries(Sentence sentence) throws Exception {
		boolean ret = true;

		if(!(corefAndConj && beginningBoundaries))
			throw new Exception("Execute identifyCorefAndConjAdverb() and processBeginningBoundaries() before running unsetBeginningBoundaries().");
		
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				WordToken thisToken = sentence.getWordList().get(i);

				if(thisToken.isDependentPhraseBegin()) {
					WordToken nextToken = new WordToken();
					WordToken prevToken = new WordToken();
					try {
						nextToken = sentence.getWordList().get(i+1);
					} catch(IndexOutOfBoundsException e) { }
					
					try {
						prevToken = sentence.getWordList().get(i-1);
					} catch(IndexOutOfBoundsException e) { }
					
					// token following dependent signal starts a prep phrase; unset dependent signal
					if(!thisToken.isPrepPhraseMember() && nextToken.isPrepPhraseMember()) {
						sentence.getWordList().get(i).setDependentPhraseBegin(null);
						sentence.getWordList().get(i).setDependentPhraseMember(false);
						sentence.getMetadata().addSimpleMetadataValue("depSignalModByPP", true);
						
						sentence.getMetadata().removeSimpleMetadataValue("depSignalOther");
						
					} else if(thisToken.getToken().matches("(?i)that") && prevToken.isPreposition() && prevToken.isPrepPhraseMember()) {						
						// token matches "that" and follows a preposition
						sentence.getWordList().get(i).setDependentPhraseBegin(null);
						sentence.getWordList().get(i).setDependentPhraseMember(false);
						sentence.getMetadata().addSimpleMetadataValue("prepPhraseContainsThat", true);
						
						// this is a band-aid put here to correct when the only instance of depSignalOther was 
						// the term "that" that was subsequently removed because it was part of a PP
						// this should probably be addressed by dep phrase metadata
						sentence.getMetadata().removeSimpleMetadataValue("depSignalOther");
						
						// "Although imaging on the October exam was limited by the lack contrast, this subjectively appears to have enlarged since that time."
						// the above causes trouble for this because "since" is a prep. End up with metadata prepPhraseContainsThat. fixed
					}
				}
			}
			
		} catch(Exception e) {
			ret = false;
			logger.error("unsetBeginningBoundaries(): {}", e);
		}
	
		unsetBeginningBoundaries = ret;
		return ret;
	}
	
	public boolean processEndingBoundaries(Sentence sentence) {
		boolean ret = true;

		try {
			ArrayList<WordToken> words = sentence.getWordList();

			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);
				
				if(thisToken.isDependentPhraseBegin()) {
					int j=i+1;
					boolean containsVerb = false;
					boolean containsModal = false;
					
					for(; j < words.size(); j++) {
						WordToken nextToken = words.get(j);
						if(nextToken.getToken().matches(",|\\.") || nextToken.isDependentPhraseBegin())
							break;
						else if((nextToken.isPrepPhraseBegin() || nextToken.isInfinitiveHead()) && j-i > 1)  // should this be isPrepPhraseMember() or matchesPrepositionConstant()?
							break;
						// TODO perhaps break also on verbs?
						
						sentence.getWordList().get(j).setDependentPhraseMember(true);
						
						if(nextToken.isVerb()) { containsVerb = true; }
						if(nextToken.isModalAuxPOS()) { containsModal = true; }
					}
					
					if(containsVerb || containsModal) {
						sentence.getWordList().get(j-1).setDependentPhraseEnd(true);
					} else {
						// no verb found; unset dependent phrase
						sentence.getWordList().get(i).setDependentPhraseBegin(null);
						sentence.getWordList().get(i).setDependentPhraseMember(false);
						for(int k=i; k < words.size(); k++) {
							sentence.getWordList().get(j).setDependentPhraseMember(false);	
						}
					}
					
					i = j-1;
				}
				
//				Begin loop through remaining tokens
//			    break on a comma
//			    break on period
//			    break on another DP signal
//			    break on prep term unless immediately following DP signal				
			}
			
		} catch(Exception e) {
			ret = false;
			logger.error("processEndingBoundaries(): {}", e);
		}
	
		beginningBoundaries = ret;
		return ret;
	}
	
	public boolean processEndingBoundaries2(Sentence sentence) {
		boolean ret = true;

		try {
			ArrayList<WordToken> words = sentence.getWordList();
			int thisTokenIdx = 0;
			for(WordToken thisToken : words) {
				
				if(thisToken.isDependentPhraseBegin()) {
					int nextTokenIdx = thisTokenIdx+1;
					boolean containsVerb = false;
					boolean containsModal = false;
					
					for(WordToken nextToken : words.subList(nextTokenIdx, words.size()-1)) {
						if(nextToken.getToken().matches(",|\\.") || nextToken.isDependentPhraseBegin())
							break;
						else if((nextToken.isPrepPhraseBegin() || nextToken.isInfinitiveHead()) && nextTokenIdx-thisTokenIdx > 1)  // should this be isPrepPhraseMember() or matchesPrepositionConstant()?
							break;
						// TODO perhaps break also on verbs?
						
						sentence.getWordList().get(nextTokenIdx).setDependentPhraseMember(true);
						
						if(nextToken.isVerb())
							containsVerb = true;
						if(nextToken.isModalAuxPOS())
							containsModal = true;
						nextTokenIdx++;
					}
					
					if(containsVerb || containsModal) {
						sentence.getWordList().get(nextTokenIdx-1).setDependentPhraseEnd(true);
					} else {
						// no verb found; unset dependent phrase and all members
						sentence.getWordList().get(thisTokenIdx).setDependentPhraseBegin(null);
						for(int i=thisTokenIdx; i < words.size(); i++) {
							sentence.getWordList().get(i).setDependentPhraseMember(false);	
						}
					}
				}
				
				thisTokenIdx++;
				
//				Begin loop through remaining tokens
//			    break on a comma
//			    break on period
//			    break on another DP signal
//			    break on prep term unless immediately following DP signal				
			}
			
		} catch(Exception e) {
			ret = false;
			logger.error("processEndingBoundaries(): {}", e);
		}
	
		beginningBoundaries = ret;
		return ret;
	}
	
	private boolean identifyCorefAndConjAdverb(Sentence sentence) {
		boolean ret = true;
		
		try {
			WordToken token = sentence.getWordList().get(0);

			if(token.getToken().matches("(?i)this|that")) {
				// if first token of the sentence is "this" or "that" and is followed by a verb
				try {
					if(sentence.getWordList().get(1).isVerb()) {
						sentence.getWordList().get(0).setCoreference(true);
						sentence.getMetadata().addSimpleMetadataValue("coReference", true);
					}
				} catch(IndexOutOfBoundsException e) { }
				
			} else if(token.matchesConjunctiveAdverbConstant()) {
				// if first token of the sentence is a conjunctive adverb
				sentence.getWordList().get(0).setConjunctiveAdverb(true);
				sentence.getMetadata().addSimpleMetadataValue("beginsWithConjAdverb", true);
			}

		} catch(Exception e) {
			ret = false;
			logger.error("identifyCorefAndConjAdverb(): {}", e);
		}
	
		corefAndConj = ret;
		return ret;
	}
	
}