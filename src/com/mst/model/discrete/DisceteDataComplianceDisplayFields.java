package com.mst.model.discrete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
@Entity("disceteDataComplianceDisplayFields")
//@Indexes({
//  @Index(fields = @Field("id"))
//})
public class DisceteDataComplianceDisplayFields {

	@Id
	private ObjectId id; 
	private String orgId; 
	private String orgName;
	private Map<String,DiscreteDataBucketGroup> bucketGroups;

	public DisceteDataComplianceDisplayFields(){
		bucketGroups = new HashMap<>();
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Map<String, DiscreteDataBucketGroup> getBucketGroups() {
		return bucketGroups;
	}

	public void setBucketGroups(Map<String, DiscreteDataBucketGroup> bucketGroups) {
		this.bucketGroups = bucketGroups;
	}



	
}
