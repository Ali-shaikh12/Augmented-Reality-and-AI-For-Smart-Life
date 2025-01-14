/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
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
import org.tensorflow.lite.examples.classification.env.BorderedText;
import org.tensorflow.lite.examples.classification.env.Logger;
import org.tensorflow.lite.examples.classification.tflite.Classifier;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Device;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassifierActivity2 extends CameraActivity implements OnImageAvailableListener/*, RecognitionListener*/ {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  public Classifier classifier;
  private BorderedText borderedText;
  /** Input image size of the model along x axis. */
  private int imageSizeX;
  /** Input image size of the model along y axis. */
  private int imageSizeY;

  TextView textView;
  MqttAndroidClient client;

  private SpeechRecognizer speech ;
  private Intent recognizerIntent;

  private String LOG_TAG = "VoiceRecognitionActivity";
  TextToSpeech textToSpeech;
    public static int flg = 0;
    public static int prevFlg=0;
    int flglight = 0;
    private boolean lightOnMessageDisplayed = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   // setContentView(R.layout.activity_voice_reognization);
    String clientId = MqttClient.generateClientId();
    String ip="";

    //textView=findViewById(R.id.textchange);

      speech = SpeechRecognizer.createSpeechRecognizer(this);
      speech.setRecognitionListener(new RecognitionListenerImpl());

      // Initialize recognizer intent
      recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    //speech.startListening(recognizerIntent);


   /* textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
          textToSpeech.setLanguage(Locale.UK);
        }
      }
    });
*/

    client = new MqttAndroidClient(this, "tcp://broker.hivemq.com", clientId);

    try {
      IMqttToken token = client.connect();
      token.setActionCallback(new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          Toast.makeText(ClassifierActivity2.this, "Connected to MQTT broker", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          Toast.makeText(ClassifierActivity2.this, "Connection to MQTT broker failed", Toast.LENGTH_SHORT).show();
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
  public void onDestroy() {
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
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    recreateClassifier(getModel(), getDevice(), getNumThreads());
    if (classifier == null) {
      LOGGER.e("No classifier on preview!");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
  }

  @Override
  protected void processImage() {
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final int cropSize = Math.min(previewWidth, previewHeight);

    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                if (classifier != null) {
                  final long startTime = SystemClock.uptimeMillis();
                  final List<Classifier.Recognition> results =
                          classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                  lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                  LOGGER.v("Detect: %s", results);




                    runOnUiThread(
                          new Runnable() {
                            @Override
                            public void run() {
                              showResultsInBottomSheet(results);
                              showFrameInfo(previewWidth + "x" + previewHeight);
                              showCropInfo(imageSizeX + "x" + imageSizeY);
                              showCameraResolution(cropSize + "x" + cropSize);
                              showRotationInfo(String.valueOf(sensorOrientation));
                              showInference(lastProcessingTimeMs + "ms");
                                boolean lightDetected = isObjectDetected(results, "light");
                                boolean fanDetected = isObjectDetected(results, "fan");


                                if (lightDetected) {


                                        Log.i("#data:","light on");
                                        turnOnLight();
                                        flg=1;
                                    }else  {
                                        Log.i("#data:","light off");
                                        turnOffLight();

                                    }

                                if (fanDetected) {


                                    Log.i("#data:","fan on");
                                    turnOnFan();
                                    flg=1;
                                }else  {
                                    Log.i("#data:","fan off");
                                    turnOffFan();

                                }



                            }
                          });
                }
                readyForNextImage();
              }
            });
  }


    private boolean isObjectDetected(List<Classifier.Recognition> results, String objectLabel) {

            float dist=0;
            String output="Unknown";
            if(results!=null)
            {
                for(int i=0;i<results.size();i++)
                {
                    Classifier.Recognition recognition = results.get(i);
                    float conf = 100 * recognition.getConfidence();
                    if(dist<=conf && conf>80.5)
                    {
                        dist=conf;
                        output=recognition.getTitle();

                    }
                }
            }
        if(output.contains(objectLabel))
        {
            return true;
        }

        return false;
    }


    private void turnOffLight() {

        setDevices("L1:off");
        Toast.makeText(this, "light is off", Toast.LENGTH_SHORT).show();
        lightOnMessageDisplayed = false;

    }


  private void turnOnLight() {

    setDevices("L1:on");
    Toast.makeText(this, "light is on", Toast.LENGTH_SHORT).show();
      if (!lightOnMessageDisplayed) {
          Toast.makeText(this, "Light is on", Toast.LENGTH_SHORT).show();
          lightOnMessageDisplayed = true;
      }
      speech.startListening(recognizerIntent);

  }

    private void turnOffFan() {

        setDevices("F1:off");
        Toast.makeText(this, "fan is off", Toast.LENGTH_SHORT).show();
        lightOnMessageDisplayed = false;

    }


    private void turnOnFan() {

        setDevices("F1:on");
        Toast.makeText(this, "fan is on", Toast.LENGTH_SHORT).show();
        if (!lightOnMessageDisplayed) {
            Toast.makeText(this, "fan is on", Toast.LENGTH_SHORT).show();
            lightOnMessageDisplayed = true;
        }
        speech.startListening(recognizerIntent);

    }
  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    final Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateClassifier(model, device, numThreads));
  }

  private void recreateClassifier(Model model, Device device, int numThreads) {
    if (classifier != null) {
      LOGGER.d("Closing classifier.");
      classifier.close();
      classifier = null;
    }
    if (device == Device.GPU && model == Model.QUANTIZED) {
      LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
      runOnUiThread(
          () -> {
            Toast.makeText(this, "GPU does not yet supported quantized models.", Toast.LENGTH_LONG)
                .show();
          });
      return;
    }
    try {
      LOGGER.d(
          "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
      classifier = Classifier.create(this, model, device, numThreads);
    } catch (IOException e) {
      LOGGER.e(e, "Failed to create classifier.");
    }

    // Updates the input image size.
    imageSizeX = classifier.getImageSizeX();
    imageSizeY = classifier.getImageSizeY();
  }

    private class RecognitionListenerImpl implements RecognitionListener {
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String lowerCaseText = matches.get(0).toLowerCase();

                // Check if the user said 'yes' or 'ok' to turn off the light
                if (lowerCaseText.contains("yes") || lowerCaseText.contains("ok")) {
                    //turnOffLight();
                } else {
                    // Continue listening for voice commands
                   // speech.startListening(recognizerIntent);
                }
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }

        // Implement other RecognitionListener methods as needed
        // ...

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
            // Continue listening for voice commands
            speech.startListening(recognizerIntent);
        }

        @Override
        public void onError(int i) {

        }
    }
}
