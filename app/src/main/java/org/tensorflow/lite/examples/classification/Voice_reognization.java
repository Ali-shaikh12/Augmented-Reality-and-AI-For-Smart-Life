package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Voice_reognization extends AppCompatActivity implements RecognitionListener {


    TextView textView;
    MqttAndroidClient client;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_reognization);
        String clientId = MqttClient.generateClientId();
        String ip="";

        textView=findViewById(R.id.textchange);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


        speech.startListening(recognizerIntent);


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });


        client = new MqttAndroidClient(this, "tcp://broker.hivemq.com", clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Voice_reognization.this, "Connected to MQTT broker", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Voice_reognization.this, "Connection to MQTT broker failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    public void setDevices(String message) {
        try {
            if (client != null && client.isConnected()) {
                client.publish("ARHOME/devices", message.getBytes(), 0, false);
            } else {
                Log.e(LOG_TAG, "MQTT client not connected.");
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error publishing MQTT message: " + ex.getMessage());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
        //startLocationUpdates();
    }
    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.e(LOG_TAG, "Speech recognition error: " + errorMessage);
    }
    public  String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        speech.startListening(recognizerIntent);
        return message;
    }
    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        StringBuilder text = new StringBuilder();

        if (matches != null && !matches.isEmpty()) {
            text.append(matches.get(0));
        }

        textView.setText(text.toString());


        String lowerCaseText = text.toString().toLowerCase();

        if (lowerCaseText.contains("turn on light") || lowerCaseText.contains("turn on bulb") ||
                lowerCaseText.contains("on light") || lowerCaseText.contains("on bulb")) {
            Toast.makeText(this, "on light", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "Turning on the light");
            setDevices("L1:on");
            textView.setText("Light is turned on");
        }

        if (lowerCaseText.contains("turn on fan") || lowerCaseText.contains("turn on fan1") ||
                lowerCaseText.contains("on fan") || lowerCaseText.contains("fan on")) {
            Log.d(LOG_TAG, "Turning on the fan");
            setDevices("F1:on");
            textView.setText("Fan is turned on");
        }
        if (lowerCaseText.contains("turn off fan") || lowerCaseText.contains("off fan") ||
                lowerCaseText.contains("fan off") || lowerCaseText.contains("off fan")) {
            Log.d(LOG_TAG, "Turning on the light");
            setDevices("F1:off");
            textView.setText("Fan is turned off");
        }

        if (lowerCaseText.contains("turn off light") || lowerCaseText.contains("off light") ||
                lowerCaseText.contains("light off") || lowerCaseText.contains("off bulb")) {
            Log.d(LOG_TAG, "Turning on the light");
            setDevices("L1:off");
            textView.setText("light is turned off");
        }

        Log.d(LOG_TAG, "Recognized text: " + text.toString());
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        // Release TextToSpeech resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        // Release SpeechRecognizer resources
        if (speech != null) {
            speech.destroy();
        }
    }
    @Override
    public void onEvent(int eventType, Bundle params) {
        // Handle the event, or provide an empty implementation if not needed
        Log.i(LOG_TAG, "onEvent");
    }
}