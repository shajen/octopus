package pl.shajen.octopus.control;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Queue;

import pl.shajen.octopus.Constants;

public class MessageParser {
    private final String TAG = "MessageParser";
    private final Context m_context;
    final Queue<Pair<String, JSONObject>> m_lastMessages = new ArrayDeque<>();

    public MessageParser(Context context) {
        m_context = context;
    }

    public void onMessageHandler(String topic, String message) {
        Log.d(TAG, "new message");
        Log.d(TAG, topic);
        Log.d(TAG, message);
        try {
            JSONObject jsonMessage = new JSONObject(message);
            m_lastMessages.add(new Pair(topic, jsonMessage));
            if (m_lastMessages.size() > 1000) {
                m_lastMessages.poll();
            }
        } catch (JSONException e) {
            Log.w(TAG, "exception during JSON parse message");
            Log.w(TAG, e.toString());
        }

        Intent intent = new Intent(Constants.MESSAGE_BROADCAST_ACTION);
        intent.putExtra(Constants.TOPIC, topic);
        intent.putExtra(Constants.MESSAGE, message);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(m_context);
        bm.sendBroadcast(intent);
    }
}
