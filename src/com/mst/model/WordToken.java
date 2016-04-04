package com.mst.model;

import java.util.ArrayList;

import com.mst.util.Constants;
import com.mst.util.Constants.DependentPhraseClass;

public class WordToken extends GenericToken {
		
	// the intent of the abbreviated member var names is to cut down on extraneous bytes in the JSON that gets
	// passed around all over the place in the camel processes
	
	private String pos = "";  // part of speech
	private String normalizedForm = null;
	private ArrayList<SemanticType> semanticTypeList = null;
	private String st = null;
	private boolean npHead; // noun phrase head
	private boolean npMod; // noun phrase modifier
	private boolean ppMember; // prep phrase member
	private boolean ppBegin; // prep phrase begin
	private boolean ppObj; // prep phrase object
	private boolean infHead; // infinitive verb head
	private boolean inf; // infinitive verb
	private boolean vob; // verb of being head
	private boolean vobSubj; // verb of being subject
	private boolean vobSubjC; // verb of being subject complement (aka Object)
	private boolean lvSubj; // linking verb subject
	private boolean lv; // linking verb 
	private boolean lvSubjC; // linking verb subject complement (aka Object)
	private boolean av; // action verb
	private boolean avSubj; // action verb subject
	private boolean avObj; // action verb object
	private boolean prepVerb; // prepositional verb
	private boolean mv; // modal auxiliary
	private boolean mvSubj;
	private boolean mvSubjC;
	private DependentPhraseClass dpBegin; // dependent phrase head
	private boolean dpMember; // dependent phrase member
	private boolean dpEnd; // dependent phrase member
	private boolean coref; // co-reference
	private boolean conjAdv; // conjunctive adverb 
	
	public WordToken() {
		super();
	}
	
	public WordToken(String word, String normalizedForm, int position) {
		super(word, position);
		this.setNormalizedForm(normalizedForm);
	}
	/*
	@Override
	public String getToken() {
		if(normalizedForm == null)
			return super.getToken();
		else
			return normalizedForm;
	}
	*/
	public boolean isVerb() {	
		return pos.startsWith("VB");
	}
	
	public boolean isPreposition() {	
		//return pos.matches("IN|TO");
		return Constants.PREPOSITIONS.matcher(pos).matches();
	}
	
	public boolean isPrepositionPOS() {	
		return pos.matches("IN|TO");
	}
	
	public boolean isArticle() {
		//return getToken().matches(Constants.ARTICLE);
		return Constants.ARTICLE.matcher(getToken()).matches();
	}
	
	public boolean matchesVerbSubjectExclusion() {
		//return getToken().matches(Constants.VERB_SUBJ_SUBJC_EXCLUSIONS);
		return Constants.VERB_SUBJ_SUBJC_EXCLUSIONS.matcher(getToken()).matches();
	}
	
	public boolean isSubjectComplement() {
		return vobSubjC || lvSubjC || avObj || mvSubjC;
	}
	
	public boolean isPronounPOS() {	
		return pos.startsWith("PRP");
	}
	
	public boolean isModalAuxPOS() {	
		return pos.equals("MD");
	}
	
	public boolean isAdjectivePOS() {	
		return pos.startsWith("JJ");
	}

	public boolean isDeterminerPOS() {	
		return pos.equals("DT");
	}
	
	public boolean isNounPOS() {	
		return pos.startsWith("NN");
	}
	
	public boolean isProperNoun() {	
		return pos.equals("NNP");
	}
	
	public boolean isAdverbPOS() {	
		return pos.startsWith("RB");
	}

	public boolean isNumericPOS() {	
		return pos.equals("CD");
	}
	
	public boolean isConjunctionPOS() {	
		return pos.equals("CC");
	}
	
	public boolean isNegationSignal() {	
		//return getToken().matches(Constants.NEGATION);
		return Constants.NEGATION.matcher(getToken()).matches();
	}
	
	public boolean isModalAuxSignal() {
		//return getToken().matches(Constants.MODAL_AUX_VERB);
		return Constants.MODAL_AUX_VERB.matcher(getToken()).matches();
	}

	public boolean isModalAuxVerb() {
		return mv;
	}

	public void setModalAuxVerb(boolean val) {
		mv = val;
	}

	public boolean isModalSubject() {
		return mvSubj;
	}

	public void setModalSubject(boolean val) {
		mvSubj = val;
	}
	
	public boolean isModalSubjectComplement() {
		return mvSubjC;
	}

	public void setModalSubjectComplement(boolean val) {
		mvSubjC = val;
	}
	
	public boolean isCorefernece() {
		return coref;
	}

	public void setCoreference(boolean val) {
		coref = val;
	}
	
	public boolean isConjunctiveAdverb() {
		return conjAdv;
	}

	public void setConjunctiveAdverb(boolean val) {
		conjAdv = val;
	}
	
	public boolean isInfinitiveVerb() {
		return inf;
	}

	public void setInfinitiveVerb(boolean val) {
		inf = val;
	}

	public boolean isPrepositionalVerb() {
		return prepVerb;
	}

	public void setPrepositionalVerb(boolean val) {
		prepVerb = val;
	}
	
	public boolean isVerbOfBeing() {
		return vob;
	}

	public void setVerbOfBeing(boolean val) {
		vob = val;
	}

	public boolean isVerbOfBeingSubject() {
		return vobSubj;
	}

	public void setVerbOfBeingSubject(boolean val) {
		vobSubj = val;
	}

	public boolean isVerbOfBeingSubjectComplement() {
		return vobSubjC;
	}

	public void setVerbOfBeingSubjectComplement(boolean val) {
		vobSubjC = val;
	}

	public boolean isLinkingVerbSubject() {
		return lvSubj;
	}

	public void setLinkingVerbSubject(boolean val) {
		lvSubj = val;
	}

	public boolean isLinkingVerb() {
		return lv;
	}

	public void setLinkingVerb(boolean val) {
		lv = val;
	}

	public boolean isLinkingVerbSubjectComplement() {
		return lvSubjC;
	}

	public void setLinkingVerbSubjectComplement(boolean val) {
		lvSubjC = val;
	}

	public boolean isActionVerb() {
		return av;
	}

	public void setActionVerb(boolean val) {
		av = val;
	}

	public boolean isActionVerbSubject() {
		return avSubj;
	}

	public void setActionVerbSubject(boolean val) {
		avSubj = val;
	}
	
	public boolean isActionVerbDirectObject() {
		return avObj;
	}

	public void setActionVerbDirectObject(boolean val) {
		avObj = val;
	}
	
	public boolean containsSemanticType(String regex) {
		boolean ret = false;
		for(SemanticType st : semanticTypeList) {
			if(st.getToken().equalsIgnoreCase(this.getToken())) {
				if(st.getSemanticType().matches(regex)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	public boolean isPunctuation() {
		// !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
		//return getToken().matches(Constants.PUNC);
		return Constants.PUNC.matcher(getToken()).matches();
	}
	
	public boolean isVerbOfBeingSignal() {
		//return getToken().matches(Constants.VERBS_OF_BEING);
		return Constants.VERBS_OF_BEING.matcher(getToken()).matches();
	}
	
	public boolean isLinkingVerbSignal() {
		//return getToken().matches(Constants.LINKING_VERBS);
		return Constants.LINKING_VERBS.matcher(getToken()).matches();
	}
	
	public boolean matchesPrepositionConstant() {
		//return getToken().matches(Constants.PREPOSITIONS);
		return Constants.PREPOSITIONS.matcher(getToken()).matches();
	}
	
	public boolean matchesConjunctiveAdverbConstant() {
		//return getToken().matches(Constants.CONJUNCTIVE_ADVERBS);
		return Constants.CONJUNCTIVE_ADVERBS.matcher(getToken()).matches();
	}
	
	public String getPOS() {
		return pos;
	}

	public void setPOS(String val) {
		pos = val;
	}

	public String getSemanticType() {
		return st;
	}

	public void setSemanticType(String val) {
		st = val;
	}
	
	public ArrayList<SemanticType> getSemanticTypeList() {
		return semanticTypeList;
	}
	
	public void setSemanticTypeList(ArrayList<SemanticType> val) {
		semanticTypeList = val;
	}
	
	public void setNounPhraseHead(boolean val) {
		npHead = val;
	}
	
	public boolean isNounPhraseHead() {
		return npHead;
	}
	
	public void setDependentPhraseBegin(DependentPhraseClass val) {
		dpBegin = val;
	}
	
	public boolean isDependentPhraseBegin() {
		return getDependentPhraseBegin() != null;
	}
	
	public DependentPhraseClass getDependentPhraseBegin() {
		return dpBegin;
	}
	
	public void setDependentPhraseMember(boolean val) {
		dpMember = val;
	}
	
	public boolean isDependentPhraseMember() {
		return dpMember;
	}
	
	public void setDependentPhraseEnd(boolean val) {
		dpEnd = val;
	}
	
	public boolean isDependentPhraseEnd() {
		return dpEnd;
	}
	
	public void setInfinitiveHead(boolean val) {
		infHead = val;
	}
	
	public boolean isInfinitiveHead() {
		return infHead;
	}
	
	public void setNounPhraseModifier(boolean val) {
		npMod = val;
	}
	
	public boolean isNounPhraseModifier() {
		return npMod;
	}
	
	public boolean isPrepPhraseMember() {
		return ppMember;
	}

	public void setPrepPhraseMember(boolean val) {
		ppMember = val;
	}

	public boolean isPrepPhraseBegin() {
		return ppBegin;
	}

	public void setPrepPhraseBegin(boolean val) {
		ppBegin = val;
	}
	
	public boolean isPrepPhraseObject() {
		return ppObj;
	}

	public void setPrepPhraseObject(boolean val) {
		ppObj = val;
	}
	
	public String getNormalizedForm() {
		return normalizedForm;
	}

	public void setNormalizedForm(String val) {
		normalizedForm = val;
	}
}