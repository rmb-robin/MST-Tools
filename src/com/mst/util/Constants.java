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
				return "test";
			}
		};
		public String getMongoCollection() {
			return "test";
		}
	}
	
	public enum VerbClass {
		LINKING_VERB, VERB_OF_BEING, INFINITIVE, ACTION, MOD_AUX, PREPOSITIONAL;
	}
	
	public enum DependentPhraseClass {
		BEGINS_SENTENCE, PRECEDED_BY_COMMA, FOLLOWED_BY_VERB, OTHER;
	}
	
	public static final String PUNC = "!|\"|#|\\$|%|&|'|\\(|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
	
	public static final String VERBS_OF_BEING = "(?i)am|are|is|was|were|be|being|been";
	
	public static final String NEGATION = "(?i)no|not|without|none";
	
	public static final String ARTICLE = "(?i)a|an|the";
	
	public static final String MODAL_AUX_VERB = "(?i)can|could|may|must|should|will|would";
	
	public static final String VERB_SUBJ_SUBJC_EXCLUSIONS = "(?i)a|an|also|and|another|as|be|but|could|elsewhere|has|may|otherwise|seen|left|right|to|which|would|mm|cm";

	public static final String PREPOSITIONS = "(?i)after|although|among|as|at|before|between|by|during|for|from|in|of|on|over|per|than|that|to|while|with|within|without";
	
	public static final String DEPENDENT_SIGNALS = "(?i)that|when|where|which|who|whom|whose|otherwise|after|also|although|and|as|because|before|certainly|consequently|finally|" +
						"first|furthermore|however|if|indeed|later|meanwhile|moreover|nevertheless|or|overall|provided|second|since|so|than|then|therefore|though|though|" +
						"thus|unless|until|whence|whenever|whereas|wherever|whether|while|whither|how|what|whatever|whichever|why|whoever|whomever|whosoever|whomsoever";

	public static final String CONJUNCTIVE_ADVERBS = "(?i)however|furthermore|moreover|nevertheless|therefore"; // left out "in contrast"

	public static final String INTERSECTION_PREPOSITIONS_AND_DEPENDENT = "(?i)after|as|before|until|than|since";
}
