package pl.shajen.octopus.control;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class ControlService extends IntentService {
    final String TAG = "ControlService";

    public ControlService() {
        super("ControlService");
        Log.d(TAG, "init");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        final String host = intent.getStringExtra("host");
        final String username = intent.getStringExtra("username");
        final String password = intent.getStringExtra("password");

        final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        final MqttAndroidClient mqttClient = new MqttAndroidClient(getApplicationContext(), host, "octopus");
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d(TAG, "connect complete");
                subscribe(mqttClient, "/device/+/status/#", 0);
                subscribe(mqttClient, "/device/+/sensor/#", 0);
                subscribe(mqttClient, "/device/+/event/#", 0);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.d(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                onMessageHandler(topic, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

        try {
            mqttClient.connect(mqttConnectOptions);
        } catch (MqttException e) {
            Log.w(TAG, "exception during connect");
            Log.w(TAG, e.toString());
        }

        try {
            while (true) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void onMessageHandler(String topic, String message) {
        Log.d(TAG, "new message");
        Log.d(TAG, topic);
        Log.d(TAG, message);
        try {
            JSONObject jsonMessage = new JSONObject(message);
        } catch (JSONException e) {
            Log.w(TAG, "exception during JSON parse message");
            Log.w(TAG, e.toString());
        }
    }

    private void subscribe(MqttAndroidClient client, final String topic, int qos) {
        try {
            IMqttToken token = client.subscribe(topic, qos);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "subscribe successfully " + topic);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.e(TAG, "subscribe failed " + topic);
                }
            });
        } catch (MqttException e) {
            Log.w(TAG, "exception during subscribe");
            Log.w(TAG, e.toString());
        }
    }
}
