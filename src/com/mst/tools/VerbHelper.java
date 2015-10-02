package com.mst.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.VerbPhraseToken;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class VerbHelper {
	private final String INFINITIVE_HEAD_TERM = "(?i)to";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Pattern ALLOWABLE_JJ_REGEX = Pattern.compile("\\.|,|\\(|\\!");
	private final Pattern PAREN_BEGIN_REGEX = Pattern.compile("\\(|\\[|\\{");
	private final Pattern PAREN_END_REGEX = Pattern.compile("\\)|\\]|\\}");
	
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
							// if first token of sentence, or preceding token is NOT a verb
							if(i==0 || !sentence.getWordList().get(i-1).isVerb()) {
								sentence.getWordList().get(i).setInfinitiveHead(true);
								sentence.getWordList().get(i+1).setInfinitiveVerb(true);
							}
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
	
	//  must be preceded by POSTagger.identifyPartsOfSpeech(), POSTagger.identifyPrepPhrases(), identifyVerbsOfBeing(), identifyModalAuxiliaryVerbs
	public ArrayList<WordToken> identifyLinkingVerbs(Sentence sentence) {
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		// TODO
		// why didn't "has all been" come through here...?
		// His bloodwork has all been good, and his last scan in 03/01/2014 was not convincing for any metastases where he is feeling pain.
	
		
		try {
			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);
				
				if(thisToken.isLinkingVerbSignal() && // verb in the list
					!(thisToken.isVerbOfBeing() || thisToken.isModalAuxVerb()) && // verb not marked as a verb of being or modal aux
					!(thisToken.isPrepPhraseMember() || thisToken.isPrepPhraseObject())) { // verb not part of a prep phrase
					
					// avoid indexOOB
					// added RB on 5/5/15 to support sentences such as "PSA is now 1.34."
					//if(i < words.size()-1 && words.get(i+1).getPOS().matches("RB|DT|JJ|NN|NNS|CD")) { // verb's successor is determiner, adjective, noun or number
					if(i < words.size()-1 && words.get(i+1).getPOS().matches("RB|DT|JJ|NN(S|P|PS)?|CD")) { // verb's successor is determiner, adjective, noun or number
						thisToken.setLinkingVerb(true);
						
						int subjIdx = identifyVerbSubject(words, i);
						List<Integer> subjcIdxs = identifySubjectComplement(words, i, false);
						
						if(subjIdx != -1)
							words.get(subjIdx).setLinkingVerbSubject(subjIdx != -1);

						if(!subjcIdxs.isEmpty())
							for(int idx : subjcIdxs)
								words.get(idx).setVerbOfBeingSubjectComplement(true);

						sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.LINKING_VERB, i, words, subjIdx, subjcIdxs));
						i++; // increment past the verb's successor
					}
				}
			}
			
			//sentence.setWordList(words);
			
		} catch(Exception e) {
			logger.error("identifyLinkingVerbs() {}", e);
		}

		return words;
	}
	
	// requires POSTagger.identifyPartsOfSpeech()
	public ArrayList<WordToken> identifyModalAuxiliaryVerbs(Sentence sentence) {
		
		int mvMetadataIndex = -1;
		boolean mvFound = false;
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);
				try {
					// first time around, compare against just the list of MV signals
					// if a MV is found, check subsequent tokens for verb POS to allow for compound
					if(!mvFound && thisToken.isModalAuxSignal()) {
						mvFound = true;
						
						thisToken.setModalAuxVerb(true);

						// now identify subject and subject complement and set metadata
						int subjIdx = identifyVerbSubject(words, i);
						
						if(subjIdx != -1) 
							words.get(subjIdx).setVerbOfBeingSubject(true);

						mvMetadataIndex = sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.MODAL_AUX, i, words, subjIdx, null));
					
					} else if(mvFound && (thisToken.isVerbOfBeingSignal() || thisToken.isLinkingVerbSignal())) {
						sentence.getMetadata().getVerbMetadata().get(mvMetadataIndex).addVerb(new VerbPhraseToken(thisToken.getToken(), i));
						thisToken.setModalAuxVerb(true);
						
					} else {
						if(!thisToken.isAdverbPOS()) // allow for ex. "may still be taking"
							mvFound = false;
					}
				} catch(IndexOutOfBoundsException e) { }
			}
			
			// because MVs can be compound (e.g. "may be taking"), identification of SUBJC must be delayed until the entire phrase is built
			for(VerbPhraseMetadata vpm : sentence.getMetadata().getVerbMetadata()) {
				if(vpm.getVerbClass() == Constants.VerbClass.MODAL_AUX) {
					int lastVerbIdx = vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition();
					List<Integer> subjcIdxs = identifySubjectComplement(words, lastVerbIdx, vpm.getVerbs().size() > 1);
					
					for(int idx : subjcIdxs) {
						words.get(idx).setModalSubjectComplement(true);
						vpm.getSubjC().add(new VerbPhraseToken(words.get(idx).getToken(), idx));
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("identifyModalAuxiliaryVerbs() {}", e);
		}

		return words;
	}
	
	public ArrayList<WordToken> identifyModalAuxiliaryVerbs2(Sentence sentence) {
		
		int mvMetadataIndex = -1;
		boolean mvFound = false;
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);
				try {
					// first time around, compare against just the list of MV signals
					// if a MV is found, check subsequent tokens for verb POS to allow for compound
					if(!mvFound && thisToken.isModalAuxSignal()) {
						mvFound = true;
						
						thisToken.setModalAuxVerb(true);

						// now identify subject and subject complement and set metadata
						int subjIdx = identifyVerbSubject(words, i);
						
						if(subjIdx != -1) 
							words.get(subjIdx).setVerbOfBeingSubject(true);

						mvMetadataIndex = sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.MODAL_AUX, i, words, subjIdx, null));
					
					} else if(mvFound && thisToken.isVerb()) {
						sentence.getMetadata().getVerbMetadata().get(mvMetadataIndex).addVerb(new VerbPhraseToken(thisToken.getToken(), i));
						thisToken.setModalAuxVerb(true);
						
					} else {
						if(!thisToken.isAdverbPOS()) // allow for ex. "may still be taking"
							mvFound = false;
					}
				} catch(IndexOutOfBoundsException e) { }
			}
			
			// because MVs can be compound (e.g. "may be taking"), identification of SUBJC must be delayed until the entire phrase is built
			for(VerbPhraseMetadata vpm : sentence.getMetadata().getVerbMetadata()) {
				if(vpm.getVerbClass() == Constants.VerbClass.MODAL_AUX) {
					int lastVerbIdx = vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition();
					List<Integer> subjcIdxs = identifySubjectComplement(words, lastVerbIdx, vpm.getVerbs().size() > 1);
					
					for(int idx : subjcIdxs) {
						words.get(idx).setModalSubjectComplement(true);
						vpm.getSubjC().add(new VerbPhraseToken(words.get(idx).getToken(), idx));
						
						// SRD 9/4/15 - experiment with allowing verbs that end in -ed and -ing
						// remove any verbs from compound list that have been marked as SUBJC
						//       to back out- remove this and one line from identifySubjectComplement.
						//for(int i=vpm.getVerbs().size()-1; i >= 0 ; i--)
						//	if(vpm.getVerbs().get(i).getPosition() == idx)
						//		vpm.getVerbs().remove(i);
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("identifyModalAuxiliaryVerbs() {}", e);
		}

		return words;
	}
	
	// requires POSTagger.identifyPartsOfSpeech(), identifyVerbsOfBeing(), identifyLinkningVerbs(), identifyInfinitivePhrases(), identifyModalAuxVerbs()
	public ArrayList<WordToken> identifyActionVerbs(Sentence sentence) {
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			for(int i=0; i < words.size(); i++) {
				WordToken thisToken = words.get(i);

				if(thisToken.isVerb() && !(thisToken.isLinkingVerb() ||
										   thisToken.isLinkingVerbSubject() ||
										   thisToken.isLinkingVerbSubjectComplement() ||
										   thisToken.isVerbOfBeing() || 
										   thisToken.isVerbOfBeingSubject() ||
										   thisToken.isVerbOfBeingSubjectComplement() ||
										   thisToken.isInfinitiveVerb() || 
										   thisToken.isPrepositionalVerb() ||
										   thisToken.isModalAuxVerb())) {
					
					thisToken.setActionVerb(true);

					int subjIdx = identifyVerbSubject(words, i);
					List<Integer> subjcIdxs = identifySubjectComplement(words, i, false);
					
					if(subjIdx > -1)
						words.get(subjIdx).setActionVerbSubject(true);
					
					if(!subjcIdxs.isEmpty())
						for(int idx : subjcIdxs)
							words.get(idx).setActionVerbDirectObject(true);
					
					sentence.getMetadata().addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.ACTION, i, words, subjIdx, subjcIdxs));
				}
			}
		} catch(Exception e) {
			logger.error("identifyActionVerbs() {}", e);
		}

		return words;
	}
	
	private VerbPhraseMetadata setVerbPhraseMetadata(Constants.VerbClass _class, int verbIdx, ArrayList<WordToken> words, int subjIdx, List<Integer> objIdxs) {
		VerbPhraseMetadata vpm = new VerbPhraseMetadata(_class);
		
		vpm.addVerb(new VerbPhraseToken(words.get(verbIdx).getToken(), verbIdx));
		
		if(subjIdx != -1) {
			vpm.setSubj(new VerbPhraseToken(words.get(subjIdx).getToken(), subjIdx));
		}
		
		if(objIdxs != null) {
			for(int idx : objIdxs)
				vpm.addSubjC(new VerbPhraseToken(words.get(idx).getToken(), idx));
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
						
							if(prevToken.isAdverbPOS() || prevToken.getToken().endsWith("(?i)ly")) {
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
	
	// must be preceded by POSTagger.identifyPartsOfSpeech(), POSTagger.identifyPrepPhrases(), identifyModalAuxVerbs()
	public ArrayList<WordToken> identifyVerbsOfBeing(Sentence sentence) {
		
		int vobMetadataIndex = -1; // keeps track of which index in metadata.verbPhrases this entry has been added
		boolean vobFound = false;
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			SentenceMetadata metadata = sentence.getMetadata();
			
			for(int i=0; i < words.size(); i++) {
				if(i < words.size() - 1) { // avoid IndexOutOfBounds exception
					WordToken thisToken = words.get(i);
					
					// first time around, compare against the list of VOB signals
					// if a VOB is found, check subsequent tokens for verb POS to allow for compound
					if(!vobFound && thisToken.isVerbOfBeingSignal() && !thisToken.isModalAuxVerb()) {
						int vobIndex = i;
						// process up to three tokens following the verb of being 
						int tokensRemaining = Math.min(3, words.size()-1 - vobIndex);
							
						for(int j=vobIndex + tokensRemaining; j > vobIndex; j--) {
						    if(words.get(j).isVerb() || words.get(j).isPreposition()) {
								vobFound = true;
								
								thisToken.setVerbOfBeing(true);
								
								// now identify subject and set metadata
								int subjIdx = identifyVerbSubject(words, vobIndex);
								
								if(subjIdx != -1)
									words.get(subjIdx).setVerbOfBeingSubject(true);
								
								// create new verb metadata entry, returning index of new entry for later use
								// note that SUBJC identification is delayed until later, in its own loop
								vobMetadataIndex = metadata.addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.VERB_OF_BEING, vobIndex, words, subjIdx, null));
								
								break;
						    }
						}
					} else if(vobFound && (thisToken.isVerbOfBeingSignal() || thisToken.isLinkingVerbSignal())) {
						// part of a multi-token verb. add this verb to the existing metadata verb phrase object
						metadata.getVerbMetadata().get(vobMetadataIndex).addVerb(new VerbPhraseToken(thisToken.getToken(), i));
						thisToken.setVerbOfBeing(true);
						
					} else {
						if(!thisToken.isAdverbPOS()) // allow for ex. "is still taking"
							vobFound = false;
					}
				}
			}
			
			// because VOBs can be compound (e.g. "is taking"), identification of SUBJC must be delayed until the entire phrase is built
			for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
				if(vpm.getVerbClass() == Constants.VerbClass.VERB_OF_BEING) {
					int lastVerbIdx = vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition();
					List<Integer> subjcIdxs = identifySubjectComplement(words, lastVerbIdx, vpm.getVerbs().size() > 1);
					
					for(int idx : subjcIdxs) {
						words.get(idx).setVerbOfBeingSubjectComplement(true);
						vpm.getSubjC().add(new VerbPhraseToken(words.get(idx).getToken(), idx));
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("identifyVerbsOfBeing() {}", e);
			e.printStackTrace();
		}

		return words;
	}
	
	public ArrayList<WordToken> identifyVerbsOfBeing2(Sentence sentence) {
		
		int vobMetadataIndex = -1; // keeps track of which index in metadata.verbPhrases this entry has been added
		boolean vobFound = false;
		
		ArrayList<WordToken> words = sentence.getWordList();
		
		try {
			SentenceMetadata metadata = sentence.getMetadata();
			
			for(int i=0; i < words.size(); i++) {
				//if(i < words.size() - 1) { // avoid IndexOutOfBounds exception. TODO still necessary?
					WordToken thisToken = words.get(i);
					
					// first time around, compare against the list of VOB signals
					// if a VOB is found, check subsequent tokens for verb POS to allow for compound
					if(!vobFound && thisToken.isVerbOfBeingSignal() && !thisToken.isModalAuxVerb()) { // TODO isModal() necessary? these two lists don't share any tokens
						int vobIndex = i;
						// process up to three tokens following the verb of being 
						int tokensRemaining = Math.min(3, words.size()-1 - vobIndex);
							
						for(int j=vobIndex + tokensRemaining; j > vobIndex; j--) {
						    if(words.get(j).isVerb() || words.get(j).isPreposition()) {
								vobFound = true;
								
								thisToken.setVerbOfBeing(true);
								
								// now identify subject and set metadata
								int subjIdx = identifyVerbSubject(words, vobIndex);
								
								if(subjIdx != -1)
									words.get(subjIdx).setVerbOfBeingSubject(true);
								
								// create new verb metadata entry, returning index of new entry for later use
								// note that SUBJC identification is delayed until later, in its own loop
								vobMetadataIndex = metadata.addVerbMetadata(setVerbPhraseMetadata(Constants.VerbClass.VERB_OF_BEING, vobIndex, words, subjIdx, null));
								
								break;
						    }
						}

					} else if(vobFound && thisToken.isVerb()) {
						// part of a compound verb. add this verb to the existing metadata verb phrase object
						metadata.getVerbMetadata().get(vobMetadataIndex).addVerb(new VerbPhraseToken(thisToken.getToken(), i));
						thisToken.setVerbOfBeing(true);
					} else {
						if(!thisToken.isAdverbPOS()) // allow for ex. "is still taking"
							vobFound = false;
					}
				//}
			}
			
			// because VOBs can be compound (e.g. "is taking"), identification of SUBJC must be delayed until the entire phrase is built
			for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
				if(vpm.getVerbClass() == Constants.VerbClass.VERB_OF_BEING) {
					// all of these indexes are 1-based
					int lastVerbIdx = vpm.getVerbs().get(vpm.getVerbs().size()-1).getPosition();
					List<Integer> subjcIdxs = identifySubjectComplement(words, lastVerbIdx, vpm.getVerbs().size() > 1);
					
					for(int idx : subjcIdxs) {
						words.get(idx).setVerbOfBeingSubjectComplement(true);
						vpm.getSubjC().add(new VerbPhraseToken(words.get(idx).getToken(), idx));
						
						// SRD 9/4/15 - experiment with allowing verbs that end in -ed and -ing
						// remove any verbs from compound list that have been marked as SUBJC
						//       to back out- remove this and one line from identifySubjectComplement.
						//for(int i=vpm.getVerbs().size()-1; i >= 0 ; i--)
						//	if(vpm.getVerbs().get(i).getPosition() == idx)
						//		vpm.getVerbs().remove(i);
					}
				}
			}
			
		} catch(Exception e) {
			logger.error("identifyVerbsOfBeing() {}", e);
			e.printStackTrace();
		}

		return words;
	}
	
	private int identifyActionVerbSubject(ArrayList<WordToken> wordList, int verbIndex) {
	
		int subjIndex = -1;
		
		try {
			for(int i=verbIndex-1; i >= 0; i--) {
				// loop backwards and break on noun + verb or noun + modal auxiliary + verb
				if(wordList.get(i).isNounPOS() || wordList.get(i).isPronounPOS()) {
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
			if(wordList.get(verbIndex+1).isNounPOS()) {
				// verb + noun (direct object)
				objIndex = verbIndex+1;
			} else 
				// action verb followed by an article
				if(wordList.get(verbIndex+1).isArticle()) {
					// verb + article + noun (direct object) + adj || prep
					if(wordList.get(verbIndex+2).isNounPOS() && (wordList.get(verbIndex+3).isAdjectivePOS() || wordList.get(verbIndex+3).isPreposition())) {
						objIndex = verbIndex+2;
					} else
						if(wordList.get(verbIndex+2).isNounPOS() && wordList.get(verbIndex+3).isArticle() && wordList.get(verbIndex+4).isNounPOS()) {
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
	
		int subjIndex = -1;
		int stack = 0;
		
		if(verbIndex == 0) {
			subjIndex = 0; // verb is first word of sentence
		} else {
			// loop backwards to find the first word preceding the verb that is not part of a prep phrase
			// and is not in the list of exclusions
			// and is not a noun phrase modifier (ensuring that it's just a normal token or, if part of a noun phrase, the HEAD)
			for(int i=verbIndex-1; i >= 0; i--) {
				WordToken token = wordList.get(i);
				// break if verb is within a dependent phrase and we've looped past the beginning of said DP
				// RULE: verb SUBJ/SUBJC cannot exist outside the DP if the verb is in a DP, nor can a SUBJ exist in a DP
				// RULE: verb SUBJ/SUBJC cannot be within parentheses.
				if(wordList.get(verbIndex).isDependentPhraseMember() != token.isDependentPhraseMember())
					break;
				
				if(token.isSubjectComplement())
					break;
				
				// these are reversed from what is seen in identifySubjectComplement() because we're looping backwards.
				if(PAREN_BEGIN_REGEX.matcher(token.getToken()).matches())
					stack--;
				else if(PAREN_END_REGEX.matcher(token.getToken()).matches())
					stack++;
				
				if(stack == 0 &&
				   !(token.isPrepPhraseObject() || 
					 token.isPrepPhraseMember() || 
					 token.isNounPhraseModifier() || 
					 token.matchesVerbSubjectExclusion() ||
					 token.isVerb() ||
					 token.isModalAuxPOS() ||
					 token.isPunctuation() ||
					 token.isNegationSignal())) { // SRD 7/10/15 - "... while risk factors do not include coronary artery disease,..."
					
					subjIndex = i;
					break;
				}
			}
		}

		return subjIndex;
	}
	
	private List<Integer> identifySubjectComplement(ArrayList<WordToken> wordList, int verbIndex, boolean compoundVerb) {
		
		List<Integer> scIndexes = new ArrayList<Integer>();
		
		// if the term immediately following the verb ends the sentence, mark it as the subjc
		int stack = 0;
	
		for(int i=verbIndex+1; i < wordList.size(); i++) {
			WordToken token = wordList.get(i);
			
			// RULE: verb and subjc must both exist, or not exist, within a DP (no crossing borders)
			if(wordList.get(verbIndex).isDependentPhraseMember() != token.isDependentPhraseMember()) {
				break;
			}
			
			// example where we might want to allow a verb as subjc...
			//   "In the last few years his PSA has been slowly rising."
			if(token.isPrepPhraseObject() || token.isPrepPhraseMember() || token.isVerb()) {
				break;
			}
			//He started Lupron and Xgeva and Firmagon in 2014.
			// TODO need an example to explain why I took out !token.isNounPhraseModifier()
			//  an example sentence that supports adding it back is: [He] [is having] [catheter related pain] and I wrote an Rx.
			//  "She is having random wuperbupic pain and she has seen gyn and ruled out that as an etiology." <- pain, she are compound SUBJCs
			// TODO should presence of a prep phrase short-circuit SUBJC detection?
			// TODO should VB be prohibited?
			// TODO check this sentence (pain and wrote are subjcs):
			//   He is having catheter related pain and I wrote an Rx.
			
			if(PAREN_BEGIN_REGEX.matcher(token.getToken()).matches())
				stack++;
			else if(PAREN_END_REGEX.matcher(token.getToken()).matches())
				stack--;
			
			if(stack == 0 && 
			   !(token.isNounPhraseModifier() || 
				 //token.matchesVerbSubjectExclusion() ||
			     token.isConjunctionPOS() ||
				 token.isPunctuation() ||
				 token.isPreposition() ||
				 token.isDeterminerPOS())) {
				
				WordToken nextToken = null;
				boolean compoundSUBJC = false;
				try {
					nextToken = wordList.get(i+1);
					// conjunction, comma, adverb, adjective following the current token allow the loop to continue
					if(nextToken.isConjunctionPOS() || nextToken.getPOS().equalsIgnoreCase(",") || nextToken.isAdverbPOS() || nextToken.isAdjectivePOS())
						compoundSUBJC = true;
					
				} catch(IndexOutOfBoundsException e) { }
				
				if(nextToken == null) {
					// final token, no period
					scIndexes.add(i);
				} else {
					if(token.isAdjectivePOS() || token.isAdverbPOS()) {	
						// JJ/RB are allowed if the following token is .,(! or a prep token or a dependent signal or a conjunction
						if(ALLOWABLE_JJ_REGEX.matcher(nextToken.getToken()).matches() ||
							nextToken.isPreposition() ||
							nextToken.isDependentPhraseBegin() || 
							nextToken.isConjunctionPOS()) {
							
							scIndexes.add(i);
						}
					} else {
						scIndexes.add(i);
					}
					
					if(!compoundSUBJC)
						break;
				}
				
//				if(token.isAdjectivePOS() || token.isAdverbPOS()) {
//					if(nextToken != null) {
//						// JJ/RB are allowed if the following token is .,(! or a prep token or a dependent signal or a conjunction
//						if(ALLOWABLE_JJ_REGEX.matcher(nextToken.getToken()).matches() ||
//							nextToken.isPreposition() ||
//							nextToken.isDependentPhraseBegin() || 
//							nextToken.isConjunctionPOS()) {
//							
//							scIndexes.add(i);
//						}
//						
//					} else {
//						// JJ/RB are allowed if not preceded by another token (period is missing)
//						scIndexes.add(i);
//					}
//				} else {
//					// some other POS came through
//					scIndexes.add(i);
//				}
//				
//				if(nextToken != null && !(nextToken.isConjunctionPOS() || nextToken.isAdverbPOS() || nextToken.getPOS().equalsIgnoreCase(",")))
//					break;
				
			}
		}
		
		// SRD 9/4/15 - experiment with allowing verbs that end in -ed and -ing
		// no SUBJC found by normal means and the verb is compound
		// set the final verb in this case (if it meets the endsWith() criteria) as the SUBJC
		if(scIndexes.size() == 0 && compoundVerb) {
			String finalVerb = wordList.get(verbIndex).getToken();
			if(finalVerb.endsWith("ed") || finalVerb.endsWith("ing"))
				scIndexes.add(verbIndex);
		}

		return scIndexes;
	}
	
}
