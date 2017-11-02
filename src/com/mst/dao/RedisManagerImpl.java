package com.mst.dao;

import java.util.Set;

import com.mst.interfaces.dao.RedisManager;
import com.mst.util.Constants;

import redis.clients.jedis.Jedis;

public class RedisManagerImpl implements RedisManager {

	private Jedis jedis;
	
	public RedisManagerImpl() {
		jedis = Constants.RedisDBx.INSTANCE.getInstance();
	}

	@Override
	public void addToSet(String key, String value) {
		jedis.sadd(key,value);	
	}

	@Override
	public Set<String> getSet(String key) {
		return jedis.smembers(key);
	}

	@Override
	public void addItem(String key, String value) {
		jedis.set(key, value);
	}

	@Override
	public String getItem(String key) {
		return jedis.get(key);
	}
}
