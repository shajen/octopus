package pl.shajen.octopus.activities;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.adapters.DevicePinsAdapter;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;
import pl.shajen.octopus.tasks.DeviceRequestTask;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;

public class RemoteSocketActivity extends AppCompatActivity implements DeviceRequestTask.DeviceRequestResponse {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_socket);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            final Device device = new Device(b.getString(DEVICE_ACTIVITY_KEY));
            new DeviceRequestTask(this, this, new NetworkTools(this), device.ip()).execute("GPIO/");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close:
                finishAffinity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void processFinish(List<String> responses) {
        boolean isError = responses.isEmpty();
        for (final String response : responses) {
            try {
                JSONObject json = new JSONObject(response);
                final int status = json.getInt("status");
                if (status != 0) {
                    isError |= true;
                } else if (json.has("data")) {
                    processData(json.getJSONObject("data"));
                }
            } catch (Exception ex) {
                isError |= true;
            }
        }
        if (isError) {
            Toast.makeText(this, getString(R.string.TASK_ERROR), Toast.LENGTH_LONG).show();
        }
    }

    private void processData(JSONObject data) {
        final List<Pair<Integer, Boolean>> list = parseData(data);
        final TextView textView = (TextView) findViewById(R.id.devicePintextView);
        textView.setText(getString(R.string.FOUND_PINS, list.size()));

        final Bundle b = getIntent().getExtras();
        if (b != null) {
            final Device device = new Device(b.getString(DEVICE_ACTIVITY_KEY));
            final ListView listview = (ListView) findViewById(R.id.devicePinListView);
            final ArrayAdapter<Pair<Integer, Boolean>> adapter = new DevicePinsAdapter(this, this, list, device.ip());
            listview.setAdapter(adapter);
        }
    }

    private List<Pair<Integer, Boolean>> parseData(JSONObject data) {
        List<Pair<Integer, Boolean>> list = new ArrayList<>();
        if (data != null) {
            for (Iterator<String> i = data.keys(); i.hasNext(); ) {
                String key = i.next();
                try {
                    list.add(new Pair<>(Integer.parseInt(key), data.getInt(key) == 1));
                } catch (Exception ex) {
                }
            }
        }
        Collections.sort(list, new Comparator<Pair<Integer, Boolean>>() {
            @Override
            public int compare(Pair<Integer, Boolean> p1, Pair<Integer, Boolean> p2) {
                return p1.first.compareTo(p2.first);
            }
        });
        return list;
    }
}
