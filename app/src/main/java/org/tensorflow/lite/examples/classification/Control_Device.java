package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class Control_Device extends AppCompatActivity {
    TextView text;
    Button onlight, offlight, onfan, offfan, onlight1, offlight1, onfan1, offfan1;

    MqttAndroidClient client;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controldevice);

        text = findViewById(R.id.lighttext);
        onlight = findViewById(R.id.btn_on);
        offlight = findViewById(R.id.btn_off);
        onfan = findViewById(R.id.btn_onfan);
        offfan = findViewById(R.id.btn_offfan);
        onlight1 = findViewById(R.id.btn_onlight1);
        offlight1 = findViewById(R.id.btn_offligth1);
        onfan1 = findViewById(R.id.btn_onfan1);
        offfan1 = findViewById(R.id.btn_offfan1);

        String clientId = MqttClient.generateClientId();

        // Button Click Listeners for controlling light
        onlight.setOnClickListener(view -> {
            if (!isLightOn()) {
                text.setText("Light 1 is ON");
                text.setTextColor(Color.parseColor("#FF9800"));
                setDevices("L1:on");
            } else {
                Toast.makeText(getApplicationContext(), "Light is already ON", Toast.LENGTH_SHORT).show();
            }
        });

        offlight.setOnClickListener(view -> {
            if (isLightOn()) {
                text.setText("Light 1 is OFF");
                text.setTextColor(Color.parseColor("#CCFF0000"));
                setDevices("L1:off");
            } else {
                Toast.makeText(getApplicationContext(), "Light is already OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // Button Click Listeners for controlling fan
        onfan.setOnClickListener(view -> {
            if (!isFanOn()) {
                text.setText("Fan 1 is ON");
                text.setTextColor(Color.parseColor("#00FF00"));
                setDevices("F1:on");
            } else {
                Toast.makeText(getApplicationContext(), "Fan is already ON", Toast.LENGTH_SHORT).show();
            }
        });

        offfan.setOnClickListener(view -> {
            if (isFanOn()) {
                text.setText("Fan 1 is OFF");
                text.setTextColor(Color.parseColor("#FF0000"));
                setDevices("F1:off");
            } else {
                Toast.makeText(getApplicationContext(), "Fan is already OFF", Toast.LENGTH_SHORT).show();
            }
        });

        // Button Click Listeners for additional light and fan controls
        onlight1.setOnClickListener(view -> {
            if (!isLightOn()) {
                text.setText("Light 2 is ON");
                text.setTextColor(Color.parseColor("#FF9800"));
                setDevices("L2:on");
            } else {
                Toast.makeText(getApplicationContext(), "Light is already ON", Toast.LENGTH_SHORT).show();
            }
        });

        offlight1.setOnClickListener(view -> {
            if (isLightOn()) {
                text.setText("Light 2 is OFF");
                text.setTextColor(Color.parseColor("#CCFF0000"));
                setDevices("L2:off");
            } else {
                Toast.makeText(getApplicationContext(), "Light is already OFF", Toast.LENGTH_SHORT).show();
            }
        });

        onfan1.setOnClickListener(view -> {
            if (!isFanOn()) {
                text.setText("Fan 2 is ON");
                text.setTextColor(Color.parseColor("#00FF00"));
                setDevices("F2:on");
            } else {
                Toast.makeText(getApplicationContext(), "Fan is already ON", Toast.LENGTH_SHORT).show();
            }
        });

        offfan1.setOnClickListener(view -> {
            if (isFanOn()) {
                text.setText("Fan 2 is OFF");
                text.setTextColor(Color.parseColor("#CCFF0000"));
                setDevices("F2:off");
            } else {
                Toast.makeText(getApplicationContext(), "Fan is already OFF", Toast.LENGTH_SHORT).show();
            }
        });

        client = new MqttAndroidClient(this, "tcp://broker.hivemq.com", clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Control_Device.this, "connected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Control_Device.this, "connection failed!!", Toast.LENGTH_SHORT).show();
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

    private boolean isLightOn() {
        return false;
    }


    private boolean isFanOn() {
        return false;
    }


    public void setDevices(String msg) {
        try {
            client.publish("ARHOME/devices", msg.getBytes(), 0, false);
        } catch (Exception ex) {
            Toast.makeText(this, String.valueOf(ex), Toast.LENGTH_LONG).show();
        }
    }
}
