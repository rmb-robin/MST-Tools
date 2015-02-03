package com.mst.model;

import java.util.ArrayList;

import com.mst.util.Constants;
import com.mst.util.Constants.DependentPhraseClass;

public class WordToken extends GenericToken {
		
	// the intent of the abbreviated member var names is to cut down on extraneous bytes in the JSON that gets
	// passed around all over the place in the camel processes
	
	private String pos = "";  // part of speech
	private String normalizedForm = null;
	private ArrayList<SemanticType> semanticTypeList = new ArrayList<SemanticType>();
	private boolean npHead; // noun phrase head
	private boolean npMod; // noun phrase modifier
	private boolean ppMember; // prep phrase member
	private boolean ppObj; // prep phrase object
	private boolean infHead; // infinitive verb head
	private boolean inf; // infinitive verb
	private boolean vob; // verb of being head
	//private boolean vobM; // verb of being member (will possibly deprecate)
	private boolean vobSubj; // verb of being subject
	private boolean vobSubjC; // verb of being subject complement (aka Object)
	private boolean lvSubj; // linking verb subject
	private boolean lv; // linking verb 
	private boolean lvSubjC; // linking verb subject complement (aka Object)
	private boolean av; // action verb
	private boolean avSubj; // action verb subject
	private boolean avObj; // action verb object
	private boolean prepVerb; // prepositional verb
	private boolean modAuxVerb; // modal auxiliary
	private boolean modAuxTerm;
	private DependentPhraseClass dpHead; // dependent phrase head
	private boolean dpMember; // dependent phrase member
	private boolean coref; // co-reference
	private boolean conjAdv; // conjunctive adverb 
	//private ArrayList<Integer> relations = new ArrayList<Integer>();
	
	public WordToken() {
		super();
	}
	
	public WordToken(String word, String normalizedForm, int position) {
		super(word, position);
		this.setNormalizedForm(normalizedForm);
	}
	
	public boolean isVerb() {	
		return this.pos.startsWith("VB");
	}
	
	public boolean isPreposition() {	
		return this.pos.matches("IN|TO");
	}
	
	public boolean isArticle() {
		return this.getToken().matches(Constants.ARTICLE);
	}
	
	public boolean matchesVerbSubjectExclusion() {
		return this.getToken().matches(Constants.VERB_SUBJ_SUBJC_EXCLUSIONS);
	}
	
	public boolean isPronoun() {	
		return this.pos.matches("PRP|PRP\\$");
	}
	
	public boolean isModalAuxPOS() {	
		return this.pos.matches("MD");
	}
	
	public boolean isAdjective() {	
		return this.pos.matches("JJ|JJR|JJS");
	}
	
	public boolean isNoun() {	
		return this.pos.startsWith("NN");
	}
	
	public boolean isAdverb() {	
		return this.pos.matches("RB|RBR|RBS");
	}
	
	public boolean isConjunction() {	
		return this.pos.equalsIgnoreCase("CC");
	}
	
	public boolean isNegationToken() {	
		return this.getToken().matches(Constants.NEGATION);
	}
	
	public boolean matchesModalAuxVerb() {	
		return this.getToken().matches(Constants.MODAL_AUX_VERB);
	}

	public boolean isModalAuxVerb() {
		return modAuxVerb;
	}

	public void setModalAuxVerb(boolean val) {
		this.modAuxVerb = val;
	}

	public boolean isCorefernece() {
		return coref;
	}

	public void setCoreference(boolean val) {
		this.coref = val;
	}
	
	public boolean isConjunctiveAdverb() {
		return conjAdv;
	}

	public void setConjunctiveAdverb(boolean val) {
		this.conjAdv = val;
	}
	
	public boolean isModalAuxTerm() {
		return modAuxTerm;
	}

	public void setModalAuxTerm(boolean val) {
		this.modAuxTerm = val;
	}
	
//	public boolean addRelation(int relationIndex) {
//		return relations.add(relationIndex);
//	}
	
	public boolean isInfinitiveVerb() {
		return inf;
	}

	public void setInfinitiveVerb(boolean val) {
		this.inf = val;
	}

	public boolean isPrepositionalVerb() {
		return prepVerb;
	}

	public void setPrepositionalVerb(boolean val) {
		this.prepVerb = val;
	}
	
	public boolean isVerbOfBeing() {
		return vob;
	}

	public void setVerbOfBeing(boolean val) {
		this.vob = val;
	}

//	public void setVerbOfBeingMember(boolean val) {
//		this.vobM = val;
//	}
	
	public boolean isVerbOfBeingSubject() {
		return vobSubj;
	}

	public void setVerbOfBeingSubject(boolean val) {
		this.vobSubj = val;
	}

	public boolean isVerbOfBeingSubjectComplement() {
		return vobSubjC;
	}

	public void setVerbOfBeingSubjectComplement(boolean val) {
		this.vobSubjC = val;
	}

	public boolean isLinkingVerbSubject() {
		return lvSubj;
	}

	public void setLinkingVerbSubject(boolean val) {
		this.lvSubj = val;
	}

	public boolean isLinkingVerb() {
		return lv;
	}

	public void setLinkingVerb(boolean val) {
		this.lv = val;
	}

	public boolean isLinkingVerbSubjectComplement() {
		return lvSubjC;
	}

	public void setLinkingVerbSubjectComplement(boolean val) {
		this.lvSubjC = val;
	}

	public boolean isActionVerb() {
		return av;
	}

	public void setActionVerb(boolean val) {
		this.av = val;
	}

	public boolean isActionVerbSubject() {
		return avSubj;
	}

	public void setActionVerbSubject(boolean val) {
		this.avSubj = val;
	}
	
	public boolean isActionVerbDirectObject() {
		return avObj;
	}

	public void setActionVerbDirectObject(boolean val) {
		this.avObj = val;
	}
	
//	public ArrayList<Integer> getRelations() {
//		return relations;
//	}
	
	public boolean containsSemanticType(String search) {
		boolean ret = false;
		for(SemanticType st : this.semanticTypeList) {
			if(st.getToken().equalsIgnoreCase(this.getToken())) {
				if(st.getSemanticType().equalsIgnoreCase(search))
					ret = true;
			}
		}
		return ret;
	}
	
	public boolean isPunctuation() {
		// !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
		return this.getToken().matches(Constants.PUNC);
	}
	
	public boolean matchesVerbOfBeingConstant() {
		return this.getToken().matches(Constants.VERBS_OF_BEING);
	}
	
	public boolean matchesPrepositionConstant() {
		return this.getToken().matches(Constants.PREPOSITIONS);
	}
	
	public boolean matchesConjunctiveAdverbConstant() {
		return this.getToken().matches(Constants.CONJUNCTIVE_ADVERBS);
	}
	
	public String getPOS() {
		return pos;
	}

	public void setPOS(String val) {
		this.pos = val;
	}
	
	public ArrayList<SemanticType> getSemanticTypeList() {
		return this.semanticTypeList;
	}
	
	public void setSemanticTypeList(ArrayList<SemanticType> val) {
		this.semanticTypeList = val;
	}
	
	public void setNounPhraseHead(boolean val) {
		this.npHead = val;
	}
	
	public boolean isNounPhraseHead() {
		return npHead;
	}
	
	public void setDependentPhraseHead(DependentPhraseClass val) {
		this.dpHead = val;
	}
	
	public DependentPhraseClass getDependentPhraseHead() {
		return dpHead;
	}
	
	public void setInfinitiveHead(boolean val) {
		this.infHead = val;
	}
	
	public boolean isInfinitiveHead() {
		return infHead;
	}
	
	public void setNounPhraseModifier(boolean val) {
		this.npMod = val;
	}
	
	public boolean isNounPhraseModifier() {
		return npMod;
	}
	
	public boolean isPrepPhraseMember() {
		return ppMember;
	}

	public void setPrepPhraseMember(boolean val) {
		this.ppMember = val;
	}
	
	public boolean isPrepPhraseObject() {
		return ppObj;
	}

	public void setPrepPhraseObject(boolean val) {
		this.ppObj = val;
	}
	
	public String getNormalizedForm() {
		return normalizedForm;
	}

	public void setNormalizedForm(String normalizedForm) {
		this.normalizedForm = normalizedForm;
	}
}