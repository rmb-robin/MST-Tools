package com.mst.util;

public class Constants {
	public enum Source {
		PUBMED {
			@Override
			public String getMongoCollection() {
				return "processed_pubmed";
			}
		}, 
		CT_SCAN {
			@Override
			public String getMongoCollection() {
				return "processed_imaging";
			}
		}, 
		UNKNOWN {
			@Override
			public String getMongoCollection() {
				return "processed_pubmed";
			}
		};
		public String getMongoCollection() {
			return "processed_pubmed";
		}
	}
	
	public enum VerbClass {
		LINKING_VERB, VERB_OF_BEING, INFINITIVE, ACTION;
	}
	
	public static final String PUNC = "!|\"|#|\\$|%|&|'|\\(|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
	
	public static final String VERBS_OF_BEING = "(?i)am|are|is|was|were|be|being|been";
	
	public static final String NEGATION = "(?i)no|not|without";
	
	public static final String ARTICLE = "(?i)a|an|the";
	
	public static final String MODAL_AUX_VERB = "(?i)can|could|may|must|should|will|would";
	
	public static final String VERB_SUBJ_SUBJC_EXCLUSIONS = "(?i)a|an|also|and|another|as|be|but|could|elsewhere|has|may|otherwise|seen|left|right|to|which|would|mm|cm";
}
