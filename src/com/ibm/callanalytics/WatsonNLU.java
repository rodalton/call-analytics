package com.ibm.callanalytics;

import java.util.List;
import java.util.Map;

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
	NaturalLanguageUnderstanding service;
	
	public WatsonNLU(){
    	dbManager = new ManageDB();
    	service = getNLU();    	
	}
   
	public void callNLU(SpeechResults speechResults, int call_id) {
		transcript = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();

		NaturalLanguageUnderstanding service = getNLU();

		System.out.println("WatsonNLU: Calling NLU for Call ID: " + call_id);
		
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
		if (keywords.size() > 0) {
			System.out.println("WatsonNLU: Persist NLU keywords for Call ID: " + call_id);
			insertKeywords(keywords, call_id);			
		} 
		else{
			System.out.println("WatsonNLU: No NLU keywords returned for Call ID: " + call_id);
		}

		
		//Insert entities into the db 
		List<EntitiesResult> entities = response.getEntities();
		if (entities.size() > 0) {
			System.out.println("WatsonNLU: Persist NLU entities for Call ID: " + call_id);
			insertEntities(entities, call_id);			
		}
		else {
			System.out.println("WatsonNLU: No NLU entities returned for Call ID: " + call_id);
		}
	}
	
	private void insertKeywords(List<KeywordsResult> keywords, int call_id){
		
		for (int i=0; i<keywords.size(); i++){ 
			//Only persist relevant keywords 
			if(keywords.get(i).getRelevance() > 0.5){ 
				dbManager.connect();
	            dbManager.insertKeywords(keywords.get(i).getText(), call_id);
			}
		}	
	}
	
	private void insertEntities(List<EntitiesResult> entities, int call_id){
		 
		for (int i=0; i<entities.size(); i++){ 
			//Only persist relevant entities 
			if(entities.get(i).getRelevance() > 0.5){ 
				dbManager.connect();
	            dbManager.insertKeywords(entities.get(i).getText(), call_id);
			}
		}	
	}
	
	private NaturalLanguageUnderstanding getNLU(){
		Map<String, String> credentials = VCAPHelper.getNLUCreds();
		String username = credentials.get("username").toString(); 
		String password = credentials.get("password").toString(); 
		String url = credentials.get("url").toString(); 
		
		service = new NaturalLanguageUnderstanding(
				NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
				username,password);
		service.setEndPoint(url);
		
		return service; 
	}
}
