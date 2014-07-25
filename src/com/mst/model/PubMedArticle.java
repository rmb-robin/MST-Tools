package com.mst.model;

import java.util.List;

public class PubMedArticle {

	private String PMID;
	private String fullArticleText = null;
	private List<AbstractText> abstractTextList;
    private List<ArticleId> articleIdList;
	
	public List<ArticleId> getArticleIdList() {
		return articleIdList;
	}

	public void setArticleIdList(List<ArticleId> articleIdList) {
		this.articleIdList = articleIdList;
	}

	public String getFullArticleText() {
		return fullArticleText;
	}

	public void setFullArticleText(String fullArticleText) {
		this.fullArticleText = fullArticleText;
	}

	public String getPMID() {
		return PMID;
	}
	
	public void setPMID(String pmid) {
		this.PMID = pmid;
	}
	
	public List<AbstractText> getAbstractTextList() {
		return abstractTextList;
	}
	
	public void setAbstractTextList(List<AbstractText> abstractList) {
		this.abstractTextList = abstractList;
	}
	
	public class AbstractText {
		private String label, nlmCategory, text;
		
		public AbstractText(String label, String nlmCategory, String text) {
			this.label = label;
			this.nlmCategory = nlmCategory;
			this.text = text;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getNlmCategory() {
			return nlmCategory;
		}
		
		public String getText() {
			return text;
		}
		
		public void setText(String text) {
			this.text = text;
		}
	}
	
	public class ArticleId {
		private String idType, text;

		public ArticleId(String idType, String text) {
			this.idType = idType;
			this.text = text;
		}
		
		public String getIdType() {
			return idType;
		}

		public void setIdType(String idType) {
			this.idType = idType;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
}
