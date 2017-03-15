package com.mst.util.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.tools.SemanticTypeSentenceAnnotatorImpl;
import com.mst.tools.Tokenizer;
import static org.junit.Assert.*;

public class SemanticTypeAnnotatorTest {


	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\sentencesST.txt";
	}
	
	
	@Test
	public void foo(){
		SemanticTypeSentenceAnnotatorImpl annotator = new SemanticTypeSentenceAnnotatorImpl();
		Tokenizer tokenizer = new Tokenizer();
		SemanticTypeHardCodedProvider provider = new SemanticTypeHardCodedProvider();
		
		
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		List<Sentence> originalSentences = TestDataProvider.getSentence(fileText);
		
		int i = 1;
		for(Sentence s: originalSentences){
			List<WordToken> tokens = tokenizer.splitWords(s.getOrigSentence());
			List<WordToken> modifiedTokens = annotator.annotate(tokens, provider.getSemanticTypes());
			assertTokens(modifiedTokens, i);
			i+=1;
		}
	}
	
	private void assertTokens(List<WordToken> modifiedTokens, int i){
	
		System.out.println(i);
		if(i==8)
		{
		 String s = "";	
		 s.split("");		 
		}
		
		Map<String,String> expected = getExpectedResults();
		for(WordToken wordToken : modifiedTokens){
			if(expected.containsKey(wordToken.getToken()))
			{
				assertEquals(expected.get(wordToken.getToken()), wordToken.getSemanticType());
			}
			else 
			{
				assertEquals("", wordToken.getSemanticType());
			}
		}
		
	}
	
	
	private Map<String,String> getExpectedResults(){
		Map<String,String> result = new HashMap<String,String>();
		result.put("ovarian", "bpoc");
		result.put("cyst", "dysn");
		result.put("abdominal", "bpoc");
		result.put("mass", "dysn");
		result.put("uterus", "bpoc");
		result.put("ovary", "bpoc");
		result.put("lesion", "dysn");
		result.put("ovary", "bpoc");
		result.put("pap smear", "proc");
		result.put("mammogram", "proc");
		result.put("lcis", "dysn");
		result.put("excisional biopsy", "proc");
		result.put("cancer", "dysn");
		result.put("brca", "gene");
		result.put("lcis", "dysn");
		result.put("excisional biopsy", "proc");
		result.put("cancer", "dysn");
		result.put("brca", "gene");
		
		result.put("ultrasound guided biopsy", "proc");
		result.put("bone marrow biopsy", "proc");
		result.put("biopsy", "proc");
		return result;
	}
	
	
}
