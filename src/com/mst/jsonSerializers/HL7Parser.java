package com.mst.jsonSerializers;

import com.mst.model.HL7Details;
import com.mst.model.requests.SentenceTextRequest;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class HL7Parser {
	
	public SentenceTextRequest run(HL7Details details, String payload, String OrgId) throws HL7Exception{
		PipeParser pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
	    					
		HL7Processor hl7 = new HL7Processor(OrgId, details, pipeParser.parse(payload), null);
		return hl7.processMessage();
	}
}
