package pl.shajen.octopus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;
import pl.shajen.octopus.tasks.DeviceRequestTask;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;

public class AnimatorActivity extends AppCompatActivity implements DeviceRequestTask.DeviceRequestResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animator);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            final Device device = new Device(b.getString(DEVICE_ACTIVITY_KEY));

            final TextView deviceTeypeTextView = (TextView) findViewById(R.id.deviceTeypeTextView);
            deviceTeypeTextView.setText(device.toString());

            final Button offButton = (Button) findViewById(R.id.animatorOffButton);
            offButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendTask(device.ip(), "POWERED_ON", "0");
                }
            });

            final Button onButton = (Button) findViewById(R.id.animatorOnButton);
            onButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendTask(device.ip(), "POWERED_ON", "1");
                }
            });

            final NumberPicker speedNumberPicker = (NumberPicker) findViewById(R.id.speedNumberPicker);
            speedNumberPicker.setMinValue(1);
            speedNumberPicker.setMaxValue(100);
            speedNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    sendTask(device.ip(), "SPEED", Integer.toString(newVal));
                }
            });

            final NumberPicker animationsNumberPicker = (NumberPicker) findViewById(R.id.animationsNumberPicker);
            animationsNumberPicker.setMinValue(0);
            animationsNumberPicker.setMaxValue(3);
            animationsNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    sendTask(device.ip(), "ANIMATION", Integer.toString(newVal - 1));
                }
            });

            new DeviceRequestTask(this, this, new NetworkTools(this), device.ip(), true).execute("/ANIMATOR/");
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

    private void sendTask(String deviceIp, String key, String value) {
        final String url = String.format("/ANIMATOR/SET?KEY=%s&VALUE=%s", key, value);
        new DeviceRequestTask(this, this, new NetworkTools(this), deviceIp, true).execute(url);
    }

    @Override
    public void processFinish(List<String> responses) {
        boolean isError = responses.isEmpty();
        for (final String response : responses) {
            try {
                processData(new JSONObject(response));
            } catch (Exception ex) {
                isError |= true;
            }
        }
        if (isError) {
            Toast.makeText(this, getString(R.string.TASK_ERROR), Toast.LENGTH_LONG).show();
        }
    }

    private void updateEditText(EditText editText, String value) {
        if (!editText.getText().toString().equals(value)) {
            editText.setText(value);
        }
    }

    private void processData(JSONObject data) {
        try {
            final ImageView stateIcon = (ImageView) findViewById(R.id.animatorIconPoweredOn);
            final NumberPicker speedNumberPicker = (NumberPicker) findViewById(R.id.speedNumberPicker);
            final NumberPicker animationsNumberPicker = (NumberPicker) findViewById(R.id.animationsNumberPicker);

            if (data.getString("powered_on").equals("1")) {
                stateIcon.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                stateIcon.setImageResource(android.R.drawable.btn_star_big_off);
            }

            speedNumberPicker.setValue(Integer.parseInt(data.getString("speed")));
            animationsNumberPicker.setValue(Integer.parseInt(data.getString("animation")) + 1);
        } catch (Exception ex) {
        }
    }

}
