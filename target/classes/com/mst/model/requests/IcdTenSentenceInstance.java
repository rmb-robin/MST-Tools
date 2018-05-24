package com.mst.model.requests;

import com.mst.model.discrete.DiscreteData;

public class IcdTenSentenceInstance{
    String icdCode, sentence;
    
    public void setIcdCode(String icdCode) {
		this.icdCode = icdCode;
	}
    public void setSentence(String sentence) {
		this.sentence = sentence;
	}
    public String getIcdCode(){
        return icdCode;
    }
    public String getSentence(){
        return sentence;
    }
	@Override
	public String toString() {
		return "IcdTenSentenceInstance [icdCode=" + icdCode + ", sentence=" + sentence + "]";
	}    
    
}
