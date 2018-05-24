package com.mst.model.graph;

import java.util.Comparator;
import java.util.Objects;

import com.mst.util.Constants.GraphClass;

public class Edge extends GraphElement {//implements Comparable<Edge> {
	// TODO this should store from and to Vertex objects rather than strings
	
	private String fromVertexUUID;
	private String toVertexUUID;
	private String fromVertex;
	private String toVertex;
	
	public Edge() {
		super();
	}
	
	public Edge(String _class, Vertex fromVertex, Vertex toVertex) {
		super(_class);
		this.fromVertexUUID = fromVertex.getUUID();
		this.toVertexUUID = toVertex.getUUID();
		
		this.fromVertex = fromVertex.getValue();
		this.toVertex = toVertex.getValue();
	}
	
	public Edge(GraphClass _class, Vertex fromVertex, Vertex toVertex) {
		this(_class.toString(), fromVertex, toVertex);
	}

	public int getObjectId() {
		int ret = -1;
		
		try {
			ret = (int) this.getProps().get("objectId");
		} catch(Exception e) { }
		
		return ret;
	}
	
	public String getFromVertex() {
		return fromVertexUUID;
	}

	public String getFromVertexString() {
		return fromVertex;
	}
	
	public String getToVertexString() {
		return toVertex;
	}
	
	public void setFromVertex(String fromVertex) {
		this.fromVertexUUID = fromVertex;
	}

	public String getToVertex() {
		return toVertexUUID;
	}

	public void setToVertex(String toVertex) {
		this.toVertexUUID = toVertex;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fromVertexUUID, toVertexUUID, getProps().get("type"));
	}

	@Override
	public boolean equals(Object o) {
		
		if(o == this)
			return true;
		if(!(o instanceof Edge)) {
			return false;
		}
		Edge edge = (Edge) o;
		return Objects.equals(fromVertexUUID, edge.fromVertexUUID) &&
			   Objects.equals(toVertexUUID, edge.toVertexUUID) &&
			   Objects.equals(getProps().get("type"), edge.getProps().get("type"));
	}
	/*
	@Override
	public int compareTo(Edge o) {
		//if(equals(o)) {
		//	return 0;
		//} else {
			return Integer.compare(this.getObjectId(), o.getObjectId());
		//}
	}
	*/
}
