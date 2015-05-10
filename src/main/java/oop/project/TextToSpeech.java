package oop.project;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TextToSpeech {
    // Instance variables for a TextToSpeech object
    private static final String VOICENAME_kevin = "kevin";

    /* Static method that speaks a string as audio */
    public static void speak(String text) {
        Voice voice;
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(VOICENAME_kevin);
        voice.allocate();
        voice.speak(text);
    }

    /* Main method to test a string to audio */
    public static void main(String[] args) {
        speak("Your train is arriving soon!");
    }
}
