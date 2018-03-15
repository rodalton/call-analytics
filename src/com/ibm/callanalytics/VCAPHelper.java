package com.ibm.callanalytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VCAPHelper {
	static String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

	//Get dashDB credentials
	public static Map<String, String> getDbCreds(){
		Map<String, String> credentials = new HashMap<>();
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("dashDB");
			if(creds == null){
				System.out.println("No dashDB service bound to this application");
				return null;
			}
			credentials.put("username", creds.get("username").getAsString());
			credentials.put("password", creds.get("password").getAsString());
			credentials.put("jdbcurl", creds.get("jdbcurl").getAsString());
		} else {
			credentials.put("username", VCAPHelper.getLocalProperties("resource.properties").getProperty("db_username"));
			credentials.put("password", VCAPHelper.getLocalProperties("resource.properties").getProperty("db_password"));
			credentials.put("jdbcurl", VCAPHelper.getLocalProperties("resource.properties").getProperty("jdbcurl"));
		}
		return credentials;
	}
	
	//Get NLU credentials
	public static Map<String, String> getNLUCreds(){
		Map<String, String> credentials = new HashMap<>();
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("natural-language-understanding");
			if(creds == null){
				System.out.println("No NLU service bound to this application");
				return null;
			}
			credentials.put("username", creds.get("username").getAsString());
			credentials.put("password", creds.get("password").getAsString());
		} else {
			credentials.put("username", VCAPHelper.getLocalProperties("resource.properties").getProperty("nlu_username"));
			credentials.put("password", VCAPHelper.getLocalProperties("resource.properties").getProperty("nlu_password"));
		}
		return credentials;
	}
	
	//Get ToneAnalyzer credentials
	public static Map<String, String> getToneAnalyzerCreds(){
		Map<String, String> credentials = new HashMap<>();
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("tone_analyzer");
			if(creds == null){
				System.out.println("No Tone Analyzer service bound to this application");
				return null;
			}
			credentials.put("username", creds.get("username").getAsString());
			credentials.put("password", creds.get("password").getAsString());
		} else {
			credentials.put("username", VCAPHelper.getLocalProperties("resource.properties").getProperty("tone_username"));
			credentials.put("password", VCAPHelper.getLocalProperties("resource.properties").getProperty("tone_password"));
		}
		return credentials;
	}
	
	//Get STT credentials
	public static Map<String, String> getSTTCreds(){
		Map<String, String> credentials = new HashMap<>();
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("speech_to_text");
			if(creds == null){
				System.out.println("No STT service bound to this application");
				return null;
			}
			credentials.put("username", creds.get("username").getAsString());
			credentials.put("password", creds.get("password").getAsString());
		} else {
			credentials.put("username", VCAPHelper.getLocalProperties("resource.properties").getProperty("stt_username"));
			credentials.put("password", VCAPHelper.getLocalProperties("resource.properties").getProperty("stt_password"));
		}
		return credentials;
	}

	//Get credentials & connection info for IBM COS
	public static Map<String, String> getCOSCreds(){
		Map<String, String> credentials = new HashMap<>();
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("cloud-object-storage");
			if(creds == null){
				System.out.println("No COS service bound to this application");
				return null;
			}
			credentials.put("apikey", creds.get("apikey").getAsString());
			credentials.put("resource_instance_id", creds.get("resource_instance_id").getAsString());
			credentials.put("cos_endpoint_url", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_url"));
			credentials.put("cos_endpoint_location", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_location"));
		} else {
			credentials.put("apikey", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_api_key"));
			credentials.put("resource_instance_id", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_service_instance_id"));
			credentials.put("cos_endpoint_url", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_url"));
			credentials.put("cos_endpoint_location", VCAPHelper.getLocalProperties("resource.properties").getProperty("cos_endpoint_location"));			
		}
		return credentials;
	}

	//Get credentials JSON from VCAP_SERVICES 
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