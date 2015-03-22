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
				return "annotations";
			}
		},
		IMAGING {
			@Override
			public String getMongoCollection() {
				return "annotations";
			}
		},
		VISIT_NOTES {
			@Override
			public String getMongoCollection() {
				return "annotations";
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
		LINKING_VERB, VERB_OF_BEING, INFINITIVE, ACTION, MODAL_AUX, PREPOSITIONAL;
	}
	
	public enum DependentPhraseClass {
		BEGINS_SENTENCE, PRECEDED_BY_COMMA, FOLLOWED_BY_VERB, OTHER;
	}
	
	public static final String PUNC = "!|\"|#|\\$|%|&|'|\\(|\\)|\\*|\\+|,|-|\\.|/|:|;|<|=|>|\\?|@|\\[|\\\\|]|\\^|_|`|\\{|\\||}|~";
	
	public static final String VERBS_OF_BEING = "(?i)am|are|is|was|were|be|being|been";
	
	public static final String LINKING_VERBS = "(?i)is|are|be|appear|was|were|remains|feels|felt|seems|being|looks|prove|remained|become|becomes|been|seemed";
	
	public static final String MODAL_AUX_VERB = "(?i)can|could|may|must|should|will|would";
	
	public static final String NEGATION = "(?i)no|not|without|none";
	
	public static final String ARTICLE = "(?i)a|an|the";
	
	//public static final String VERB_SUBJ_SUBJC_EXCLUSIONS = "(?i)a|an|also|and|another|as|be|but|could|elsewhere|has|may|otherwise|seen|left|right|to|which|would|mm|cm";
	public static final String VERB_SUBJ_SUBJC_EXCLUSIONS = "(?i)a|also|an|and|another|as|be|but|cm|could|elsewhere|has|left|may|mm|otherwise|right|seen|to|which|would";
	
	public static final String PREPOSITIONS = "(?i)after|although|among|as|at|before|between|by|during|for|from|in|of|on|over|per|than|that|to|while|with|within|without";
	
	//public static final String DEPENDENT_SIGNALS = "(?i)that|when|where|which|who|whom|whose|otherwise|after|also|although|and|as|because|before|certainly|consequently|finally|" +
	//					"first|furthermore|however|if|indeed|later|meanwhile|moreover|nevertheless|or|overall|provided|second|since|so|than|then|therefore|though|though|" +
	//					"thus|unless|until|whence|whenever|whereas|wherever|whether|while|whither|how|what|whatever|whichever|why|whoever|whomever|whosoever|whomsoever";

	public static final String DEPENDENT_SIGNALS = "(?i)after|also|although|as|because|before|certainly|consequently|finally|first|furthermore|how|however|if|indeed|later|meanwhile|moreover|nevertheless|otherwise|overall|provided|second|since|so|than|that|then|therefore|though|though|thus|unless|until|what|whatever|when|whence|whenever|where|whereas|wherever|whether|which|whichever|while|whither|who|whoever|whom|whomever|whomsoever|whose|whosoever|why";
	
	public static final String CONJUNCTIVE_ADVERBS = "(?i)furthermore|however|moreover|nevertheless|therefore"; // left out "in contrast"

	public static final String INTERSECTION_PREPOSITIONS_AND_DEPENDENT = "(?i)after|as|before|since|than|until"; // since and until are not preps in our list. wtf?
}
