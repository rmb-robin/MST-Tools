package com.mst.interfaces.dao;

import java.util.Set;

public interface RedisManager {

	void addToSet(String key, String value);
	Set<String> getSet(String key);
	void addItem(String key, String value);
	String getItem(String key);
}
