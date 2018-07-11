package com.mst.model;

public class ICDQueryOutput {
	private String sentenceDiscoveryObjectId = null;
	private String documentId = null;
	private String searchedToken = null;
	private String icdCode = null;
	private String normalizedSentence = null;

	public ICDQueryOutput() {
		super();
	}

	public ICDQueryOutput(String sentenceDiscoveryObjectId, String documentId, String searchedToken, String icdCode,
			String normalizedSentence) {
		super();
		this.sentenceDiscoveryObjectId = sentenceDiscoveryObjectId;
		this.documentId = documentId;
		this.searchedToken = searchedToken;
		this.icdCode = icdCode;
		this.normalizedSentence = normalizedSentence;
	}

	public String getSentenceDiscoveryObjectId() {
		return sentenceDiscoveryObjectId;
	}

	public void setSentenceDiscoveryObjectId(String sentenceDiscoveryObjectId) {
		this.sentenceDiscoveryObjectId = sentenceDiscoveryObjectId;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getSearchedToken() {
		return searchedToken;
	}

	public void setSearchedToken(String searchedToken) {
		this.searchedToken = searchedToken;
	}

	public String getIcdCode() {
		return icdCode;
	}

	public void setIcdCode(String icdCode) {
		this.icdCode = icdCode;
	}

	public String getNormalizedSentence() {
		return normalizedSentence;
	}

	public void setNormalizedSentence(String normalizedSentence) {
		this.normalizedSentence = normalizedSentence;
	}

	@Override
	public String toString() {
		return "ICDQueryOutput [sentenceDiscoveryObjectId=" + sentenceDiscoveryObjectId + ", documentId=" + documentId
				+ ", searchedToken=" + searchedToken + ", icdCode=" + icdCode + ", normalizedSentence="
				+ normalizedSentence + "]";
	}
}
