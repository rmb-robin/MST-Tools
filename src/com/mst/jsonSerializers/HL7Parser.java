package com.mst.jsonSerializers;

import com.mst.model.HL7Details;

import com.mst.model.raw.ParseHl7Result;
import com.mst.model.requests.SentenceTextRequest;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class HL7Parser {
	
	public ParseHl7Result run(HL7Details details, String payload, String OrgId, AllHl7Elements allHl7Elements) throws HL7Exception{
		PipeParser pipeParser = new PipeParser();
		pipeParser.setValidationContext(new NoValidation());
	    					
		HL7Processor hl7 = new HL7Processor(OrgId, details, pipeParser.parse(payload), null);
		ParseHl7Result result = new ParseHl7Result();
		result.setSentenceTextRequest(hl7.processMessage());
		result.setMissingFields(hl7.getMissingFields());
		result.setAllFields(hl7.getAllAvailableHl7Fields(allHl7Elements));
		return result;
	}
}
