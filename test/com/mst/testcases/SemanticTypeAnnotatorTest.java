package com.mst.testcases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.model.Sentence;
import com.mst.model.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.Tokenizer;
import com.mst.testHelpers.NGramsHardCodedProvider;
import com.mst.testHelpers.SemanticTypeHardCodedProvider;
import com.mst.testHelpers.TestDataProvider;

import static org.junit.Assert.*;

public class SemanticTypeAnnotatorTest {


	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\sentencesST.txt";
	}
	
	
	@Test
	public void annotate(){
		SemanticTypeSentenceAnnotatorImpl annotator = new SemanticTypeSentenceAnnotatorImpl();
		SemanticTypeHardCodedProvider provider = new SemanticTypeHardCodedProvider();
		NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
		
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		List<Sentence> originalSentences = TestDataProvider.getSentences(fileText);
		NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
		int i = 1;
		for(Sentence s: originalSentences){
			s = processor.process(s, ngramsProvider.getNGrams());
			List<WordToken> modifiedTokens = annotator.annotate(s.getModifiedWordList(), provider.getSemanticTypes());
			assertTokens(modifiedTokens, i);
			i+=1;
		}
	}
	
	private void assertTokens(List<WordToken> modifiedTokens, int i){	
		Map<String,String> expected = getExpectedResults();
		for(WordToken wordToken : modifiedTokens){
			
			if(expected.containsKey(wordToken.getToken()))
			{
				assertEquals(expected.get(wordToken.getToken()), wordToken.getSemanticType());
			}
			else 
			{
				assertEquals(wordToken.getToken(),null, wordToken.getSemanticType());
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
		result.put("ultrasound-guided-biopsy", "proc");
		result.put("bone-marrow-biopsy", "proc");
		result.put("biopsy", "proc");
		result.put("polyp",	"dysn");
		result.put("one",	"number");
		result.put("mildly",	"qlco");
		result.put("complicated",	"qlco");
		result.put("left",	"laterality");
		result.put("stable",	"qlco");
		result.put("lobe",	"bpoc");
		result.put("hysterectomy",	"bpoc");
		result.put("bilateral",	"laterality");
		result.put("benign",	"neop-stage");
		result.put("stable",	"qlco");
		result.put("mass",	"dysn");
		result.put("cm",	"unit of measure");
		result.put("indeterminate",	"qlco");
		result.put("hemorrhagic",	"qlco");
		result.put("endometrioma",	"dysn");
		result.put("findings",	"dysn");
		result.put("salpingo-oophorectomy",	"proc");
		result.put("appearing",	"appearance");
		result.put("invasive", 	"qlco");
		result.put("1",	"cardinal number");
		result.put("adjuvant",	"qlco");
		result.put("carcinoma",	"dysn");
		result.put("cervical",	"bpoc");
		result.put("curatively",	"qlco");
		result.put("ductal",	"bpoc");
		result.put("Genedx",	"co name");
		result.put("left",	"laterality");
		result.put("malignancy",	"neop-stage");
		result.put("minimal","qlco");
		result.put("non-melanotic",	"dysn");
		result.put("panel",	"proc");
		result.put("previous",	"temporal");
		result.put("prior",	"temporal");
		result.put("risk",	"risk");
		result.put("skin",	"bpoc");
		result.put("small",	"qlco");
		result.put("stage",	"neop-stage");
		result.put("total",	"qlco");
		result.put("very",  "qlco");
		return result;
	}
	
	
}
