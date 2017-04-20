package com.mst.model.sentenceProcessing;

import java.util.ArrayList;

import com.mst.model.GenericToken;
import com.mst.model.SemanticType;
import com.mst.util.Constants;
import com.mst.util.Constants.DependentPhraseClass;

public class WordToken extends GenericToken {
		
	private String semanticType = null;
	private String pos = null;  // part of speech
	private Verb verb;
	private PropertyValueTypes propertyValueType;
	private boolean isSubjectSetFromWildCard;
	
	
	public WordToken() {
		super();
	}
	
	public WordToken(String word, int position) {
		super(word, position);
	}

	public boolean isNull() {
		return this.getPosition() == 0;
	}
	
	public boolean isVerb() {	
		return verb != null;
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
	
	public boolean isSubjectSetFromWildCard() {
		return isSubjectSetFromWildCard;
	}

	public void setSubjectSetFromWildCard(boolean isSubjectSetFromWildCard) {
		this.isSubjectSetFromWildCard = isSubjectSetFromWildCard;
	}
}