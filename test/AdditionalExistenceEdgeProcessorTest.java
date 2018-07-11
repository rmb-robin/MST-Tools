import java.util.List;

import org.junit.Test;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import com.mst.util.TokenRelationshipUtil;

import static org.junit.Assert.*;


public class AdditionalExistenceEdgeProcessorTest {

	private SentenceProcessingControllerImpl sentenceController; 
	
	private SentenceTextRequest getRequest(String sentence){
		SentenceTextRequest request = new SentenceTextRequest();
		request.setDiscreteData(new DiscreteData());
		request.setText(sentence);
		return request;
	}
	
	@Test
	public void process() throws Exception{
		sentenceController = new SentenceProcessingControllerImpl();
		sentenceController.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
		SentenceProcessingResult result = sentenceController.processText(getRequest("anechoic interpolar cyst measures up to 1 cm in diameter."));
		Sentence sentence = result.getSentences().get(0);

		List<TokenRelationship> relationships =  TokenRelationshipUtil.getTokenRelationshipsByEdgeName(EdgeNames.existence, sentence.getTokenRelationships());
		assertEquals(1, relationships.size());
		
		TokenRelationship first = relationships.get(0);
		assertEquals("anechoic", first.getFromToken().getToken());
		assertEquals("cyst", first.getToToken().getToken());
		
		
		result = sentenceController.processText(getRequest("simple right ovarian cyst."));
		sentence = result.getSentences().get(0);

		relationships =  TokenRelationshipUtil.getTokenRelationshipsByEdgeName(EdgeNames.existence, sentence.getTokenRelationships());
		assertEquals(1, relationships.size());
		
		
		
	}
}
