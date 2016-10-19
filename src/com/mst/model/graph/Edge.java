package com.mst.model.graph;

import com.mst.util.Constants.GraphClass;

public class Edge extends GraphElement {
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

	public String getFromVertex() {
		return fromVertexUUID;
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
}
