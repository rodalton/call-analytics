package com.ibm.callanalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;

public class WatsonTone {
	
	ToneAnalyzer service; 
	
	public WatsonTone() {
		service = getToneAnalyser();
	}
	
	public void callToneAnalyzer(String transcript, int call_id) {
        JsonParser jParser = new JsonParser();
        JsonArray jArray = (JsonArray) jParser.parse(transcript) ;
        
        //Block below is to get customer engagement tone
        //See here https://www.ibm.com/watson/developercloud/tone-analyzer/api/v3/#customer-tone
        List<Utterance> watsonUtterances = new ArrayList<>();

        //Use this array of strings to store speaker labels
        String[] speakers = new String[jArray.size()];

        for (int i=0; i<jArray.size(); i++) {
        	JsonObject jObj = (JsonObject)jArray.get(i);
        	Utterance utterance = new com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance.Builder()
            .text(jObj.get("transcript").toString())
            .user(jObj.get("speaker").toString())
            .build();
        	watsonUtterances.add(utterance);
        	//Stash the speaker label on this string array 
        	speakers[i] = jObj.get("speaker").toString();
        }
        
        //Call Watson Tone Analyzer & insert into db 
        System.out.println("WatsonTone: Calling Tone Analyzer for Call ID: " + call_id);
        
        ToneChatOptions options = new ToneChatOptions.Builder()
          .utterances(watsonUtterances).build();
        UtteranceAnalyses tones = service.toneChat(options).execute();         
        
        //Create a JSON Object from tones
        JsonObject toneObj = (JsonObject) jParser.parse(tones.toString()) ;
        JsonArray utteranceArray = toneObj.getAsJsonArray("utterances_tone");
        
        JsonArray toneArray = null; 
        String utterance = ""; 
        String tone = "";
        String speaker = "";
        
    	ManageDB dbManager = new ManageDB();
        
        //Use this to insert into the UTTERANCES table 
    	for (int i=0; i<utteranceArray.size(); i++) { 
    		JsonObject jObj = (JsonObject)utteranceArray.get(i);
            utterance = jObj.get("utterance_text").toString();
            //User substring as db stores just 500 chars
            if (utterance.length() > 500) {
            	utterance = utterance.substring(0, 499);
            }
            
            toneArray = jObj.getAsJsonArray("tones");
            
            if(toneArray.size() > 0) {
            	jObj = (JsonObject)toneArray.get(0);
            	tone =  jObj.get("tone_id").toString();               	
            }  
            else {
            	tone = "";
            }
            
            speaker = speakers[i];
            dbManager.connect();
            dbManager.insertUtterance(speaker, utterance, tone, call_id);
        }  
	}
	
	private ToneAnalyzer getToneAnalyser(){
		Map<String, String> credentials = VCAPHelper.getToneAnalyzerCreds();
		String username = credentials.get("username").toString(); 
		String password = credentials.get("password").toString(); 
		
        service = new ToneAnalyzer("2017-09-21");
        service.setUsernameAndPassword(username, password);
		
		return service; 
	}
}
