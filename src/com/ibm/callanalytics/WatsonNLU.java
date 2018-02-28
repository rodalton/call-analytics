package com.ibm.callanalytics;

import java.util.List;

import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;

public class WatsonNLU {

	String transcript; 
	ManageDB dbManager = null;
	
	public WatsonNLU(){
    	dbManager = new ManageDB();
    	
	}
   
	public void callNLU(SpeechResults speechResults, int call_id) {
		transcript = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();

		NaturalLanguageUnderstanding service = getNLU();

		EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
				.emotion(true)
				.sentiment(true)
				.limit(10)
				.build();

		KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
				.emotion(true)
				.sentiment(true)
				.limit(10)
				.build();

		Features features = new Features.Builder()
				.entities(entitiesOptions)
				.keywords(keywordsOptions)
				.build();

		AnalyzeOptions parameters = new AnalyzeOptions.Builder()
				.text(transcript)
				.features(features)
				.build();

		AnalysisResults response = service
				.analyze(parameters)
				.execute();
	
		//Insert keywords into the db 
		List<KeywordsResult> keywords = response.getKeywords();
		insertKeywords(keywords, call_id);
		
		//Insert entities into the db 
		List<EntitiesResult> entities = response.getEntities();
		insertEntities(entities, call_id);		
	}
	
	private void insertKeywords(List<KeywordsResult> keywords, int call_id){
		//Only persist relevant keywords 
		for (int i=0; i<keywords.size(); i++){ 
			if(keywords.get(i).getRelevance() > 0.5){ 
				dbManager.connect();
	            dbManager.insertKeywords(keywords.get(i).getText(), call_id);
			}
		}	
	}
	
	private void insertEntities(List<EntitiesResult> entities, int call_id){
		//Only persist relevant entities  
		for (int i=0; i<entities.size(); i++){ 
			if(entities.get(i).getRelevance() > 0.5){ 
				dbManager.connect();
	            dbManager.insertKeywords(entities.get(i).getText(), call_id);
			}
		}	
	}
	
	private NaturalLanguageUnderstanding getNLU(){
		String username; 
		String password; 
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject creds = VCAPHelper.getCloudCredentials("natural-language-understanding");
			if(creds == null){
				System.out.println("No Natural Language Understanding service bound to this application");
				return null;
			}
			username = creds.get("username").getAsString();
			password = creds.get("password").getAsString();
		} else {
			System.out.println("Running locally. Looking for credentials in resource.properties");
			username = VCAPHelper.getLocalProperties("resource.properties").getProperty("nlu_username");
			password = VCAPHelper.getLocalProperties("resource.properties").getProperty("nlu_password");
			if(username == null || username.length()==0){
				System.out.println("Missing Speech to Text credentials in resource.properties");
				return null;
			}
		}
		NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
				NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
				username,password);
		return service; 
	}
}
