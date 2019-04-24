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

        final MessageParser messageParser = new MessageParser(this);
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
                messageParser.onMessageHandler(topic, new String(mqttMessage.getPayload()));
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
