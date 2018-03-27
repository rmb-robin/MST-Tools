package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.DiscreteData;

public class SentenceQueryResult {

	private String sentence;
	private String sentenceId;
	private DiscreteData discreteData;
	private String reportText;

	private List<SentenceQueryEdgeResult> sentenceQueryEdgeResults;

	public SentenceQueryResult() {
		sentenceQueryEdgeResults = new ArrayList<>();
	}

	public DiscreteData getDiscreteData() {
		return discreteData;
	}

	public String getReportText() {
		return reportText;
	}

	public String getSentence() {
		return sentence;
	}

	public String getSentenceId() {
		return sentenceId;
	}

	public List<SentenceQueryEdgeResult> getSentenceQueryEdgeResults() {
		return sentenceQueryEdgeResults;
	}

	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}

	public void setReportText(String reportText) {
		this.reportText = reportText;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public void setSentenceId(String sentenceId) {
		this.sentenceId = sentenceId;
	}

	public void setSentenceQueryEdgeResults(List<SentenceQueryEdgeResult> sentenceQueryEdgeResults) {
		this.sentenceQueryEdgeResults = sentenceQueryEdgeResults;
	}
	// Only using the sentenceId for array handling.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sentenceQueryEdgeResults == null) ? 0 : sentenceQueryEdgeResults.hashCode());
		return result;
	}

	// Only using the sentenceId for array handling.
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SentenceQueryResult other = (SentenceQueryResult) obj;
		if (getSentenceId() == null) {
			if (other.getSentenceId() != null)
				return false;
		} else if (!getSentenceId().equals(other.getSentenceId()))
			return false;
		return true;
	}

}
