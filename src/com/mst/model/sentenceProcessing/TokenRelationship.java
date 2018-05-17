package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;

import com.mst.model.metadataTypes.PartOfSpeachTypes;

public class TokenRelationship {
    private String edgeName;
    private String descriptor;
    private String frameName;
    private DateTime createdTime;
    private String uniqueIdentifier;
    private List<String> links;
    private String namedEdge;
    private String source;
    private WordToken toToken;
    private WordToken fromToken;

    public TokenRelationship() {
        links = new ArrayList<>();
    }

    public String getEdgeName() {
        if (edgeName == null)
            return "";
        return edgeName;
    }

    public void setEdgeName(String edgeName) {
        this.edgeName = edgeName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getFrameName() {
        return frameName;
    }

    public void setFrameName(String frameName) {
        this.frameName = frameName;
    }

    public WordToken getToToken() {
        return toToken;
    }

    public void setToToken(WordToken toToken) {
        this.toToken = toToken;
    }

    public WordToken getFromToken() {
        return fromToken;
    }

    public void setFromToken(WordToken fromToken) {
        this.fromToken = fromToken;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isToFromWordTokenMatch(WordToken wordToken) {
        if (this.fromToken != null) {
            if (this.fromToken.equals(wordToken)) return true;
        }
        if (this.toToken != null) {
            return this.toToken.equals(wordToken);
        }
        return false;
    }

    public boolean isToFromTokenMatch(String token) {
        if (this.fromToken != null)
            if (fromToken.getToken().equals(token)) return true;
        if (this.toToken != null)
            return toToken.getToken().equals(token);
        return false;
    }

    public boolean isToFromTokenSetMatch(HashSet<String> tokens) {
        if (this.fromToken != null)
            if (tokens.contains(fromToken.getToken())) return true;
        if (this.toToken != null)
            return tokens.contains(toToken.getToken());
        return false;
    }

    public String getOppositeToken(String token) {
        if (this.getToToken().getToken().equals(token)) return this.getFromToken().getToken();
        return this.getToToken().getToken();
    }

    public String getFromTokenToTokenString() {
        return this.fromToken.getToken() + this.toToken.getToken();
    }

    public String getFromTokenToTokenStringWithSpace() {
        return this.fromToken.getToken() + " " + this.toToken.getToken();
    }

    public boolean isNegationEdge() {
        if (this.fromToken != null && this.fromToken.getPos() != null) {
            if (this.fromToken.getPos().equals(PartOfSpeachTypes.NEG))
                return true;
        }
        if (this.toToken != null && this.toToken.getPos() != null) {
            return this.toToken.getPos().equals(PartOfSpeachTypes.NEG);
        }
        return false;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getNamedEdge() {
        return namedEdge;
    }

    public void setNamedEdge(String namedEdge) {
        this.namedEdge = namedEdge;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
