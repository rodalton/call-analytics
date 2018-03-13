package com.ibm.callanalytics;

import java.io.File;
import java.io.InputStream;

import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

public class CallTranscript {
	
	int call_id; 
	String time; 
	String date; 
	
	public CallTranscript(int call_id, String time, String date){
		this.call_id = call_id; 
		this.time = time; 
		this.date = date; 
	}
	
	//Update to use different models & file formats
	public void getTranscript(InputStream audio) {
			
		SpeechToText service = getSTT();
		RecognizeOptions options = new RecognizeOptions.Builder()
				.model("en-US_BroadbandModel").contentType("audio/wav")
				.speakerLabels(true).build();

		BaseRecognizeCallback callback = new BaseRecognizeCallback() {
			@Override
			public void onTranscription(SpeechResults speechResults) {
			
				WatsonNLU watsonNLU = new WatsonNLU();
				watsonNLU.callNLU(speechResults, call_id);
				
			    SpeakerLabels.RecoTokens recoTokens = new SpeakerLabels.RecoTokens();
				recoTokens.add(speechResults);
				
				String transcriptWithSpeakers = recoTokens.getTranscriptWithSpeakers();
				WatsonTone watsonTone = new WatsonTone();
				watsonTone.callToneAnalyzer(transcriptWithSpeakers, call_id);
			}

			@Override
			public void onDisconnected() {
				//System.exit(0);
			}
		};

		try {
			service.recognizeUsingWebSocket(audio, options, callback);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private SpeechToText getSTT(){
		String username; 
		String password; 
		
		if (System.getenv("VCAP_SERVICES") != null) {
			JsonObject sttCreds = VCAPHelper.getCloudCredentials("speech_to_text");
			if(sttCreds == null){
				System.out.println("No STT service bound to this application");
				return null;
			}
			username = sttCreds.get("username").getAsString();
			password = sttCreds.get("password").getAsString();
		} else {
			username = VCAPHelper.getLocalProperties("resource.properties").getProperty("stt_username");
			password = VCAPHelper.getLocalProperties("resource.properties").getProperty("stt_password");
			if(username == null || username.length()==0){
				System.out.println("Missing STT credentials in resource.properties");
				return null;
			}
		}
		
		SpeechToText service = new SpeechToText();
		service.setUsernameAndPassword(username, password);
		
		return service; 
	}
	
	
	/**
	 * Use when running as a stand-alone Java app 
	 */
	public SpeechResults getTranscriptStandAlone(){
	    SpeechToText sttservice = new SpeechToText();
	    sttservice.setUsernameAndPassword("6c563e0c-9caf-4416-a547-f197b57db027", "B4SXzphcQSH3");

	    //File audio = new File("/Users/daltonro/Downloads/SpaceShuttle.wav");
	    File audio = new File("/Users/daltonro/LostCard.wav");    
	    RecognizeOptions options = new RecognizeOptions.Builder()
	    		  .contentType("audio/wav").speakerLabels(true).smartFormatting(true).build();
	    
	    SpeechResults speechResults = sttservice.recognize(audio, options).execute();	
	    return speechResults; 
	}
}
