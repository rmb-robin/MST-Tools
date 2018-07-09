import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.sentenceProcessing.NGramsModifierEntity;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.SemanticTypeSentenceAnnotatorImpl;
import com.mst.sentenceprocessing.Tokenizer;

import static org.junit.Assert.*;

public class SemanticTypeAnnotatorTest {


	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\sentencesST.txt";
	}
	
	
	//@Test
	public void annotate(){
//		SemanticTypeSentenceAnnotatorImpl annotator = new SemanticTypeSentenceAnnotatorImpl();
//		SemanticTypeHardCodedProvider provider = new SemanticTypeHardCodedProvider();
//		NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
//		
//		String fileText = TestDataProvider.getFileText(getTestDataPath());
//		List<Sentence> originalSentences = TestDataProvider.getSentences(fileText);
//		NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
//		int i = 1;
//		for(Sentence s: originalSentences){
//			s = processor.process(s, ngramsProvider.getNGrams());
//			List<WordToken> modifiedTokens = annotator.annotate(s.getModifiedWordList(), provider.getSemanticTypes());
//			assertTokens(modifiedTokens, i);
//			i+=1;
//		}
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
	
	@Test
	public void createFile() throws Exception{
//		String file =  System.getProperty("user.dir") + File.separator + "testData" + File.separator + "semanticTypes.txt";
//		Path path = Paths.get(file);
//		Map<String,String> stTypes =  new SemanticTypeHardCodedProvider().getSemanticTypes();
//		
//		StringBuilder sb = new StringBuilder();
//		for (Map.Entry<String, String> entry : stTypes.entrySet()) {
//
//			sb.append(entry.getKey() + "," + entry.getValue() + System.lineSeparator());
//		}
//		//Use try-with-resource to get auto-closeable writer instance
//		try (BufferedWriter writer = Files.newBufferedWriter(path)) 
//		{
//		    writer.write(sb.toString());
//		}
	}
	
	private Map<String,String> getExpectedResults(){
		Map<String,String> result = new HashMap<String,String>();
		result.put("ovarian", "bpoc");
		result.put("cyst", "DYSN");
		result.put("abdominal", "bpoc");
		result.put("mass", "DYSN");
		result.put("uterus", "bpoc");
		result.put("ovary", "bpoc");
		result.put("lesion", "DYSN");
		result.put("ovary", "bpoc");
		result.put("pap smear", "proc");
		result.put("mammogram", "proc");
		result.put("lcis", "DYSN");
		result.put("excisional biopsy", "proc");
		result.put("cancer", "DYSN");
		result.put("brca", "gene");
		result.put("lcis", "DYSN");
		result.put("excisional biopsy", "proc");
		result.put("cancer", "DYSN");
		result.put("brca", "gene");
		result.put("ultrasound-guided-biopsy", "proc");
		result.put("bone-marrow-biopsy", "proc");
		result.put("biopsy", "proc");
		result.put("polyp",	"DYSN");
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
		result.put("mass",	"DYSN");
		result.put("cm",	"unit of measure");
		result.put("indeterminate",	"qlco");
		result.put("hemorrhagic",	"qlco");
		result.put("endometrioma",	"DYSN");
		result.put("findings",	"DYSN");
		result.put("salpingo-oophorectomy",	"proc");
		result.put("appearing",	"appearance");
		result.put("invasive", 	"qlco");
		result.put("1",	"cardinal number");
		result.put("adjuvant",	"qlco");
		result.put("carcinoma",	"DYSN");
		result.put("cervical",	"bpoc");
		result.put("curatively",	"qlco");
		result.put("ductal",	"bpoc");
		result.put("Genedx",	"co name");
		result.put("left",	"laterality");
		result.put("malignancy",	"neop-stage");
		result.put("minimal","qlco");
		result.put("non-melanotic",	"DYSN");
		result.put("panel",	"proc");
		result.put("previous",	"temporal");
		result.put("prior",	"temporal");
		result.put("risk",	"risk");
		result.put("skin",	"bpoc");
		result.put("small",	"qlco");
		result.put("stage",	"neop-stage");
		result.put("total",	"qlco");
		result.put("very",  "qlco");
		result.put("10", "cardinal number");
		result.put("11", "cardinal number");
		result.put("67", "cardinal number");
		result.put("2", "cardinal number");
		result.put("4", "cardinal number");
		return result;
	}
	
	
}
