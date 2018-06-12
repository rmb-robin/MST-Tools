package com.mst.model.graph;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.mst.util.Constants.GraphClass;

public class GraphElement {
	@SerializedName("class") // Gson override of var name to a reserved word
	private String _class;
	private Map<String, Object> props;
	
	public GraphElement() {
		props = new HashMap<String, Object>();
	}
	
	public GraphElement(GraphClass _class) {
		this();
		this._class = _class.toString();
	}
	
	// 10/10/2016 - SRD - added to support dynamic frames
	public GraphElement(String _class) {
		this();
		this._class = _class;
	}
	
	public String get_Class() {
		return _class;
	}
	
	public void set_Class(String _class) {
		this._class = _class;
	}
	
	public Map<String, Object> getProps() {
		return props;
	}
	
	public void setProps(Map<String, Object> props) {
		this.props = props;
	}
/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_class == null) ? 0 : _class.hashCode());
		result = prime * result + ((props == null) ? 0 : props.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphElement other = (GraphElement) obj;
		if (_class == null) {
			if (other._class != null)
				return false;
		} else if (!_class.equals(other._class))
			return false;
		if (props == null) {
			if (other.props != null)
				return false;
		} else if (!props.equals(other.props))
			return false;
		return true;
	}
*/
}
