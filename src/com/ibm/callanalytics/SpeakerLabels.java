package com.ibm.callanalytics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeakerLabel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.util.GsonSingleton;

public class SpeakerLabels {
	
    public static class RecoToken {
        private Integer speaker;
        private String word;

        /**
         * Instantiates a new reco token.
         *
         * @param speechTimestamp the speech timestamp
         */
        RecoToken(SpeechTimestamp speechTimestamp) {
            speechTimestamp.getStartTime();
            speechTimestamp.getEndTime();
            word = speechTimestamp.getWord();
        }

        /**
         * Instantiates a new reco token.
         *
         * @param speakerLabel the speaker label
         */
        RecoToken(SpeakerLabel speakerLabel) {
            speakerLabel.getFrom();
            speakerLabel.getTo();
            speaker = speakerLabel.getSpeaker();
        }

        /**
         * Update from.
         *
         * @param speechTimestamp the speech timestamp
         */
        void updateFrom(SpeechTimestamp speechTimestamp) {
            word = speechTimestamp.getWord();
        }

        /**
         * Update from.
         *
         * @param speakerLabel the speaker label
         */
        void updateFrom(SpeakerLabel speakerLabel) {
            speaker = speakerLabel.getSpeaker();
        }
    }

    /**
     * The Class Utterance.
     */
    public static class Utterance {
        private Integer speaker;
        private String transcript = "";

        /**
         * Instantiates a new utterance.
         *
         * @param speaker the speaker
         * @param transcript the transcript
         */
        public Utterance(final Integer speaker, final String transcript) {
            this.speaker = speaker;
            this.transcript = transcript;
        }
    }

    /**
     * The Class RecoTokens.
     */
    public static class RecoTokens {

        private Map<Double, RecoToken> recoTokenMap;

        /**
         * Instantiates a new reco tokens.
         */
        public RecoTokens() {
            recoTokenMap = new LinkedHashMap<Double, RecoToken>();
        }

        /**
         * Adds the.
         *
         * @param speechResults the speech results
         */
        public void add(SpeechResults speechResults) {
            if (speechResults.getResults() != null)
                for (int i = 0; i < speechResults.getResults().size(); i++) {
                    Transcript transcript = speechResults.getResults().get(i);
                    if (transcript.isFinal()) {
                        SpeechAlternative speechAlternative = transcript.getAlternatives().get(0);

                        for (int ts = 0; ts < speechAlternative.getTimestamps().size(); ts++) {
                            SpeechTimestamp speechTimestamp = speechAlternative.getTimestamps().get(ts);
                            add(speechTimestamp);
                        }
                    }
                }
            if (speechResults.getSpeakerLabels() != null)
                for (int i = 0; i < speechResults.getSpeakerLabels().size(); i++) {
                    add(speechResults.getSpeakerLabels().get(i));
                }

        }

        /**
         * Adds the.
         *
         * @param speechTimestamp the speech timestamp
         */
        public void add(SpeechTimestamp speechTimestamp) {
            RecoToken recoToken = recoTokenMap.get(speechTimestamp.getStartTime());
            if (recoToken == null) {
                recoToken = new RecoToken(speechTimestamp);
                recoTokenMap.put(speechTimestamp.getStartTime(), recoToken);
            } else {
                recoToken.updateFrom(speechTimestamp);
            }
        }

        /**
         * Adds the.
         *
         * @param speakerLabel the speaker label
         */
        public void add(SpeakerLabel speakerLabel) {
            RecoToken recoToken = recoTokenMap.get(speakerLabel.getFrom());
            if (recoToken == null) {
                recoToken = new RecoToken(speakerLabel);
                recoTokenMap.put(speakerLabel.getFrom(), recoToken);
            } else {
                recoToken.updateFrom(speakerLabel);
            }

            if (speakerLabel.isFinal()) {
                getTranscriptWithSpeakers();
            }
        }


        /**
         * Transcript with speaker labels 
         */
        public String getTranscriptWithSpeakers() {
            List<Utterance> utterances = new ArrayList<Utterance>();
            Utterance currentUtterance = new Utterance(0, "");

            for (RecoToken rt : recoTokenMap.values()) {
                if (currentUtterance.speaker != rt.speaker) {
                    utterances.add(currentUtterance);
                    currentUtterance = new Utterance(rt.speaker, "");
                }
                currentUtterance.transcript = currentUtterance.transcript + rt.word + " ";
            }
            utterances.add(currentUtterance);                 
            
            String result = GsonSingleton.getGson().toJson(utterances);
            return result; 
        } 
    }    
}
