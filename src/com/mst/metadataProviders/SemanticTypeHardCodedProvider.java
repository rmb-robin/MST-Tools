package com.mst.metadataProviders;

import java.util.HashMap;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.SemanticTypeProvider;

public class SemanticTypeHardCodedProvider implements SemanticTypeProvider {

	public Map<String, String> getSemanticTypes() {
		Map<String,String> result = new HashMap<String,String>();
		result.put("liver","bpoc");
		result.put("breast","bpoc");
		result.put("kidney","bpoc");
		result.put("spine","bpoc");
		result.put("stomach","bpoc");
		result.put("t cell","bpoc-cell");
		result.put("Myriad Genetic Laboratories"," co name");
		result.put("lupron","drugpr");
		result.put("xtandi","drugpr");
		result.put("zytiga","drugpr");
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
		result.put("ct-scan",	"proc"); 
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
		result.put("hepatic",	"bpoc");
		
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
//		result.put("1",	"cardinal number");
//		result.put("10",	"cardinal number");
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



 
