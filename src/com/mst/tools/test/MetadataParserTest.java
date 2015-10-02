package com.mst.tools.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mst.model.MapValue;
import com.mst.model.Sentence;
import com.mst.model.StructuredData;
import com.mst.tools.StructuredOutputHelper;

public class MetadataParserTest {
	Util util = new Util();
	
	@Test
	public final void testQualifierOverride() {
		
		Sentence s = util.annotateSentence("I will start her on Vesicare 5 mg daily, samples given.");	
		StructuredOutputHelper struct = new StructuredOutputHelper(false);
		
		StructuredData output = struct.process(s, false);
		
		List<MapValue> values = new ArrayList<MapValue>();
		output.getValueFromList(output.related, "Admin of Drug", "Vesicare", "Admin of Drug", ".*", values);
		
		assertEquals("Value must be 'Vesicare'", "Vesicare", values.get(0).value);
		assertEquals("Qualifier must be 'Future'", "Future", values.get(0).qualifier);		
	}
}
