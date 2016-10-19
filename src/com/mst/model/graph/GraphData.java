package com.mst.model.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphData {
	List<Vertex> vertices = new ArrayList<>();
	List<Edge> edges = new ArrayList<>();
	
	public GraphData() {
		
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
}
