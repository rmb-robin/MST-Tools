package com.mst.model.sql;

import java.sql.Date;
import java.util.ArrayList;

public class OntologyEntry {
	public String token;
	public String pos;
	public String snomed_id;
	public String _class;
	public String infinitive;
	public String source;
	public Date last_modified;
	public String modified_by;
	public ArrayList<SnomedTreeValue> snomed_tree;
}
