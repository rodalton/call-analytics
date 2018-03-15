package com.ibm.callanalytics;

import java.io.InputStream;
import java.util.Map;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

public class CallTranscript {
	
	int call_id;  
	SpeechToText service;
	
	public CallTranscript(int call_id){
		this.call_id = call_id; 
		service = getSTT();
	}
	
	//Update to use different models & file formats
	public void getTranscript(InputStream audio) {
			
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
		Map<String, String> credentials = VCAPHelper.getSTTCreds();
		String username = credentials.get("username").toString(); 
		String password = credentials.get("password").toString(); 
		
		service = new SpeechToText();
		service.setUsernameAndPassword(username, password);
		
		return service; 
	}
}
