package pl.shajen.octopus.activities;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;
import pl.shajen.octopus.tasks.DeviceRequestTask;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;

public class ThermostatActivity extends AppCompatActivity implements DeviceRequestTask.DeviceRequestResponse {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermostat);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            final Device device = new Device(b.getString(DEVICE_ACTIVITY_KEY));

            final TextView deviceTeypeTextView = findViewById(R.id.deviceTeypeTextView);
            deviceTeypeTextView.setText(device.toString());

            final Button setButton = findViewById(R.id.setButton);
            setButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setData(device);
                }
            });

            final Button resetButton = findViewById(R.id.resetButton);
            resetButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    refresh(device);
                }
            });

            refresh(device);
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
        try {
            final DecimalFormat df = new DecimalFormat("#.##");
            final RadioButton cooler = findViewById(R.id.coolerRadioButton);
            final RadioButton heater = findViewById(R.id.heaterRadioButton);
            final EditText desiredTemperature = findViewById(R.id.desiredTemperatureEditText);
            final EditText hysteresis = findViewById(R.id.hysteresisEditText);
            final TextView currentTemperature = findViewById(R.id.currentTemperatureTextView);

            if (data.getBoolean("is_heater")) {
                heater.toggle();
            } else {
                cooler.toggle();
            }
            desiredTemperature.setText(df.format(data.getDouble("desired_temperature")));
            hysteresis.setText(df.format(data.getDouble("hysteresis")));
            currentTemperature.setText(df.format(data.getDouble("current_temperature")));
        } catch (JSONException ex) {
            Log.e("exception", ex.toString());
        }
    }

    private void sendTask(Device device, List<Pair<String, String>> values) {
        String url = String.format("/THERMOSTAT/SET?");
        for (Pair<String, String> value : values) {
            url += String.format("&%s=%s", value.first, value.second);
        }
        new DeviceRequestTask(this, this, new NetworkTools(this), device.ip()).execute(url);
    }

    private void setData(Device device) {
        final RadioButton heater = findViewById(R.id.heaterRadioButton);
        final EditText desiredTemperature = findViewById(R.id.desiredTemperatureEditText);
        final EditText hysteresis = findViewById(R.id.hysteresisEditText);

        List<Pair<String, String>> values = new LinkedList<>();
        values.add(new Pair<String, String>("is_heater", String.valueOf(heater.isChecked())));
        values.add(new Pair<String, String>("desired_temperature", desiredTemperature.getText().toString()));
        values.add(new Pair<String, String>("hysteresis", hysteresis.getText().toString()));

        sendTask(device, values);
    }

    private void refresh(Device device) {
        new DeviceRequestTask(this, this, new NetworkTools(this), device.ip()).execute("/THERMOSTAT/");
    }
}
