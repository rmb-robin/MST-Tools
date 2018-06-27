package com.mst.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utility {
	static Properties properties = new Properties();

	static {
		loadProperties();
	}

	public static void loadProperties() {
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getProperty(String key) {
		return (String) properties.get(key);
	}

	public static void printAllProperties() {
		properties.forEach((key, value) -> System.out.println(key + " : " + value));
	}
}
