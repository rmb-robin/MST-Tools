package com.mst.tools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mst.model.GenericToken;
import com.mst.model.Sentence;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class VerbHelper {
	private final String INFINITIVE_HEAD_TERM = "(?i)to";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public boolean identifyInfinitivePhrases(Sentence sentence) {
		// must be preceded by POSTagger.identifyPartsOfSpeech()
		// must execute before POSTagger.identifyPrepPhrases()
		boolean ret = true;
		
		// TODO what checks need to occur to ensure that wordList is valid?
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				if(sentence.getWordList().get(i).getToken().matches(INFINITIVE_HEAD_TERM)) {
					// avoid an OOB exception on the +1
					if(i < sentence.getWordList().size() - 1) {
						// TODO check if next word is punctuation, e.g. period or other end of sentence?
						// if next word is a verb
						if(sentence.getWordList().get(i+1).isVerb()) {
							sentence.getWordList().get(i).setInfinitiveHead(true);
							sentence.getWordList().get(i+1).setInfinitiveVerb(true);
							//i++;
						}
					}
				}
			}
		} catch(Exception e) {
			ret = false;
			logger.error("identifyInfinitivePhrases() {}", e);
			e.printStackTrace();
		}

		return ret;
	}
	
	private String arrayListToString(ArrayList<WordToken> list) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(WordToken token : list) {
			sb.append("\"").append(token.getToken()).append("\",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	//  must be preceded by POSTagger.identifyPartsOfSpeech(), POSTagger.identifyPrepPhrases(), identifyVerbsOfBeing()
	public ArrayList<WordToken> identifyLinkingVerbs(Sentence sentence) {
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);
				
				if(thisToken.getToken().matches(Constants.LINKING_VERBS) && // verb in the list
					!(thisToken.isVerbOfBeing()) && // verb not marked as a verb of being
					!(thisToken.isPrepPhraseMember() || thisToken.isPrepPhraseObject())) { // verb not part of a prep phrase
					
					// avoid indexOOB
					if(i < words.size()-1 && words.get(i+1).getPOS().matches("DT|JJ|NN|NNS|CD")) { // verb's successor token is adjective or noun 
						words.get(i).setLinkingVerb(true);
						
						int subjIdx = identifyVerbSubject(words, i);
						int subjcIdx = identifySubjectComplement(words, i);
						
						if(subjIdx != -1) words.get(subjIdx).setLinkingVerbSubject(subjIdx != -1);
						if(subjcIdx != -1) words.get(subjcIdx).setLinkingVerbSubjectComplement(subjcIdx != -1);

						sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.LINKING_VERB, i, words, subjIdx, subjcIdx));
						i++; // increment past the verb's successor
					}
				}
			}
			
			sentence.setWordList(words);
			
		} catch(Exception e) {
			logger.error("identifyLinkingVerbs() {}", e);
		}

		return words;
	}
	
	// requires POSTagger.identifyPartsOfSpeech()
	public ArrayList<WordToken> identifyModalAuxiliaryVerbs(Sentence sentence) {
		
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				WordToken thisToken = sentence.getWordList().get(i);
				try {
					if(thisToken.matchesModalAuxVerb() && sentence.getWordList().get(i+1).isVerb()) {
						sentence.getWordList().get(i).setModalAuxTerm(true);
						sentence.getWordList().get(i+1).setModalAuxVerb(true);
					}
				} catch(IndexOutOfBoundsException e) { }
			}
		} catch(Exception e) {
			logger.error("identifyModalAuxiliaryVerbs() {}", e);
		}

		return sentence.getWordList();
	}
	
	// requires POSTagger.identifyPartsOfSpeech(), identifyVerbsOfBeing(), identifyLinkningVerbs(), identifyInfinitivePhrases(), identifyModalAuxVerbs()
	public ArrayList<WordToken> identifyActionVerbs(Sentence sentence) {
		
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				WordToken thisToken = sentence.getWordList().get(i);

				if(thisToken.isVerb() && !(thisToken.isLinkingVerb() ||
										   thisToken.isVerbOfBeing() || 
										   thisToken.isInfinitiveVerb() || 
										   thisToken.isPrepositionalVerb() )) {
					
					sentence.getWordList().get(i).setActionVerb(true);

					int subjIdx = identifyActionVerbSubject(sentence.getWordList(), i);
					int objIdx = identifyActionVerbDirectObject(sentence.getWordList(), i);
					
					sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.ACTION, i, sentence.getWordList(), subjIdx, objIdx));
				}
			}
		} catch(Exception e) {
			logger.error("identifyActionVerbs() {}", e);
		}

		return sentence.getWordList();
	}
	
	private VerbPhraseMetadata setVerbPhraseMetadata(Constants.VerbClass _class, int verbIdx, ArrayList<WordToken> words, int subjIdx, int objIdx) {
		VerbPhraseMetadata vpm = new VerbPhraseMetadata(_class);
		
		vpm.setVerb(new VerbPhraseToken(words.get(verbIdx).getToken(), verbIdx));
		
		if(subjIdx != -1) {
			vpm.setSubj(new VerbPhraseToken(words.get(subjIdx).getToken(), subjIdx));
		}
		
		if(objIdx != -1) {
			vpm.setSubjC(new VerbPhraseToken(words.get(objIdx).getToken(), objIdx));
		}
		
		return vpm;
	}
	
	// Jan's notes mention requirements of all other verb phrase types and noun phrases but doesn't mention how they factor in
	// requires prep phrases parts of speech
	public ArrayList<WordToken> identifyPrepositionalVerbs(Sentence sentence) {
		
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				WordToken thisToken = sentence.getWordList().get(i);
				
				// token is a preposition and has been marked as being the head of a prep phrase (may be overkill)
				if(thisToken.isPreposition() && thisToken.isPrepPhraseMember()) {
					try {
						// loop backwards looking for a verb, optionally separated by an adverb or token ending in 'ly'
						for(int j=i-1; j >= 0; j--) {
							WordToken prevToken = sentence.getWordList().get(j);
						
							if(prevToken.isAdverb() || prevToken.getToken().endsWith("(?i)ly")) {
								continue;
							} else if(prevToken.isVerb()) {
								sentence.getWordList().get(j).setPrepositionalVerb(true);
								break;
							} else {
								break;
							}
						}
					} catch(IndexOutOfBoundsException e) { }
				}
			}
		} catch(Exception e) {
			logger.error("identifyPrepositionalVerbs() {}", e);
		}

		return sentence.getWordList();
	}
	
	// must be preceded by POSTagger.identifyPartsOfSpeech(), POSTagger.identifyPrepPhrases()
	public ArrayList<WordToken> identifyVerbsOfBeing(Sentence sentence) {
		
		// TODO this needs to be overhauled since removing VOB member designations
		
		try {
			for(int i=0; i < sentence.getWordList().size(); i++) {
				
				if(sentence.getWordList().get(i).matchesVerbOfBeingConstant()) {
					// avoid indexOutOfBounds exception on the +1
					if(i < sentence.getWordList().size() - 1) {
						sentence.getWordList().get(i).setVerbOfBeing(true);
						
						int vobIndex = i;
						WordToken nextToken = sentence.getWordList().get(i+1);
						
						// Step 1. Process token following the verb of being
						// if next token is a verb or ends in 'ly' or 'ed', mark as vob member
						if(nextToken.isVerb() || nextToken.getToken().matches(".*(?i)ly|ed")) {
							//wordList.get(i+1).setVerbOfBeingMember(true);
							i++;
						}
						// TODO why do i do this check again? should this be nextToken+1?
						// additional check: if next token ends in "ly", also denote its successor as a vob member
						if(nextToken.getToken().matches(".*(?i)ly")) {
							if(i < sentence.getWordList().size()-1) { // avoid IndexOutOfBounds
								//wordList.get(i+1).setVerbOfBeingMember(true);
								i++;
							}
						}
						
						// Step 2. process up to three tokens following the verb of being 
						int tokensRemaining = Math.min(3, sentence.getWordList().size()-1 - vobIndex);
						boolean verbFound = false;
						
						for(int j=vobIndex + tokensRemaining; j > vobIndex; j--) {
							if(verbFound) {
								//wordList.get(j).setVerbOfBeingMember(true);
							} else {
								if(sentence.getWordList().get(j).isPreposition()) {
									// if beginning a prep phrase then mark the preceding token
									verbFound = true;
								} else if(sentence.getWordList().get(j).isVerb()) {
									// if a verb then mark verb itself
									//wordList.get(j).setVerbOfBeingMember(true);
									verbFound = true;
								}
							}
						}
					
						if(i == vobIndex && !verbFound) {
							// if no vob members found, unset vob head
							sentence.getWordList().get(vobIndex).setVerbOfBeing(false);
							// TODO what if verb follows verb of being (first scenario)? Do we still process following 3 tokens and mark isLinkingVerb?
							//wordList.get(vobIndex).isLinkingVerb = true;
						} else {
							// verb of being found
							// now identify subject and subject complement and set metadata
							int subjIdx = identifyVerbSubject(sentence.getWordList(), vobIndex);
							int subjcIdx = identifySubjectComplement(sentence.getWordList(), vobIndex);
							
							if(subjIdx != -1) sentence.getWordList().get(subjIdx).setVerbOfBeingSubject(true);
							if(subjcIdx != -1) sentence.getWordList().get(subjcIdx).setVerbOfBeingSubjectComplement(true);
								
							sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.VERB_OF_BEING, vobIndex, sentence.getWordList(), subjIdx, subjcIdx));
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("identifyVerbsOfBeing() {}", e);
			e.printStackTrace();
		}

		return sentence.getWordList();
	}
	
	private int identifyActionVerbSubject(ArrayList<WordToken> wordList, int verbIndex) {
	
		int subjIndex = -1;
		
		try {
			for(int i=verbIndex-1; i >= 0; i--) {
				// loop backwards and break on noun + verb or noun + modal auxiliary + verb
				if(wordList.get(i).isNoun()) {
					subjIndex = i;
					break;
				} else if(wordList.get(i).isModalAuxPOS()) {
					continue;
				} else {
					break;
				}
			}
		} catch(IndexOutOfBoundsException e) { }
	
		if(subjIndex > -1)
			wordList.get(subjIndex).setActionVerbSubject(true);
		
		return subjIndex;
	}
	
	private int identifyActionVerbDirectObject(ArrayList<WordToken> wordList, int verbIndex) {
		// intransitive verbs do not have a direct object
		int objIndex = - 1;
			
		try {
			// if action verb followed by a noun
			if(wordList.get(verbIndex+1).isNoun()) {
				// verb + noun (direct object)
				objIndex = verbIndex+1;
			} else 
				// action verb followed by an article
				if(wordList.get(verbIndex+1).isArticle()) {
					// verb + article + noun (direct object) + adj || prep
					if(wordList.get(verbIndex+2).isNoun() && (wordList.get(verbIndex+3).isAdjective() || wordList.get(verbIndex+3).isPreposition())) {
						objIndex = verbIndex+2;
					} else
						if(wordList.get(verbIndex+2).isNoun() && wordList.get(verbIndex+3).isArticle() && wordList.get(verbIndex+4).isNoun()) {
							// verb + article + noun + article + noun (direct object)
							objIndex = verbIndex+4;
						}
			}

		} catch(IndexOutOfBoundsException e) { }
	
		if(objIndex > -1)
			wordList.get(objIndex).setActionVerbDirectObject(true);
		
		return objIndex;
	}
	
	private int identifyVerbSubject(ArrayList<WordToken> wordList, int verbIndex) {
		
		// TODO what checks need to occur to ensure that wordList is valid?
	
		int subjIndex = -1;
		
		if(verbIndex == 0) {
			subjIndex = 0; // verb is first word of sentence
		} else {
			// loop backwards to find the first word preceding the verb that is not part of a prep phrase
			// and is not in the list of exclusions
			// and is not a noun phrase modifier (ensuring that it's just a normal token or, if part of a noun phrase, the HEAD)
			for(int i=verbIndex-1; i >= 0; i--) {
				// break if verb is within a dependent phrase and we've looped past the beginning of said DP
				// RULE: verb SUBJ/SUBJC cannot exist outside the DP if the verb is in a DP, nor can a SUBJ exist in a DP
				//if(wordList.get(verbIndex).isDependentPhraseMember() && !wordList.get(i).isDependentPhraseMember()) {
				if(wordList.get(verbIndex).isDependentPhraseMember() != wordList.get(i).isDependentPhraseMember()) {
					break;
				}
				
				if(!(wordList.get(i).isPrepPhraseObject() || 
					 wordList.get(i).isPrepPhraseMember() || 
					 wordList.get(i).isNounPhraseModifier() || 
					 wordList.get(i).matchesVerbSubjectExclusion() ||
					 wordList.get(i).isPunctuation() )) {
					
					subjIndex = i;
					break;
				}
			}
		}

		return subjIndex;
	}
	
	private int identifySubjectComplement(ArrayList<WordToken> wordList, int verbIndex) {
	
		int scIndex = -1;
		
		// if the term immediately following the verb ends the sentence, mark it as the subjc
		// assumption is that token[index] is as such: verb[0] token[1] .[2] (period)
		if(verbIndex == wordList.size()-2) {
			scIndex = verbIndex+1;
		} else {
			for(int i=verbIndex+1; i < wordList.size(); i++) {
				if(wordList.get(verbIndex).isDependentPhraseMember() != wordList.get(i).isDependentPhraseMember()) {
					break;
				}
				
				if(wordList.get(i).isConjunction()) {
					// default subjc to token following the verb
					scIndex = verbIndex + 1;
					break;
				} else {
					// subjc cannot be part of a prep phrase
					// nor can it be a noun phrase modifier (ensuring that it's just a normal token or, if part of a noun phrase, the HEAD)
					// nor can it appear in a list of exclusions
					if(!(wordList.get(i).isPrepPhraseObject() || 
					     wordList.get(i).isPrepPhraseMember() || 
						 wordList.get(i).isNounPhraseModifier() || 
						 wordList.get(i).matchesVerbSubjectExclusion() ||
						 wordList.get(i).isPunctuation() ||
						 wordList.get(i).isNegationToken())) {
						
						scIndex = i;
						break;
					}
				}
			}
		}
		/*
		// the goal here is to loop through all tokens, beginning with the verb, looking
		// for the last occurrence of a noun
		for(int i = vobIndex; i < wordList.size(); i++) {
			//if(wordList.get(i).isAdjective() || wordList.get(i).isNoun() || wordList.get(i).isPronoun()) {
			if(wordList.get(i).isNoun()) {
				scIndex = i;
			} else if(scIndex > -1 && !wordList.get(i).isNoun()) {
				// dealing with "evidence of X" 
				// -- Jan mentions handling it as an interphrase constructor
				// -- could also handle it here by detecting "of" as the next word
				break;
			}
		}
		*/
		return scIndex;
	}
}
