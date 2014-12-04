package com.mst.model;

import java.util.ArrayList;
import java.util.List;

public class NounPhraseMetadata {
	private List<TokenPosition> phrase = new ArrayList<TokenPosition>();
	private boolean negated;
	private boolean headEqPPObj;
	private TokenPosition headModByPP;

}
