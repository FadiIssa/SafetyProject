package com.example.fadi.testingrx.audio;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by fadi on 05/01/2018.
 */

public class Speaker {
    public static Speaker instance;
    TextToSpeech mTTS;
    String TAG = "Audio";
    boolean isAudioInitializationOK;

    boolean speakerIsMuted;

    private Speaker(Context ctx){
        initializeSpeaker(ctx);
    }

    //this could play the role of initializer, without the need to return an instance.
    public static void PrepareSpeaker(Context ctx){
        if (instance==null){
            instance = new Speaker(ctx);
        }
    }

    public static Speaker getInstance(Context ctx){
        if (instance==null){
            instance = new Speaker(ctx);
        }
        return instance;
    }

    // this method is called from the constructor, usually the constructor should be called only one time per the life cycle of an app, no need to create new Speaker for every new running activity. app context should be passed on from the caller
    private void initializeSpeaker(Context mContext) {
        speakerIsMuted=false;
        mTTS = new TextToSpeech(mContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isAudioInitializationOK = true;
                Log.d(TAG, "speaker initialization is successful.");
                Log.e(TAG, "This Language : " + Locale.getDefault().toString() + " is the local one");
                String currentLanguage = Locale.getDefault().getLanguage();
                Log.d(TAG, "current language is:" + currentLanguage);

                mTTS.setLanguage(Locale.ENGLISH);

                mTTS.setPitch(0.9f);
                mTTS.setSpeechRate(1f);

            } else {
                isAudioInitializationOK = false;
                Log.e(TAG, "Initialization Failed!");
            }
        });
    }

    public void speak(String text){

        if (speakerIsMuted) {
            return;
        }

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void speakPosture(String text) {

        if (speakerIsMuted) {
            return;
        }

        mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);}

    public void muteSpeaker(){
        speakerIsMuted=true;
    }

    public void unmuteSpeaker(){
        speakerIsMuted=false;
    }
}
