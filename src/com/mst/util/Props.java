package com.mst.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Props {

	private static String PROP_FILE = "/mst-tools.properties";
    private static Map<String, String> propsMap = null;
    private static Logger logger = LoggerFactory.getLogger(Props.class);
	
	public static String getProperty(String propName) {
        return getProperty(propName, null);
    }
	
	public static String getProperty(String propName, String defaultValue) {
        if(propsMap == null)
            loadPropsFile();
        
        String value = (String) propsMap.get(propName);
        
        if(value == null)
            value = defaultValue;
        
        return value;
    }
	
	private static void loadPropsFile() {
        InputStream is = null;

        try {
        	Properties props = new Properties();
            propsMap = new HashMap<String, String>();
            
            // get location of jar file
            String path = Props.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            path = (new File(path)).getParentFile().getPath() + PROP_FILE;
            //logger.info("props path: {}", path);

            is = new FileInputStream(path);
            
            props.load(is);

            for(Object key : props.keySet())
            	propsMap.put(key.toString(), props.get(key).toString());

        } catch(Exception e) {
            logger.error("Error loading properties file. {}", e);
        } finally {
        	if(is != null) {
                try {
                    is.close();
                }
                catch(IOException ioe) {
                    logger.warn("loadPropsFile(): Unable to close properties file. {}", ioe);
                }
        	}
        }
    }
}
