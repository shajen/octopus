package pl.shajen.octopus.control.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import pl.shajen.octopus.Constants;

public abstract class BaseControlFragment extends Fragment {
    private final String TAG = "BaseControlFragment";
    private final BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.MESSAGE_BROADCAST_ACTION)) {
                final String topic = intent.getStringExtra(Constants.TOPIC);
                final String message = intent.getStringExtra(Constants.MESSAGE);
                try {
                    onMessageHandler(topic, new JSONObject(message));
                } catch (JSONException e) {
                    Log.w(TAG, "exception during JSON parse message");
                    Log.w(TAG, e.toString());
                }
            }
        }
    };

    abstract public CharSequence title();

    protected void onMessageHandler(String topic, JSONObject message) {
        Log.d(TAG, "new message");
        Log.d(TAG, topic);
        Log.d(TAG, message.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resume");
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.registerReceiver(m_broadcastReceiver, new IntentFilter(Constants.MESSAGE_BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "pause");
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.unregisterReceiver(m_broadcastReceiver);
    }
}
