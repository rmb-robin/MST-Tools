package com.mst.metadataProviders;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mst.model.sentenceProcessing.DynamicEdgeCondition;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;

public class DynamicRuleProvider extends BaseProvider {

	public List<DynamicEdgeCreationRule> getRules(){
		List<String> lines = TestDataProvider.readLines(getFullFilePath());
		List<DynamicEdgeCreationRule> rules = new ArrayList<>();
		DynamicEdgeCreationRule rule=null;
		if (lines != null) {
			for(String line:lines){
				String[]contents = line.split(",");
				String type = contents[0];
				if(type.equals("r")){
					rule = getRule(contents);
					rules.add(rule);
					continue;
				}
				if(type.equals("c"))
					if(rule!=null)
						rule.getConditions().add(getCondition(contents));
			}
		}
		return rules;
	}

	private String getFullFilePath(){
		return System.getProperty("user.dir") + File.separator + "testData" + File.separator + "dynamicEdgeCreation.txt";
	}

	private DynamicEdgeCreationRule getRule(String[] contents){
		DynamicEdgeCreationRule rule = new DynamicEdgeCreationRule();
		rule.setName(contents[1]);
		rule.setEdgeName(contents[2]);
		if(!contents[3].equals("")){
			String[] edgeArray = contents[3].split(";");
			rule.setFromEdgeNames(new ArrayList<>(Arrays.asList(edgeArray)));
			rule.setFromTokenSemanticType(false);
			rule.setFromToken(null);
		}
		else {
			rule.setFromToken(contents[4]);
			rule.setFromTokenSemanticType(convertToBool(contents[5]));
		}
		rule.setToToken(contents[6]);
		rule.setToTokenSemanticType(convertToBool(contents[7]));
		return rule;
	}

	private DynamicEdgeCondition getCondition(String[] content){
		DynamicEdgeCondition condition = new DynamicEdgeCondition();
		condition.setCondition1Token(convertToBool(content[1]));
		condition.setToken(getString(content[2]));
		condition.setIsTokenSemanticType(convertToBool(content[3]));
		condition.setIsTokenPOSType(convertToBool(content[4]));
		condition.setEdgeNames(getValuesforSemiColenList(content[5]));
		condition.setFromTokens(getValuesforSemiColenList(content[6]));
		condition.setIsFromTokenSemanticType(convertToBool(content[7]));
		condition.setIsFromTokenPOSType(convertToBool(content[8]));
		condition.setToTokens(getValuesforSemiColenList(content[9]));
		condition.setIsToTokenSemanticType(convertToBool(content[10]));
		condition.setIsToTokenPOSType(convertToBool(content[11]));
		condition.setIsEqualTo(convertToBool(content[12]));
		return condition;
	}
}
