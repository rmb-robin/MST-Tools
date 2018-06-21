package com.mst.model.sentenceProcessing;

import java.io.Serializable;

import com.mst.model.GenericToken;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.SemanticTypes;


public class WordToken extends GenericToken implements Serializable {
    private String descriptor = null;
    private String semanticType = null;
    private String pos = null;
    private Verb verb;
    private PropertyValueTypes propertyValueType;
    private boolean isSubjectSetFromWildCard;
    private int tokenRanking;	//variable used to store the token ranks

    public WordToken() {
        super();
    }

    public WordToken(String word, int position) {
        super(word, position);
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public boolean isVerb() {
        return verb != null;
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
        if (propertyValueType == null) return PropertyValueTypes.NA;
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

    public boolean isCardinal() {
        if (this.getSemanticType() == null) return false;
        return this.getSemanticType().equals(SemanticTypes.CARDINAL_NUMBER);
    }
    
    /**
     * getTokenRanking() is used to get the tokenRanking set for the token
     * @return tokenRanking
     */
    public int getTokenRanking() {
        return tokenRanking;
    }
    
    /**
     * setTokenRanking sets the ranks on the tokens after getting it from RecommendationEdgesVerificationProcesser
     * @param tokenRanking
     */
    public void setTokenRanking(int tokenRanking) {
        this.tokenRanking = tokenRanking;
    }
}