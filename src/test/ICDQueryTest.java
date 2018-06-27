package test;

import org.junit.Test;

import com.mst.icd.query.ICDQuery;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.util.Constants;
import com.mst.util.Utility;

public class ICDQueryTest {

	// @Test
	public void processTokensFromFileTest() {
		ICDQuery icdQuery = new ICDQuery();
		icdQuery.processTokensFromFile();
	}

	@Test
	public void processToken() {
		String searchToken = "shortness";
		String orgId = Utility.getProperty(Constants.QUERY_INPUT_ORG_ID);
		String edgeName = WordEmbeddingTypes.tokenToken;
		ICDQuery icdQuery = new ICDQuery();
		icdQuery.processToken(orgId, edgeName, searchToken);
	}
}
