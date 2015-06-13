package com.mst.util;

import java.util.ArrayList;
//import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.mst.model.StanfordDependency;
//import com.mst.model.StanfordDependencyInfo;
import com.mst.model.WordToken;

//import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
//import edu.stanford.nlp.trees.GrammaticalStructure;
//import edu.stanford.nlp.trees.GrammaticalStructureFactory;
//import edu.stanford.nlp.trees.PennTreebankLanguagePack;
//import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.trees.TreebankLanguagePack;
//import edu.stanford.nlp.trees.TypedDependency;

public class StanfordNLP {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	//http://nlp.stanford.edu/software/dependencies_manual.pdf
	//private final String STANFORD_SUBJ_REGEX = "nsubj.*|csubj.*";
	//private final String STANFORD_OBJ_REGEX = ".obj";
	
	private MaxentTagger tagger = null;
	
	public StanfordNLP(MaxentTagger tagger) {
		this.tagger = tagger;
		// TODO instantiating the MaxentTagger is an expensive operation. Need to make this more efficient.
		//if(tagger == null)
		//	tagger = new MaxentTagger(Props.getProperty("stanford_maxent_path"));
	}
	
	public ArrayList<WordToken> identifyPartsOfSpeech(ArrayList<WordToken> wordList) {
		
		//ArrayList<WordToken> wordListOut = new ArrayList<WordToken>();
		
		try {
			StringBuilder sb = new StringBuilder();
			
			for(WordToken word : wordList) {
				sb.append(word.getToken()); // was getNormalizedForm()
				sb.append(" ");
			}
			//System.out.println(sb.toString());
			String result = tagger.tagTokenizedString(sb.toString());
			//System.out.println(result);
			String[] tagged = result.split(" ");
			
			for(int i=0; i < wordList.size(); i++) {
				String[] split = tagged[i].split("_");
				//if(split[0].matches(",|\\(|\\)"))
				//	wordList.get(i).setPOS(split[0]);
				//else
				//	if(split[0].matches("(?i)scan"))
						
				//	else
				//wordList.get(i).setPOS(split[split.length-1]);
				wordList.get(i).setPOS(split[1]);
			}
			
		} catch(Exception e) {
			logger.error("StanfordNLP:identifyPartsOfSpeech(): {}", e);
			e.printStackTrace();
		}
		
		return wordList;
	}
	
//	public StanfordDependencyInfo getDependencyTree(String input) {
//		StanfordDependencyInfo depInfo = new StanfordDependencyInfo();
//		ArrayList<StanfordDependency> list = new ArrayList<StanfordDependency>();
//		
//		try {
//			LexicalizedParser lp = LexicalizedParser.loadModel(
//	                "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
//	                "-maxLength", "80", "-retainTmpSubcategories");
//	        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//	        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//	        
//	        Tree parse = lp.parse(input);
//	        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse); 
//	        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed(); 
//	        
//	        for(TypedDependency t : tdl) {
//	        	//https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/ling/CoreLabel.java
//	        	String relation = t.reln().toString();
//	        	StanfordDependency dep = new StanfordDependency(relation, t.gov().toString("value"), t.dep().toString("value"));
//	        	list.add(dep);
//	        	if(relation.matches(STANFORD_OBJ_REGEX)) {
//	        		depInfo.incrementObjCount();
//	        	} else if(relation.matches(STANFORD_SUBJ_REGEX)) {
//	        		depInfo.incrementSubjCount();
//	        	}
//	        }
//	        depInfo.setDependencyList(list);
//	        
//		} catch(Exception e) {
//			logger.error("getDependencyTree(): {}", e);
//		}
//		
//        return depInfo;
//	}
}
