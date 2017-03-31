package com.mst.model;

import java.util.ArrayList;

import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.Verb;
import com.mst.util.Constants;
import com.mst.util.Constants.DependentPhraseClass;

public class WordToken extends GenericToken {
		
	// the intent of the abbreviated member var names is to cut down on extraneous bytes in the JSON that gets
	// passed around all over the place in the camel processes
	
	// 7/27/2016 - All booleans were converted to Booleans because they are, by default, null, and nulls
	// don't get serialized by Gson. The end result is much cleaner JSON because booleans default to false,
	// which DO get serialized.
	
	//reference what can go away....
	//staying
	private String semanticType = null;
	private String pos = null;  // part of speech
	private Verb verb;
	private PropertyValueTypes propertyValueType;
	
	
	//not sure if staying..

	private String normalizedForm = null;
	private ArrayList<SemanticType> semanticTypeList = null;
	
	private Boolean npHead; // noun phrase head
	private Boolean npMod; // noun phrase modifier
	private Boolean ppMember; // prep phrase member
	private Boolean ppBegin; // prep phrase begin
	private Boolean ppObj; // prep phrase object
	private Boolean infHead; // infinitive verb head
	private Boolean inf; // infinitive verb
	private Boolean vob; // verb of being head
	private Boolean vobSubj; // verb of being subject
	private Boolean vobSubjC; // verb of being subject complement (aka Object)
	private Boolean lvSubj; // linking verb subject
	private Boolean lv; // linking verb 
	private Boolean lvSubjC; // linking verb subject complement (aka Object)
	private Boolean av; // action verb
	private Boolean avSubj; // action verb subject
	private Boolean avObj; // action verb object
	private Boolean prepVerb; // prepositional verb
	private Boolean mv; // modal auxiliary
	private Boolean mvSubj;
	private Boolean mvSubjC;
	private DependentPhraseClass dpBegin; // dependent phrase head
	private Boolean dpMember; // dependent phrase member
	private Boolean dpEnd; // dependent phrase member
	private Boolean coref; // co-reference
	private Boolean conjAdv; // conjunctive adverb 
	
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
	public boolean isNull() {
		return this.getPosition() == 0;
	}
	
	public boolean isVerb() {	
		return pos.startsWith("VB");
	}
	
	public boolean isPreposition() {	
		//return pos.matches("IN|TO");
		return Constants.PREPOSITIONS.matcher(pos).matches();
	}
	
	public boolean isToPOS() {	
		return pos.matches("TO");
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
		return vobSubjC != null || lvSubjC != null || avObj != null || mvSubjC != null;
	}
	
	public boolean isVerbPhraseSubject() {
		return vobSubj != null || lvSubj != null || avSubj != null || mvSubj != null;
	}
	
	public boolean isVerbPhraseVerb() {
		return vob != null || lv != null || av != null || mv != null;
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
		return mv != null;
	}

	public void setModalAuxVerb(boolean val) {
		mv = val;
	}

	public boolean isModalSubject() {
		return mvSubj != null;
	}

	public void setModalSubject(boolean val) {
		mvSubj = val;
	}
	
	public boolean isModalSubjectComplement() {
		return mvSubjC != null;
	}

	public void setModalSubjectComplement(boolean val) {
		mvSubjC = val;
	}
	
	public boolean isCorefernece() {
		return coref != null;
	}

	public void setCoreference(boolean val) {
		coref = val;
	}
	
	public boolean isConjunctiveAdverb() {
		return conjAdv != null;
	}

	public void setConjunctiveAdverb(boolean val) {
		conjAdv = val;
	}
	
	public boolean isInfinitiveVerb() {
		return inf != null;
	}

	public void setInfinitiveVerb(boolean val) {
		inf = val;
	}

	public boolean isPrepositionalVerb() {
		return prepVerb != null;
	}

	public void setPrepositionalVerb(boolean val) {
		prepVerb = val;
	}
	
	public boolean isVerbOfBeing() {
		return vob != null;
	}

	public void setVerbOfBeing(boolean val) {
		vob = val;
	}

	public boolean isVerbOfBeingSubject() {
		return vobSubj != null;
	}

	public void setVerbOfBeingSubject(boolean val) {
		vobSubj = val;
	}

	public boolean isVerbOfBeingSubjectComplement() {
		return vobSubjC != null;
	}

	public void setVerbOfBeingSubjectComplement(boolean val) {
		vobSubjC = val;
	}

	public boolean isLinkingVerbSubject() {
		return lvSubj != null;
	}

	public void setLinkingVerbSubject(boolean val) {
		lvSubj = val;
	}

	public boolean isLinkingVerb() {
		return lv != null;
	}

	public void setLinkingVerb(boolean val) {
		lv = val;
	}

	public boolean isLinkingVerbSubjectComplement() {
		return lvSubjC != null;
	}

	public void setLinkingVerbSubjectComplement(boolean val) {
		lvSubjC = val;
	}

	public boolean isActionVerb() {
		return av != null;
	}

	public void setActionVerb(boolean val) {
		av = val;
	}

	public boolean isActionVerbSubject() {
		return avSubj != null;
	}

	public void setActionVerbSubject(boolean val) {
		avSubj = val;
	}
	
	public boolean isActionVerbDirectObject() {
		return avObj != null;
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
	
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getSemanticType() {
		return semanticType;
	}

	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
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
		return npHead != null;
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
		return dpMember != null;
	}
	
	public void setDependentPhraseEnd(boolean val) {
		dpEnd = val;
	}
	
	public boolean isDependentPhraseEnd() {
		return dpEnd != null;
	}
	
	public void setInfinitiveHead(boolean val) {
		infHead = val;
	}
	
	public boolean isInfinitiveHead() {
		return infHead != null;
	}
	
	public void setNounPhraseModifier(boolean val) {
		npMod = val;
	}
	
	public boolean isNounPhraseModifier() {
		return npMod != null;
	}
	
	public boolean isPrepPhraseMember() {
		return ppMember != null;
	}

	public void setPrepPhraseMember(boolean val) {
		ppMember = val;
	}

	public boolean isPrepPhraseBegin() {
		return ppBegin != null;
	}

	public void setPrepPhraseBegin(boolean val) {
		ppBegin = val;
	}
	
	public boolean isPrepPhraseObject() {
		return ppObj != null;
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
	
	public boolean isWithinVerbPhrase() {
		return infHead != null || inf != null || vob != null || vobSubj != null || vobSubjC != null || lvSubj != null || lv != null || lvSubjC != null || av != null || avSubj != null || avObj != null || prepVerb != null || mv != null || mvSubj != null || mvSubjC != null;
	}
	
	public boolean isWithinNounPhrase() {
		return isNounPhraseModifier() || isNounPhraseHead();
	}
	
	public boolean isWithinPrepPhrase() {
		return isPrepPhraseObject() || isPrepPhraseMember() || isPrepPhraseBegin();
	}
	
	@Override
	public String toString() {
		return super.getToken();
	}

	public Verb getVerb() {
		return verb;
	}

	public void setVerb(Verb verb) {
		this.verb = verb;
	}

	public PropertyValueTypes getPropertyValueType() {
		return propertyValueType;
	}

	public void setPropertyValueType(PropertyValueTypes propertyValueType) {
		this.propertyValueType = propertyValueType;
	}
}