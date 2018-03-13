package com.ibm.callanalytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VCAPHelper {
	static String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

	public static JsonObject getCloudCredentials(String serviceName) {
		if(VCAP_SERVICES == null){
			return null;
		}
		//Convert VCAP_SERVICES String to JSON
		JsonObject obj = (JsonObject) new JsonParser().parse(VCAP_SERVICES);		
		Entry<String, JsonElement> dbEntry = null;
		Set<Entry<String, JsonElement>> entries = obj.entrySet();
		
		// Look for the VCAP key that holds the service info
		for (Entry<String, JsonElement> eachEntry : entries) {
			if (eachEntry.getKey().contains(serviceName)) {
				dbEntry = eachEntry;
				break;
			}
		}
		if (dbEntry == null) {
			System.out.println("VCAP_SERVICES: Could not find " + serviceName);
			return null;
		}

		obj = (JsonObject) ((JsonArray) dbEntry.getValue()).get(0);
		System.out.println("VCAP_SERVICES: Found " + (String) dbEntry.getKey());

		return (JsonObject) obj.get("credentials");
	}
	
	public static Properties getLocalProperties(String fileName){
		Properties properties = new Properties();
		InputStream inputStream = VCAPHelper.class.getClassLoader().getResourceAsStream(fileName);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

}