package com.mst.model.recommandation;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.utils.IndexType;

import com.mst.model.sentenceProcessing.TokenRelationship;

@Entity("recommendedTokenRelatationshipsTest")
@Indexes({
    @Index(fields = {@Field(value = "key", type = IndexType.TEXT)})
})
public class RecommendedTokenRelationship {

	@Id
	private ObjectId id;

	private TokenRelationship tokenRelationship;
	private String key;
	private int iterationPoint;  
	
	private boolean isVerified;
	public TokenRelationship getTokenRelationship() {
		return tokenRelationship;
	}
	public void setTokenRelationship(TokenRelationship tokenRelationship) {
		this.tokenRelationship = tokenRelationship;
	}
	public boolean getIsVerified() {
		return isVerified;
	}
	public void setIsVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getIterationPoint() {
		return iterationPoint;
	}
	public void setIterationPoint(int iterationPoint) {
		this.iterationPoint = iterationPoint;
	}

	
	
}
