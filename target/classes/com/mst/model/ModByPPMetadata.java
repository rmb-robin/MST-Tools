package com.mst.model;

import com.mst.util.Constants.ModByPPClass;

public class ModByPPMetadata {
	private int idx;
	private ModByPPClass _class;
	
	public ModByPPMetadata(int idx, ModByPPClass _class) {
		this.idx = idx;
		this._class = _class;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public void setIdx(int idx) {
		this.idx = idx;
	}
	
	public ModByPPClass get_class() {
		return _class;
	}
	
	public void set_class(ModByPPClass _class) {
		this._class = _class;
	}
}
