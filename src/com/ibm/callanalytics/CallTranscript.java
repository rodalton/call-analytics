package com.ibm.callanalytics;

import java.io.InputStream;
import java.util.Map;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

public class CallTranscript {

	int call_id;  
	SpeechToText speechToTextService;

	public CallTranscript(int call_id){
		this.call_id = call_id; 
		speechToTextService = getSTT();
	}

	//Update to use different models & file formats
	public void getTranscript(InputStream audio) {

		RecognizeOptions options = new RecognizeOptions.Builder()
				.model("en-US_BroadbandModel").contentType("audio/wav")
				.speakerLabels(true).build();

		//New thread per audio file to be transcribed 
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					speechToTextService.recognizeUsingWebSocket(audio, options,
							new BaseRecognizeCallback() {
						@Override
						public void onTranscription(SpeechResults speechResults) {
							try {
								WatsonNLU watsonNLU = new WatsonNLU();
								watsonNLU.callNLU(speechResults, call_id);

								SpeakerLabels.RecoTokens recoTokens = new SpeakerLabels.RecoTokens();
								recoTokens.add(speechResults);

								String transcriptWithSpeakers = recoTokens.getTranscriptWithSpeakers();
								WatsonTone watsonTone = new WatsonTone();
								watsonTone.callToneAnalyzer(transcriptWithSpeakers, call_id);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onError(Exception e) {
							e.printStackTrace();
						}

						@Override
						public void onDisconnected() {
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}


	private SpeechToText getSTT(){
		Map<String, String> credentials = VCAPHelper.getSTTCreds();
		String username = credentials.get("username").toString(); 
		String password = credentials.get("password").toString(); 
		String url = credentials.get("url").toString(); 

		speechToTextService = new SpeechToText();
		speechToTextService.setUsernameAndPassword(username, password);
		speechToTextService.setEndPoint(url);

		return speechToTextService; 
	}
}
