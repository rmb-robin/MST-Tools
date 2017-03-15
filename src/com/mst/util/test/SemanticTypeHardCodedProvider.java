package com.mst.util.test;

import java.util.HashMap;
import java.util.Map;

import com.mst.interfaces.SemanticTypeProvider;

public class SemanticTypeHardCodedProvider implements SemanticTypeProvider {

	public Map<String, String> getSemanticTypes() {
		Map<String,String> result = new HashMap<String,String>();
		result.put("liver","bpoc");
		result.put("breast","bpoc");
		result.put("kidney","bpoc");
		result.put("t cell","bpoc-cell");
		result.put("Myriad Genetic Laboratories"," co name");
		result.put("lupron","drugpr");
		result.put("tamoxifen",	"drugpr");
		result.put("abdominal aortic aneurysm",	"dysn");
		result.put("ct scan","proc"); 
		result.put("ultrasound","proc"); 
		result.put("biopsy","proc"); 
		result.put("pap smear","proc"); 
		result.put("mammogram","proc"); 
		result.put("bone-marrow-biopsy","proc"); 
		result.put("ultrasound-guided-biopsy","proc"); 
		result.put("cancer panel","proc"); 
		result.put("Magnetic resonance imaging","proc"); 
		result.put("therapy","proc"); 
		result.put("triple positive","qlco");
		result.put("triple negative", "qlco");
		result.put("radiation",	"proc"); 
		result.put("colonoscopies",	"proc"); 
		result.put("ovary","bpoc");
		result.put("cyst","dysn");
		result.put("treatment",	"proc"); 
		result.put("probably","qlco");
		result.put("likely","qlco");		
		result.put("ovarian",	"bpoc");
		result.put("abdominal",	"bpoc");
		result.put("lcis","dysn");
		result.put("cancer", "dysn");
		result.put("brca", "gene");
		result.put("uterus", "bpoc");
		result.put("mass", "dysn");
		result.put("lesion", "dysn");
		result.put("excisional biopsy",	"proc");
		return result;
	}

}



 
