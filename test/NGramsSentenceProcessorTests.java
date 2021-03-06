import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
 
import org.junit.Test;

import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.TestDataProvider;
import com.mst.model.SentenceToken;
import com.mst.model.sentenceProcessing.NGramsModifierEntity;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.NGramsSentenceProcessorImpl;
import com.mst.sentenceprocessing.SentenceCleaner;
import com.mst.sentenceprocessing.Tokenizer;

import static org.junit.Assert.*;

public class NGramsSentenceProcessorTests {



	
	private String getTestDataPath(){
		return System.getProperty("user.dir") + "\\testData\\sentences.txt";
	}
	
	private String getNGramsExpectedPath(){
		return System.getProperty("user.dir") + "\\testData\\ngramsExpected.txt";
	}
	
	private String getoutputPath(){
		return System.getProperty("user.dir") + "\\testoutput\\ngramTest.txt";
	}	
	

	//@Test
	public void process() {
		List<String> output = new ArrayList<>();
		NGramsSentenceProcessorImpl processor = new NGramsSentenceProcessorImpl();
		String fileText = TestDataProvider.getFileText(getTestDataPath());
		List<Sentence> originalSentences = TestDataProvider.getSentences(fileText);
		
		fileText = TestDataProvider.getFileText(getNGramsExpectedPath());
		List<Sentence> expectedSentences = TestDataProvider.getSentences(fileText);
		
		NGramsHardCodedProvider ngramsProvider = new NGramsHardCodedProvider();
		
		int index = 0;
		for(Sentence sentence: originalSentences){
			Sentence ngramsProcessedSentence = processor.process(sentence,ngramsProvider.getNGrams());
		    assertEquals(expectedSentences.get(index).getOrigSentence(), ngramsProcessedSentence.getNormalizedSentence());
			output.add(getSentenceOutput(sentence));
		    index+=1;
		}
		writeOutput(output);
	}
	
	private String getSentenceOutput(Sentence sentence){
		StringBuilder sb = new StringBuilder();
		sb.append("Original Sentence: " + sentence.getOrigSentence());
		sb.append(System.getProperty("line.separator"));
		sb.append("Modified Sentence: " + sentence.getNormalizedSentence());
		sb.append(System.getProperty("line.separator"));
		
		sb.append(System.getProperty("line.separator"));
		sb.append("Original Tokens");
		sb.append(System.getProperty("line.separator"));
		for(String word: sentence.getOriginalWords()){
			sb.append(word);
			sb.append(System.getProperty("line.separator"));				
		}
		
		sb.append(System.getProperty("line.separator"));
		sb.append("Modified Tokens");
		sb.append(System.getProperty("line.separator"));
		for(WordToken token: sentence.getModifiedWordList()){
			sb.append(token.getToken());
			sb.append(System.getProperty("line.separator"));				
		}
	
		sb.append("************************************************");
		sb.append(System.getProperty("line.separator"));
		return sb.toString();
	
	}
	
	private void writeOutput(List<String> output){
		Path out = Paths.get(getoutputPath());
		try {
			Files.write(out,output,Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
