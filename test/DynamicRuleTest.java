import java.util.List;

import org.junit.Test;

import com.mst.metadataProviders.DynamicRuleProvider;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;

public class DynamicRuleTest {

	@Test
	public void getRules(){
		List<DynamicEdgeCreationRule> rules = new DynamicRuleProvider().getRules();
		String t = "S";
	}
}
