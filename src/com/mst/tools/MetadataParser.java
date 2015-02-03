package com.mst.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.TokenPosition;
import com.mst.model.TokenPositionVerbPhrase;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class MetadataParser {

	private final Pattern datePattern = Pattern.compile("\\d\\d?/\\d\\d?/\\d{4}");
	//private final Pattern numericPattern = Pattern.compile("\\b\\d+\\b");
	public final Pattern specialPattern = Pattern.compile("\\$|%|\\*|#|@|_|\\+|&|<|=|>");
	public final Pattern hyphenPattern = Pattern.compile("-|Ð|Ñ");
	public final Pattern uppercasePattern = Pattern.compile("[A-Z]{2,}");
	// regex that is numeric but also not part of a date
	// digit not bookended by / but allowing decimal and possibly preceded by a digit (eg. T11)
	public final Pattern numericPattern = Pattern.compile("(?<!\\/)\\b[A-Z]*\\d+\\.*\\d*\\b(?!\\/)");
	
	public void parseComplex(Sentence sentence) {
		ArrayList<WordToken> words = sentence.getWordList();
		SentenceMetadata metadata = sentence.getMetadata();
		int verbCount = 0;
		boolean compoundVerb = false;
		int i = 0;
		
		if(containsPattern(datePattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsDate", true);
		}
		if(containsPattern(numericPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsNumeric", true);
		}
		if(containsPattern(specialPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsSpecialChar", true);
		}
		if(containsPattern(hyphenPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsHyphen", true);
		}
		if(containsPattern(uppercasePattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsUppercase", true);
		}
		
		for(WordToken word : words) {
			try {
				if(i == 0) {
					if(word.isPrepPhraseMember()) {
						metadata.addSimpleMetadataValue("beginsWithPreposition", true);
					}			
				}
				
				//if(word.isConjunction() && verbCount > 0) {
				//	compoundVerb = true;
				//}
				
				/* PREP phrases */
				if(word.isPrepPhraseObject() && !word.isPrepPhraseMember()) {
					PrepPhraseMetadata pp = new PrepPhraseMetadata();
					
					// reached the final token of the prep phrase
					pp.setPhrase(buildPrepPhrase(words, i));
						
					// check negation, beginning with position of prep phrase start
					pp.setNegated(checkNegation(words, pp.getPhrase().get(0).getPosition()));
					
					metadata.addPrepMetadata(pp);
				}
				
				/* VERB phrases */
				if(word.isVerbOfBeing() || word.isLinkingVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(word.isVerbOfBeing() ? Constants.VerbClass.VERB_OF_BEING : Constants.VerbClass.LINKING_VERB);
					vp.setVerb(new TokenPosition(word.getToken(), i));
					vp.setSubj(findSubject(words, i, vp.getVerbClass()));
					vp.setSubjC(findObjectOrSubjectComplement(words, i, vp.getVerbClass()));
					
					if(vp.getSubj() != null) {
						//vp.setSubjModByPP(isTokenModifiedByPrepPhrase(words, vp.getSubj().getPosition()));
						vp.getSubj().setModByPP(isTokenModifiedByPrepPhrase(words, vp.getSubj().getPosition()));
						
						//vp.setSubjEqNPHead(isTokenNounPhraseHead(words, vp.getSubj().getPosition()));
						vp.getSubj().setEqNPHead(isTokenNounPhraseHead(words, vp.getSubj().getPosition()));
						// negation
						//vp.setSubjectNegated(checkSubjNegation(words, vp.getSubj().getPosition()));
						vp.getSubj().setNegated(checkSubjNegation(words, vp.getSubj().getPosition()));
					}
					
					if(vp.getSubjC() != null) {
						//vp.setSubjCModByPP(isTokenModifiedByPrepPhrase(words, vp.getSubjC().getPosition()));
						vp.getSubjC().setModByPP(isTokenModifiedByPrepPhrase(words, vp.getSubjC().getPosition()));
						
						//vp.setSubjCEqNPHead(isTokenNounPhraseHead(words, vp.getSubjC().getPosition()));
						vp.getSubjC().setEqNPHead(isTokenNounPhraseHead(words, vp.getSubjC().getPosition()));
						// negation
						//vp.setSubjectComplementNegated(checkSubjCNegation(words, vp.getSubjC().getPosition()));
						vp.getSubjC().setNegated(checkSubjCNegation(words, vp.getSubjC().getPosition()));
					}
					
					metadata.addVerbMetadata(vp);
											
					verbCount++;
					
				} else if(word.isInfinitiveVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.INFINITIVE);
					vp.setVerb(new TokenPosition(word.getToken(), i));
					
					// TODO negated
					
					// infinitive follows prep phrase
					try {
						if(i > 0 && words.get(i-1).isPrepPhraseObject())
							vp.setInfFollowsPP(true);
					} catch(IndexOutOfBoundsException e) { }
					
					metadata.addVerbMetadata(vp);
					
					verbCount++;
					
				} else if(word.isActionVerb()) {				
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.ACTION);
					vp.setVerb(new TokenPosition(word.getToken(), i));
					vp.setSubj(findSubject(words, i, vp.getVerbClass()));
					vp.setSubjC(findObjectOrSubjectComplement(words, i, vp.getVerbClass()));
					
					if(vp.getSubjC() != null) {
						vp.setIntransitive(false); // i.e. verb is transitive (has an object)
					}
				}
				
				
				/* NOUN phrases */
				if(word.isNounPhraseHead()) {
					NounPhraseMetadata np = new NounPhraseMetadata();
					// noun phrase HEAD = prep phrase OBJ
					if(word.isPrepPhraseObject()) {
						np.setHeadEqPPObj(true);
					}
					try {
						// noun phrase HEAD is modified by prep phrase
						if(i < words.size()-1 && words.get(i+1).isPrepPhraseMember()) {
							np.setModByPP(true);
						}
					} catch(IndexOutOfBoundsException e) { }
					
					// noun phrase
					np.setPhrase(buildNounPhrase(words, i));
					
					// check negation, from start of phrase
					np.setNegated(checkNegation(words, np.getPhrase().get(0).getPosition()));
					
					// contains verb, backwards from head
					//nounPhraseContainsVerb(words, i);
					
					metadata.addNounMetadata(np);
				}
				
				i++;
				
			} catch(Exception e) { 
				e.printStackTrace(); 
			}
		}
		
		// compound verb phrases
		if(compoundVerb) {
			for(VerbPhraseMetadata phrase : metadata.getVerbMetadata())
				// set every verb phrase compound = true
				phrase.setCompound(true);
		}
		
		for(NounPhraseMetadata phrase : metadata.getNounMetadata()) {
			// contains comma
			for(TokenPosition tp : phrase.getPhrase()) {
				if(tp.getToken().equals(",")) {
					metadata.addSimpleMetadataValue("NPContainsComma", true);
					break;
				}
			}
		}
	}
	
	private TokenPositionVerbPhrase findSubject(ArrayList<WordToken> words, int verbPos, Constants.VerbClass _class) {
		TokenPositionVerbPhrase tpos = null;
		
		for(int i=verbPos-1; i >= 0; i--) {
			switch(_class) {
				case ACTION:
					if(words.get(i).isActionVerbSubject()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				case VERB_OF_BEING:
					if(words.get(i).isVerbOfBeingSubject()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				case LINKING_VERB:
					if(words.get(i).isLinkingVerbSubject()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				default:
					
					break;
			}
			if(tpos != null)
				break;
		}
		
		return tpos;
	}
	
	private TokenPositionVerbPhrase findObjectOrSubjectComplement(ArrayList<WordToken> words, int verbPos, Constants.VerbClass _class) {
		TokenPositionVerbPhrase tpos = null;
		
		for(int i=verbPos+1; i < words.size(); i++) {
			switch(_class) {
				case ACTION:
					if(words.get(i).isActionVerbDirectObject()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				case VERB_OF_BEING:
					if(words.get(i).isVerbOfBeingSubjectComplement()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				case LINKING_VERB:
					if(words.get(i).isLinkingVerbSubjectComplement()) {
						tpos = new TokenPositionVerbPhrase(words.get(i).getToken(), i);
					}
					break;
				default:
					
					break;
			}
			if(tpos != null)
				break;
		}
		
		return tpos;
	}
	
	private boolean checkSubjCNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backwards from subject complement to verb
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isVerbOfBeing() || words.get(i).isLinkingVerb()) {
					break;
				} else if(words.get(i).isNegationToken()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkSubjNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backwards from subject to start of sentence
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isNegationToken()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {	
			if(words.get(tokenPos-1).isNegationToken()) {
				ret = true;
			} else if(words.get(tokenPos-1).isArticle() && words.get(tokenPos-2).isNegationToken()) {
				ret = true;
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean containsPattern(Pattern pattern, String fullSentence) {
		Matcher matcher = pattern.matcher(fullSentence);
		return matcher.find();
	}
	
	private TokenPosition isTokenModifiedByPrepPhrase(ArrayList<WordToken> words, int tokenPos) {
		TokenPosition tpos = null;
		
		try {
			// this token is NOT a prep phrase member but the next token IS (the final token of a prep phrase is never a member)	
			if(!words.get(tokenPos).isPrepPhraseMember() && words.get(tokenPos+1).isPrepPhraseMember()) {
				// return object or preposition?
				tpos = new TokenPosition(words.get(tokenPos+1).getToken(), tokenPos+1);
				/*
				for(int i=tokenPos+1; i < words.size(); i++) {
					if(words.get(i).isPrepPhraseObject()) {
						tpos = new TokenPosition(words.get(i).getToken(), i);
						break;
					}
				}
				*/
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return tpos;
	}
	
	private boolean isTokenNounPhraseHead(ArrayList<WordToken> words, int tokenPos) {
		return words.get(tokenPos).isNounPhraseHead();		
	}

	private List<TokenPosition> getCompoundVerbs(ArrayList<WordToken> words, int startPos) {
		List<TokenPosition> tposList = new ArrayList<TokenPosition>();
		boolean conjFound = false;
		
		// add initial verb
		tposList.add(new TokenPosition(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos+1; i < words.size(); i++) {
			if(words.get(i).isConjunction()) {
				conjFound = true;
				tposList.add(new TokenPosition(words.get(i).getToken(), i));
			} else {
				if(conjFound) {
					if(words.get(i).isLinkingVerb() || words.get(i).isInfinitiveVerb() || words.get(i).isVerbOfBeing()) {
						tposList.add(new TokenPosition(words.get(i).getToken(), i));
						conjFound = false;
					}
				}
			}
			
		}
		
		// don't return empty, single-verb, or verb/conj-only list
		if(tposList.size() <= 2)
			tposList = null;
		
		return tposList;
	}

	private List<TokenPosition> buildNounPhrase(ArrayList<WordToken> words, int startPos) {
		List<TokenPosition> tposList = new ArrayList<TokenPosition>();
		
		// add initial noun phrase HEAD
		tposList.add(new TokenPosition(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos-1; i >= 0; i--) {
			if(words.get(i).isNounPhraseModifier()) {
				tposList.add(new TokenPosition(words.get(i).getToken(), i));
			} else {
				break;
			}
		}
		
		if(tposList.isEmpty())
			tposList = null;
		else
			tposList = Lists.reverse(tposList);
		
		return tposList;
	}
	
	// for error-checking purposes. may remove in time.
//	private boolean nounPhraseContainsVerb(ArrayList<WordToken> words, int startPos) {
//		boolean ret = false;
//		
//		for(int i=startPos-1; i >= 0; i--) {
//			if(words.get(i).isNounPhraseModifier()) {
//				if(words.get(i).isVerb()) {
//					ret = true;
//					break;
//				}
//			} else {
//				break;
//			}
//		}
//		
//		return ret;
//	}
	
	private List<TokenPosition> buildPrepPhrase(ArrayList<WordToken> words, int startPos) {
		List<TokenPosition> tposList = new ArrayList<TokenPosition>();
		
		// add prep phrase object
		tposList.add(new TokenPosition(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos-1; i >= 0; i--) {
			if(words.get(i).isPrepPhraseMember()) {
				tposList.add(new TokenPosition(words.get(i).getToken(), i));
			} else {
				break;
			}
		}
		
		if(tposList.isEmpty())
			tposList = null;
		else
			tposList = Lists.reverse(tposList);
		
		return tposList;
	}
}
