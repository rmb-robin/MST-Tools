package com.mst.tools;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mst.model.SemanticType;
import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.util.Props;

public class MetaMapWrapper {
	private String restrictST = null; // comma-separated list of values ("ftcn,lbpr"). restrict takes precedence over exclude
	private String excludeST = null;
	private List<String> tokensToExclude = new ArrayList<String>();  // these tokens (words) will not be added to the output
	private List<String> preferredNamesToExclude = new ArrayList<String>(); // these preferred names will not be added to the output
	private MetaMapApi api = null;
	private Gson gson = new Gson();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public MetaMapWrapper() {
		String host = "";
        try {
            host = Props.getProperty("metamap_api_host");
            if(host == null || host.equalsIgnoreCase("localhost"))
                api = new MetaMapApiImpl();
            else
                api = new MetaMapApiImpl(host);
            
        } catch(Exception e) {
            logger.warn("Error connecting to Metamap server. Host: {}\n{}", host, e);
        }
	}
	
	public MetaMapWrapper(String server) {
        api = new MetaMapApiImpl(server);
    }
	
	public String processSentence(String json) throws Exception {
		//http://metamap.nlm.nih.gov/Docs/README_javaapi.html
		String ret = null;
		List<String> args = new ArrayList<String>();
	    args.add("-i");  // ignore word order, also --ignore_word_order
	    
	    if(restrictST != null) {
	    	args.add("-J"); // restrict to semantic types, also --restrict_to_sts
	    	args.add(restrictST);
	    } else if(excludeST != null) {
	    	args.add("-k"); // exclude semantic types, also --exclude_sts
	    	args.add(excludeST);
	    }
	    api.setOptions(args);
	    
	    try {
	    	Sentence sentence = gson.fromJson(json, Sentence.class);
		    List<Result> resultList = api.processCitationsFromString(sentence.getFullSentence());
	
		    api.resetOptions(); // MANDATORY otherwise the options remain "stuck" on the mmserver
		    //api.disconnect();
	   
		    if(resultList == null || resultList.size() == 0) {
		    	logger.error("No results from metamap. \n PMID: {} \n Full Sentence: {}", sentence.getArticleId(), sentence.getFullSentence());
                ret = null;
		    } else {
			    //int beginIndex = 0;
			    //int resultCnt=0, utteranceCnt=0, pcmCnt=0, evCnt=0;
			    for(Result result : resultList) {
			    	//resultCnt++;
			    	for(Utterance utterance : result.getUtteranceList()) {
			    		//utteranceCnt++;
			    		for(PCM pcm : utterance.getPCMList()) {
			    			//pcmCnt++;
			    			for(Ev ev : pcm.getCandidateList()) {
			    				//evCnt++;
			    				String token = "";
			    				try {
				    				List<Position> pos = ev.getPositionalInfo();
				    				int begin = pos.get(0).getX();
				    				int end = pos.get(pos.size() - 1).getX() + pos.get(pos.size() - 1).getY();
			
				    				token = sentence.getFullSentence().substring(begin, end);
				    				
				    				if(!preferredNamesToExclude.contains(ev.getPreferredName()) && !tokensToExclude.contains(token)) {
				    					//metaMapList.add(new MetaMapToken(token, ev.getConceptId(), ev.getConceptName(), ev.getPreferredName(), ev.getSemanticTypes(), ev.getSources()));
				    					// loop through all instances of token in wordList
				    					for(int index : findAllWordTokenIndices(sentence.getWordList(), token)) {
				    						// add semantic type, if not already present
				    						SemanticType newST = new SemanticType(ev.getSemanticTypes().get(0), token);
				    						if(!sentence.getWordList().get(index).getSemanticTypeList().contains(newST)) {
				    							sentence.getWordList().get(index).getSemanticTypeList().add(newST);
				    						}
				    					}
				    				}
			    				} catch(Exception e) {
			    					logger.error("Error processing semantic type. \n PMID: {}; Sentence Id: {}; Token: {}", sentence.getArticleId(), sentence.getPosition(), token);
			    				}
			    			}
			    		}
			    	}
			    }
			    //System.out.println("result: " + resultCnt + "; utterance: " + utteranceCnt + "; pcm: " + pcmCnt + "; ev: " + evCnt);
		    }
		    ret = gson.toJson(sentence);
		    
	    } catch(Exception e) {
	    	logger.error("Error processing metamap. JSON: {} \n {}", json, e);
	    }
	    // remove WordList to cut down on json clutter
	    //sentence.setWordList(null);
	    
	    return ret;
	}
	
	// The NIH metamap can return phrases related to the token being searched.	E.g. a token of 'sample' can return 'sample sizes'. 
	// This function splits the string on a space and searches the list of tokens to find a match by the first word of the phrase.
	// The result is that words and phrases are grouped together by the first token.
	private ArrayList<Integer> findAllWordTokenIndices(ArrayList<WordToken> tokens, String token) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		String firstToken = token.split(" ")[0];
		
		for(int i=0; i < tokens.size(); i++) {
			if(tokens.get(i).getToken().equalsIgnoreCase(firstToken))
				indices.add(i);
		}
		
		return indices;
	}
	
	private int findWordToken(ArrayList<WordToken> tokens, String token, int beginIndex) {
		int i = beginIndex;
		
		for(; i < tokens.size(); i++) {
			if(tokens.get(i).getToken().equalsIgnoreCase(token))
				break;
		}
		
		return i;
	}
	
	public String getRestrictST() {
		return restrictST;
	}

	public void setRestrictST(String restrictST) {
		this.restrictST = restrictST;
	}

	public String getExcludeST() {
		return excludeST;
	}

	public void setExcludeST(String excludeST) {
		this.excludeST = excludeST;
	}

	public List<String> getTokensToExclude() {
		return tokensToExclude;
	}

	public void setTokensToExclude(String tokensToExclude) {
		String[] tokens = tokensToExclude.split(",");
		
		this.tokensToExclude.clear();
		for(String token : tokens)
			this.tokensToExclude.add(token.trim());
	}

	public List<String> getPreferredNamesToExclude() {
		return preferredNamesToExclude;
	}

	public void setPreferredNamesToExclude(String preferredNamesToExclude) {
		String[] tokens = preferredNamesToExclude.split(",");
		
		this.preferredNamesToExclude.clear();
		for(String token : tokens)
			this.preferredNamesToExclude.add(token.trim());
	}
}
