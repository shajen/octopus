package pl.shajen.octopus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import pl.shajen.octopus.tasks.ScanTask;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICES_PREFS_KEY;
import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;
import static pl.shajen.octopus.constants.SettingsConstant.PREFS_NAME;

public class SearchDevicesActivity extends AppCompatActivity implements ScanTask.ScanTaskResponse {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);

        final Set<String> devices = getSharedPreferences(PREFS_NAME, 0).getStringSet(DEVICES_PREFS_KEY, new HashSet<String>());
        setDevices(devices);
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
                if (!networkTools.isInternet()) {
                    Toast.makeText(this, getString(R.string.NO_INTERNET_CONNECTION), Toast.LENGTH_LONG).show();
                } else {
                    new ScanTask(this, this, networkTools).execute();
                }
                return true;
            case R.id.close:
                finishAffinity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDevices(Set<String> devices) {
        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getString(R.string.FOUND_DEVICES, devices.size()));
        final ListView listview = (ListView) findViewById(R.id.listView);
        List<String> list = new ArrayList<>();
        list.addAll(devices);
        Collections.sort(list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                Intent in = new Intent(getApplicationContext(),DeviceActivity.class);
                Bundle b = new Bundle();
                b.putString(DEVICE_ACTIVITY_KEY, item);
                in.putExtras(b);
                startActivity(in);
            }
        });
    }

    @Override
    public void processFinish(Set<String> devices) {
        final SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putStringSet(DEVICES_PREFS_KEY, devices);
        editor.commit();
        setDevices(devices);
    }
}
