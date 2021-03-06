package pl.shajen.octopus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;
import pl.shajen.octopus.tasks.ScanTask;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICES_PREFS_KEY;
import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;
import static pl.shajen.octopus.constants.SettingsConstant.PREFS_NAME;

public class SearchDevicesActivity extends AppCompatActivity implements ScanTask.ScanTaskResponse {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        setDevices(readDevices());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                NetworkTools networkTools = new NetworkTools(this);
                if (networkTools.isAnyInterfaceAvailable()) {
                    new ScanTask(this, this, networkTools).execute();
                } else {
                    Toast.makeText(this, getText(R.string.NOT_FOUND_ANY_INTERFACE), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.clear:
                final SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
                editor.remove(DEVICES_PREFS_KEY);
                editor.commit();
                setDevices(readDevices());
                return true;
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            case R.id.close:
                finishAffinity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Set<Device> readDevices() {
        Set<Device> devices = new HashSet<>();
        for (String packedDevice : getSharedPreferences(PREFS_NAME, 0).getStringSet(DEVICES_PREFS_KEY, new HashSet<String>())) {
            devices.add(new Device(packedDevice));
        }
        return devices;
    }

    private void writeDevices(Set<Device> devices) {
        Set<String> packedDevices = new HashSet<>();
        for (Device device : devices) {
            packedDevices.add(device.toPackedString());
        }
        final SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putStringSet(DEVICES_PREFS_KEY, packedDevices);
        editor.commit();
    }

    private void setDevices(Set<Device> devices) {
        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getString(R.string.FOUND_DEVICES, devices.size()));
        final ListView listview = (ListView) findViewById(R.id.listView);
        List<Device> list = new ArrayList<>();
        list.addAll(devices);
        Collections.sort(list);
        final ArrayAdapter<Device> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                NetworkTools networkTools = new NetworkTools(getApplicationContext());
                if (!networkTools.isAnyInterfaceAvailable()) {
                    Toast.makeText(getApplicationContext(), getText(R.string.NOT_FOUND_ANY_INTERFACE), Toast.LENGTH_SHORT).show();
                    return;
                }
                final Device item = (Device) parent.getItemAtPosition(position);
                if (item.controller().equals("Animator")) {
                    startActivity(AnimatorActivity.class, item);
                } else if (item.controller().equals("Sonoff")) {
                    startActivity(RemoteSocketActivity.class, item);
                } else if (item.controller().equals("Remote Socket")) {
                    startActivity(RemoteSocketActivity.class, item);
                } else if (item.controller().equals("Thermostat")) {
                    startActivity(ThermostatActivity.class, item);
                }
            }
        });
    }

    private void startActivity(Class<?> cls, Device device) {
        final Bundle b = new Bundle();
        b.putString(DEVICE_ACTIVITY_KEY, device.toPackedString());
        final Intent in = new Intent(getApplicationContext(), cls);
        in.putExtras(b);
        startActivity(in);
    }

    @Override
    public void processFinish(Set<Device> devices) {
        for (final Device oldDevice : readDevices()) {
            boolean alreadyAdded = false;
            for (final Device newDevice : devices) {
                if (oldDevice.ip().equals(newDevice.ip())) {
                    alreadyAdded = true;
                }
            }
            if (!alreadyAdded) {
                devices.add(oldDevice);
            }
        }
        writeDevices(devices);
        setDevices(devices);
    }
}
