package com.mst.model;

// created as a structure for json object that will represent a PubMed search request
// used as input for a camel process
public class PubMedRequest {

	private String searchTerm;
	private int minYear;
	private int maxYear;
	private int articlesPerIteration;
	private int articlesTotal;
	private int offset;
    private boolean exactMatch;
    private boolean getFullArticleText;
	
	public PubMedRequest() { }
	
	public PubMedRequest(String searchTerm, int minYear, int maxYear, int articlesPerIteration, int articlesTotal, boolean exactMatch, int offset, boolean getFullArticleText) {
        this.searchTerm = searchTerm;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.articlesPerIteration = articlesPerIteration;
        this.articlesTotal = articlesTotal;
        this.exactMatch = exactMatch;
        this.offset = offset;
        this.getFullArticleText = getFullArticleText;
    }

	public boolean getFullArticleText()
    {
        return getFullArticleText;
    }

    public void setGetFullArticleText(boolean getFullArticleText)
    {
        this.getFullArticleText = getFullArticleText;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public boolean isExactMatch()
    {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch)
    {
        this.exactMatch = exactMatch;
    }
	
	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public int getMinYear() {
		return minYear;
	}

	public void setMinYear(int minYear) {
		this.minYear = minYear;
	}

	public int getMaxYear() {
		return maxYear;
	}

	public void setMaxYear(int maxYear) {
		this.maxYear = maxYear;
	}

	public int getArticlesPerIteration() {
		return articlesPerIteration;
	}

	public void setArticlesPerIteration(int articlesPerIteration) {
		this.articlesPerIteration = articlesPerIteration;
	}

	public int getArticlesTotal() {
		return articlesTotal;
	}

	public void setArticlesTotal(int articlesTotal) {
		this.articlesTotal = articlesTotal;
	}

	
}
