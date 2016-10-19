package com.mst.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;

import com.google.common.collect.Lists;
import com.mst.model.DependentPhraseMetadata;
import com.mst.model.ModByPPMetadata;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.OrphanMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.GenericToken;
import com.mst.model.VerbPhraseToken;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class MetadataParser {

	private final Pattern datePattern = Pattern.compile("\\d\\d?/\\d\\d?/\\d{4}");
	//private final Pattern numericPattern = Pattern.compile("\\b\\d+\\b");
	public final Pattern specialPattern = Pattern.compile("\\$|%|\\*|#|@|_|\\+|&|<|=|>");
	public final Pattern hyphenPattern = Pattern.compile("-|�|�");
	public final Pattern uppercasePattern = Pattern.compile("[A-Z]{2,}");
	// regex that is numeric but also not part of a date
	// digit not bookended by / but allowing decimal and possibly preceded by a character (eg. T11)
	public final Pattern numericPattern = Pattern.compile("(?<!\\/)\\b[A-Z]*\\d+\\.*\\d*\\b(?!\\/)");
	
	public void parseComplex(Sentence sentence) {
		ArrayList<WordToken> words = sentence.getWordList();
		SentenceMetadata metadata = sentence.getMetadata();
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
		if(containsPattern(Constants.NEGATION, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsNegation", true);
		}
		// TODO add containsMultipleVerbPhrases, possibly containsParens
		
		for(WordToken word : words) {
			try {
				if(i == 0) {
					if(word.isPrepPhraseMember()) {
						metadata.addSimpleMetadataValue("beginsWithPreposition", true);
					}			
				}
				
				/* PREP phrases */
				if(word.isPrepPhraseObject() && !word.isPrepPhraseMember()) {
					PrepPhraseMetadata pp = new PrepPhraseMetadata();
					
					// reached the final token of the prep phrase
					pp.setPhrase(buildPrepPhrase(words, i));
						
					// check negation, beginning with position of prep phrase start
					pp.setNegated(checkNegation(words, pp.getPhrase().get(0).getPosition()));
					
					// check intra-pp tokens for negation
					for(PrepPhraseToken token : pp.getPhrase()) {
						if(words.get(token.getPosition()).isNegationSignal()) {
							pp.setNegated(true);
							break;
						}
					}
					
					try {
						if(words.get(pp.getPhrase().get(0).getPosition()-1).isVerbPhraseVerb()) {
							pp.setModifiesVerb(true);
						}
					} catch(Exception e) { }
					
					// first stab at a heuristics-based ST for PPs
					if(pp.getPhrase().get(0).getToken().equalsIgnoreCase("in")) {
						String finalST = words.get(pp.getPhrase().get(pp.getPhrase().size()-1).getPosition()).getSemanticType();
						if(finalST != null) {
							if(finalST.startsWith("bpoc") ) {
								pp.setSemanticType("Finding Type");  // TODO possibly store in a var called 'frame' rather than 'st' but it's sort of functioning as an st
							} else if(finalST.startsWith("tempor") ) {
								pp.setSemanticType("Period of Time");
							} else {
								pp.setSemanticType("Location");
							}
						} else {
							pp.setSemanticType("Location");
						}
					}
					
					metadata.addPrepMetadata(pp);
				}

				
				/* VERB phrases (not handled in VerbPhraseHelper) */
				if(word.isInfinitiveVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.INFINITIVE);
					
					// grab previous token, even though it'll always be "to"
					WordToken prevToken = Constants.getToken(words, i-1);
					
					vp.addVerb(new VerbPhraseToken(prevToken.getToken(), i-1));
					vp.addVerb(new VerbPhraseToken(word.getToken(), i));
					
					// infinitive follows prep phrase
					WordToken prevPrevToken = Constants.getToken(words, i-2);
					
					if(prevPrevToken.isPrepPhraseObject())
						vp.setInfFollowsPP(true);
					
					metadata.addVerbMetadata(vp);
				}	
					
				if(word.isPrepositionalVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.PREPOSITIONAL);
					vp.addVerb(new VerbPhraseToken(word.getToken(), i));
					
					// TODO negated?
									
					metadata.addVerbMetadata(vp);
				} 
				
				/* NOUN phrases */
				if(word.isNounPhraseHead()) {
					NounPhraseMetadata np = new NounPhraseMetadata();
					// noun phrase HEAD = prep phrase OBJ
					if(word.isPrepPhraseObject()) {
						np.setWithinPP(true);
					}
					// noun phrase
					np.setPhrase(buildNounPhrase(words, i));
					
					// check negation, from start of phrase
					np.setNegated(checkNegation(words, np.getPhrase().get(0).getPosition()));
					
					// check intra-np tokens for negation
					for(GenericToken token : np.getPhrase()) {
						if(words.get(token.getPosition()).isNegationSignal()) {
							np.setNegated(true);
							break;
						}
					}
					
					metadata.addNounMetadata(np);
				}
		
				
				/* DEPENDENT phrases */
				if(word.isDependentPhraseBegin()) {
					DependentPhraseMetadata dp = new DependentPhraseMetadata(word.getDependentPhraseBegin());

					// dependent phrase
					dp.setPhrase(buildDependentPhrase(words, i));

					metadata.addDependentMetadata(dp);
				}
				
				i++;
				
			} catch(Exception e) { 
				e.printStackTrace(); 
			}
		}
		
		// #####
		// loop through word tokens to build phrases complete
		// now loop through phrases built in previous loop and construct additional metadata
		// #####
		
		for(NounPhraseMetadata phrase : metadata.getNounMetadata()) {
			int finalTokenPos = 0;

			for(GenericToken tp : phrase.getPhrase()) {
				// contains comma
				if(tp.getToken().equals(",")) {
					metadata.addSimpleMetadataValue("NPContainsComma", true);
				}
				finalTokenPos = tp.getPosition();

			}
			phrase.setPrepPhrasesIdx(getModifyingPrepPhrases(finalTokenPos, metadata.getPrepMetadata()));
			// query lexicon for noun phrase semantic type
			//String st = Constants.semanticTypes.get(phrase.getNounPhraseString().trim().toLowerCase());
			
			String st;// = jedis.get("st:"+phrase.getNounPhraseString().trim().toLowerCase());

			try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
				//list = jedis.hmget("ct:"+key, "attr", "qualifier");
				st = jedis.get("st:"+phrase.getNounPhraseString().trim().toLowerCase());
			}
			
			phrase.setSemanticType(st);
		}
		
		for(int j=0; j < metadata.getPrepMetadata().size(); j++) {
			PrepPhraseMetadata phrase = metadata.getPrepMetadata().get(j);
			
			if(j > 0) {
				// if prep phrase is negated are all grammatically-linked to it also negated?
				PrepPhraseMetadata prevPhrase = metadata.getPrepMetadata().get(j-1);
				
				if(prevPhrase.isNegated()) {
					int prevPhraseEndIdx = prevPhrase.getPhrase().get(prevPhrase.getPhrase().size()-1).getPosition();
					if(phrase.getPhrase().get(0).getPosition() - prevPhraseEndIdx == 1) {
						phrase.setNegated(true);
					}
				}
			}
			
			for(PrepPhraseToken ppt : phrase.getPhrase()) {
				ppt.setNounPhraseIdx(getContainingNounPhraseIdx(ppt.getPosition(), metadata.getNounMetadata()));
			}
		}
		
		Set<Integer> subjectIndexes = new HashSet<>();
		
		for(VerbPhraseMetadata phrase : metadata.getVerbMetadata()) {
			if(phrase.getVerbClass() == Constants.VerbClass.ACTION ||
				phrase.getVerbClass() == Constants.VerbClass.LINKING_VERB ||
				phrase.getVerbClass() == Constants.VerbClass.VERB_OF_BEING || 
				phrase.getVerbClass() == Constants.VerbClass.MODAL_AUX) {
				
				boolean oneVerbNegated = false;
				
				for(VerbPhraseToken verb : phrase.getVerbs()) {
					verb.setNegated(checkNegation(words, verb.getPosition()));
					verb.setPrepPhrasesIdx(getModifyingPrepPhrases(verb.getPosition(), metadata.getPrepMetadata()));
					verb.setDepPhraseIdx(getContainingDependentPhraseIdx(verb.getPosition(), metadata.getDependentMetadata()));
					
					for(int j=verb.getPosition()-1; j>=0; j--) {
						if(words.get(j).isAdjectivePOS() || words.get(j).isAdverbPOS()) {
							verb.getModifierList().add(j);
						} else
							break;
					}
					
					if(verb.isNegated())
						oneVerbNegated = true;
				}
				
				// if verb consists of more than one token, query lexicon for semantic type
				if(phrase.getVerbs().size() > 1) {
					//phrase.setSemanticType(Constants.semanticTypes.get(phrase.getVerbString()));
					try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
						phrase.setSemanticType(jedis.get("st:"+phrase.getVerbString().toLowerCase()));
					}
					
					// if one verb of the phrase is negated, set all to negated
					if(oneVerbNegated) {
						for(VerbPhraseToken verb : phrase.getVerbs()) {
							verb.setNegated(true);
						}
					}
				}
				
				// TODO remove when compound subjects fully implemented
				if(phrase.getSubj() != null) {
					int subjPos = phrase.getSubj().getPosition();
					phrase.getSubj().setPrepPhrasesIdx(getModifyingPrepPhrases(subjPos, metadata.getPrepMetadata()));
					phrase.getSubj().setNounPhraseIdx(getContainingNounPhraseIdx(subjPos, metadata.getNounMetadata()));
					phrase.getSubj().setNegated(checkSubjNegation(words, subjPos));
					phrase.getSubj().setDepPhraseIdx(getContainingDependentPhraseIdx(subjPos, metadata.getDependentMetadata()));
					
					for(int j=phrase.getSubj().getPosition()-1; j>=0; j--) {
						if(words.get(j).isAdjectivePOS() || words.get(j).isAdverbPOS()) {
							phrase.getSubj().getModifierList().add(j);
						} else
							break;
					}
				}
				
				for(VerbPhraseToken token : phrase.getSubjects()) {
					token.setPrepPhrasesIdx(getModifyingPrepPhrases(token.getPosition(), metadata.getPrepMetadata()));
					token.setNounPhraseIdx(getContainingNounPhraseIdx(token.getPosition(), metadata.getNounMetadata()));
					token.setNegated(checkSubjNegation(words, token.getPosition()));
					token.setDepPhraseIdx(getContainingDependentPhraseIdx(token.getPosition(), metadata.getDependentMetadata()));
					
					subjectIndexes.add(token.getPosition());
					
					for(int j=token.getPosition()-1; j>=0; j--) {
						if(words.get(j).isAdjectivePOS() || words.get(j).isAdverbPOS()) {
							token.getModifierList().add(j);
						} else
							break;
					}
				}
				
				if(!phrase.getSubjC().isEmpty()) {
					for(VerbPhraseToken token : phrase.getSubjC()) {
						if(subjectIndexes.contains(token.getPosition()))
							metadata.addSimpleMetadataValue("subjSubjCEqual", true);
						
						token.setPrepPhrasesIdx(getModifyingPrepPhrases(token.getPosition(), metadata.getPrepMetadata()));
						token.setNounPhraseIdx(getContainingNounPhraseIdx(token.getPosition(), metadata.getNounMetadata()));
						token.setNegated(checkSubjCNegation(words, token.getPosition()));
						// ScottD - 3/29/16 - attempting to set depPhraseIdx of metadata properly
						//token.setDepPhraseIdx(findModifyingDependentPhrase(words, token.getPosition(), metadata.getDependentMetadata()));
						token.setDepPhraseIdx(getContainingDependentPhraseIdx(token.getPosition(), metadata.getDependentMetadata()));
						
						for(int j=token.getPosition()-1; j>=0; j--) {
							if(words.get(j).isAdjectivePOS() || words.get(j).isAdverbPOS()) {
								token.getModifierList().add(j);
							} else
								break;
						}
					}
				}

			} else if(phrase.getVerbClass() == Constants.VerbClass.PREPOSITIONAL) {
				//phrase.getVerb().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getVerb().getPosition(), metadata.getPrepMetadata()));
				for(VerbPhraseToken verb : phrase.getVerbs()) {
					verb.setPrepPhrasesIdx(getModifyingPrepPhrases(verb.getPosition(), metadata.getPrepMetadata()));
				}
			}
		}
		
		for(DependentPhraseMetadata phrase : metadata.getDependentMetadata()) {
			phrase.setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition(), metadata.getPrepMetadata()));
		}
		
		// determine orphaned tokens (those which do not fall within a known phrase type [with restrictions])
		buildOrphanList(words, metadata);
		
		buildModByPPList(words, metadata);
	}
	
	private void buildOrphanList(ArrayList<WordToken> words, SentenceMetadata metadata) {
		Set<Integer> metadataIndexes = new HashSet<>(); // indexes of tokens contained within known phrases
        Set<Integer> orphanedIndexes = new HashSet<>(); // indexes of tokens outside of phrases
        
        // add all token indexes within known phrase types to metadataIndexes
        for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
            if(vpm.getSubj() != null) {
            	metadataIndexes.add(vpm.getSubj().getPosition());
            	for(Integer mod : vpm.getSubj().getModifierList()) {
            		metadataIndexes.add(mod);
            	}
            }
            
//            if(vpm.getSubjects() != null) { 
//				for(VerbPhraseToken vpt : vpm.getSubjects()) {
//					metadataIndexes.add(vpt.getPosition());
//					for(Integer mod : vpt.getModifierList())
//						metadataIndexes.add(mod);
//				}
//			}
            
            for(VerbPhraseToken vpt : vpm.getVerbs()) {
				metadataIndexes.add(vpt.getPosition());
				try { // remove this try once data has been re-annotated
					for(Integer mod : vpt.getModifierList())
						metadataIndexes.add(mod);
				} catch(Exception e) { }
			}
			
			if(vpm.getSubjC() != null) { 
				for(VerbPhraseToken vpt : vpm.getSubjC()) {
					metadataIndexes.add(vpt.getPosition());
					try { // remove this try once data has been re-annotated
						for(Integer mod : vpt.getModifierList())
							metadataIndexes.add(mod);
					} catch(Exception e) { }
				}
			}
        }
        for(NounPhraseMetadata npm : metadata.getNounMetadata()) {
            for(GenericToken g : npm.getPhrase()) {
                metadataIndexes.add(g.getPosition());
            }
        }
        for(PrepPhraseMetadata ppm : metadata.getPrepMetadata()) {
            for(GenericToken g : ppm.getPhrase()) {
                metadataIndexes.add(g.getPosition());
            }
        }
        //for(DependentPhraseMetadata dpm : metadata.getDependentMetadata()) {
        //    for(GenericToken g : dpm.getPhrase()) {
        //        metadataIndexes.add(g.getPosition());
        //    }
        //}
        
        // build a Set that represents orphaned tokens not accounted for within metadata
		for(int i = 0; i < words.size(); i++) {
			WordToken word = words.get(i);
			if(!metadataIndexes.contains(i))
				// added more restriction to make it match the grammatical patterns report
				if(!(word.isPunctuation() || word.isDeterminerPOS() || word.isConjunctionPOS() || word.isNegationSignal()
				  || word.isDependentPhraseBegin())) {
					orphanedIndexes.add(i);
				}
		}
		
		for(Integer index : orphanedIndexes) {
			metadata.addOrphan(new OrphanMetadata(new GenericToken(words.get(index).getToken(), index)));
		}
        
	}
	
	private void buildModByPPList(ArrayList<WordToken> words, SentenceMetadata metadata) {        
        // 
        for(VerbPhraseMetadata vpm : metadata.getVerbMetadata()) {
            if(vpm.getSubj() != null) {
            	int i = 0;
            	for(Integer ppIdx : vpm.getSubj().getPrepPhrasesIdx()) {
            		if(i == 0)
            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.SUBJ));
            		else
            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.PP));
            		i++;
            	}
            }
            
//            if(vpm.getSubjects() != null) { 
//				for(VerbPhraseToken vpt : vpm.getSubjects()) {
//					metadataIndexes.add(vpt.getPosition());
//					for(Integer mod : vpt.getModifierList())
//						metadataIndexes.add(mod);
//				}
//			}
            
            for(VerbPhraseToken vpt : vpm.getVerbs()) {
            	int i = 0;
            	for(Integer ppIdx : vpt.getPrepPhrasesIdx()) {
            		if(i == 0)
            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.VB));
            		else
            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.PP));
            		i++;
            	}
			}
			
			if(vpm.getSubjC() != null) { 
				for(VerbPhraseToken vpt : vpm.getSubjC()) {
					int i = 0;
	            	for(Integer ppIdx : vpt.getPrepPhrasesIdx()) {
	            		if(i == 0)
	            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.SUBJC));
	            		else
	            			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.PP));
	            		i++;
	            	}
				}
			}
        }
        for(NounPhraseMetadata npm : metadata.getNounMetadata()) {
        	int i = 0;
        	for(Integer ppIdx : npm.getPrepPhrasesIdx()) {
        		if(i == 0)
        			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.NP));
        		else
        			metadata.addModByPPMetadata(new ModByPPMetadata(ppIdx, Constants.ModByPPClass.PP));
        		i++;
        	}
        }
        
        //for(DependentPhraseMetadata dpm : metadata.getDependentMetadata()) {
        //    for(GenericToken g : dpm.getPhrase()) {
        //        metadataIndexes.add(g.getPosition());
        //    }
        //}
	}
	
	private int findModifyingDependentPhrase(ArrayList<WordToken> words, int subjcPos, List<DependentPhraseMetadata> dpMetadata) {
		int depPhrasePos = -1;
		
		// TODO kill this with fire and make it not suck
		
		int startPos = subjcPos;
		
		if(words.get(startPos).isDependentPhraseMember()) {
			do {
				startPos++;
			} while(words.get(startPos).isDependentPhraseMember());
		}
		
		// loop forward from subj complement position until a dependent phrase member is found
		for(int i=startPos; i < words.size(); i++) {
			if(words.get(i).isDependentPhraseMember()) {
				int phraseStartIdx = i;
				int phraseEndIdx = -1;
				int dpIdx = 0;
				// find the dependent phrase object in the list
				for(DependentPhraseMetadata phrase : dpMetadata) {
					if(phrase.getPhrase().get(0).getPosition() == phraseStartIdx) {
						phraseEndIdx = phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition();
						break;
					}
					dpIdx++;
				}
				
				boolean vobSubj = false, vobSubjC = false, vob = false;
				boolean lvSubj = false, lvSubjC = false, lv = false;
				boolean avSubj = false, avSubjC = false, av = false;
				
				for(int j=phraseStartIdx; j <= phraseEndIdx; j++) {
					if(words.get(j).isActionVerb())
						av = true;
					if(words.get(j).isActionVerbSubject())
						avSubj = true;
					if(words.get(j).isActionVerbDirectObject())
						avSubjC = true;
					
					if(words.get(j).isLinkingVerb())
						lv = true;
					if(words.get(j).isLinkingVerbSubject())
						lvSubj = true;
					if(words.get(j).isLinkingVerbSubjectComplement())
						lvSubjC = true;
					
					if(words.get(j).isVerbOfBeing())
						vob = true;
					if(words.get(j).isVerbOfBeingSubject())
						vobSubj = true;
					if(words.get(j).isVerbOfBeingSubjectComplement())
						vobSubjC = true;
				}
				
				// the idea is to only process if a complete verb structure is present within a DP
				// each type of verb structure has to be tracked independently because, for example, you could have a verb/obj from an AV and a subj from an LV
				// in the same sentence. this still isn't perfect because there could be two of the same class of verb phrase in the same damn DP.
				
				// do something if none of the verb phrases are complete.
				if(!((vobSubj && vobSubjC && vob) || (lvSubj && lvSubjC && lv) || (avSubj && avSubjC && av))) {
					depPhrasePos = dpIdx;
				}
				
				break;
			}
		}
		
		return depPhrasePos;
	}
	
	private boolean checkNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {	
			// loop backward from tokenPos
			for(int i=tokenPos-1; i >= 0; i--) {
				WordToken word = words.get(i);

				if(words.get(i).isNegationSignal()) { // flipped this logic to allow "fail" to be detected as negation signal
					ret = true;
					break;
				} else if(word.isVerb() || word.isPrepPhraseObject()) { // break on a verb or prep phrase 
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkSubjCNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backward from subject complement to verb
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isNegationSignal()) {
					ret = true;
					break;
				} else if(words.get(i).isVerbOfBeing() || words.get(i).isLinkingVerb()) {
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
				if(words.get(i).isNegationSignal()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean containsPattern(Pattern pattern, String fullSentence) {
		Matcher matcher = pattern.matcher(fullSentence);
		return matcher.find();
	}

	private List<GenericToken> buildNounPhrase(ArrayList<WordToken> words, int startPos) {
		List<GenericToken> tposList = new ArrayList<GenericToken>();
		
		// add initial noun phrase HEAD
		tposList.add(new GenericToken(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos-1; i >= 0; i--) {
			if(words.get(i).isNounPhraseModifier()) {
				tposList.add(new GenericToken(words.get(i).getToken(), i));
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
	
	private List<PrepPhraseToken> buildPrepPhrase(ArrayList<WordToken> words, int startPos) {
		List<PrepPhraseToken> list = new ArrayList<PrepPhraseToken>();
		
		for(int i=startPos; i >= 0; i--) {
			list.add(new PrepPhraseToken(words.get(i).getToken(), i, words.get(i).isPrepPhraseObject()));
			
			if(words.get(i).isPrepPhraseBegin())
				break;
		}
		
		if(list.isEmpty())
			return null;
		else
			return Lists.reverse(list);
	}
	
	private List<GenericToken> buildDependentPhrase(ArrayList<WordToken> words, int startPos) {
		List<GenericToken> tposList = new ArrayList<GenericToken>();
		
		for(int i=startPos; i < words.size(); i++) {
			tposList.add(new GenericToken(words.get(i).getToken(), i));
			
			if(words.get(i).isDependentPhraseEnd())
				break;
		}
		
		if(tposList.isEmpty())
			tposList = null;
		
		return tposList;
	}
	
	private List<Integer> getModifyingPrepPhrases(int sourcePos, List<PrepPhraseMetadata> prepList) {
		List<Integer> modList = new ArrayList<Integer>();
		
		// loop through each prep phrase and add it to a list if it modifies the token before it
		for(int i=0; i < prepList.size(); i++) {
			PrepPhraseMetadata phrase = prepList.get(i);
			if(phrase.getPhrase().get(0).getPosition() == sourcePos+1) {
				modList.add(i);
				sourcePos = phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition();
			}
		}
		
		return modList;
	}
	
	private int getContainingNounPhraseIdx(int sourcePos, List<NounPhraseMetadata> nounList) {
		int idx = -1;
		
		for(int i=0; i < nounList.size(); i++) {
			NounPhraseMetadata phrase = nounList.get(i);
			for(GenericToken token : phrase.getPhrase()) {
				if(token.getPosition() == sourcePos) {
					idx = i;
					break;
				}
			}
			if(idx != -1)
				break;
		}
		
		return idx;
	}
	
	private int getContainingDependentPhraseIdx(int sourcePos, List<DependentPhraseMetadata> dpMetadata) {
		int idx = -1;
		
		for(int i=0; i < dpMetadata.size(); i++) {
			DependentPhraseMetadata phrase = dpMetadata.get(i);
			for(GenericToken token : phrase.getPhrase()) {
				if(token.getPosition() == sourcePos) {
					idx = i;
					break;
				}
			}
			if(idx != -1)
				break;
		}
		
		return idx;
	}
}
