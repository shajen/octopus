package pl.shajen.octopus.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import pl.shajen.octopus.R;
import pl.shajen.octopus.helper.NetworkTools;
import pl.shajen.octopus.models.Device;
import pl.shajen.octopus.tasks.DeviceRequestTask;
import top.defaults.colorpicker.ColorPickerPopup;

import static pl.shajen.octopus.constants.SettingsConstant.DEVICE_ACTIVITY_KEY;

public class AnimatorActivity extends AppCompatActivity implements DeviceRequestTask.DeviceRequestResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animator);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            final NumberPicker secondsNumberPicker = findViewById(R.id.secondsNumberPicker);
            secondsNumberPicker.setMinValue(5);
            secondsNumberPicker.setMaxValue(60 * 60 * 24);

            final NumberPicker ledsNumberPicker = findViewById(R.id.ledsNumberPicker);
            ledsNumberPicker.setMinValue(1);
            ledsNumberPicker.setMaxValue(200);

            final NumberPicker speedNumberPicker = findViewById(R.id.speedNumberPicker);
            speedNumberPicker.setMinValue(1);
            speedNumberPicker.setMaxValue(100);

            final NumberPicker animationsNumberPicker = findViewById(R.id.animationsNumberPicker);
            animationsNumberPicker.setMinValue(0);
            animationsNumberPicker.setMaxValue(3);

            final Button colorButton = findViewById(R.id.colorButton);
            colorButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    selectColor();
                }
            });

            final Device device = new Device(b.getString(DEVICE_ACTIVITY_KEY));

            final Switch powerSwitch = findViewById(R.id.powerSwitch);
            powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sendTask(device, "POWERED_ON", isChecked ? "1" : "0");
                }
            });

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

    private void refresh(Device device) {
        new DeviceRequestTask(this, this, new NetworkTools(this), device.ip(), true).execute("/ANIMATOR/");
    }

    private void sendTask(Device device, String key, String value) {
        final List<String> keys = new LinkedList<>();
        final List<String> values = new LinkedList<>();
        keys.add(key);
        values.add(value);
        sendTask(device, keys, values);
    }

    private void sendTask(Device device, List<String> keys, List<String> values) {
        final String keysString = TextUtils.join(",", keys);
        final String valuesString = TextUtils.join(",", values);
        final String url = String.format("/ANIMATOR/SET?KEY=%s&VALUE=%s", keysString, valuesString);
        new DeviceRequestTask(this, this, new NetworkTools(this), device.ip(), true).execute(url);
    }

    @Override
    public void processFinish(List<String> responses) {
        boolean isError = responses.isEmpty();
        for (final String response : responses) {
            try {
                processData(new JSONObject(response));
            } catch (JSONException ex) {
                isError |= true;
            }
        }
        if (isError) {
            Toast.makeText(this, getString(R.string.TASK_ERROR), Toast.LENGTH_LONG).show();
        }
    }

    private void setData(Device device) {
        final List<String> keys = new LinkedList<>();
        final List<String> values = new LinkedList<>();

        final NumberPicker secondsNumberPicker = findViewById(R.id.secondsNumberPicker);
        final NumberPicker ledsNumberPicker = findViewById(R.id.ledsNumberPicker);
        final NumberPicker speedNumberPicker = findViewById(R.id.speedNumberPicker);
        final NumberPicker animationsNumberPicker = findViewById(R.id.animationsNumberPicker);
        final Switch useColorSwitch = findViewById(R.id.useColorSwitch);
        final Button button = findViewById(R.id.colorButton);
        final String color = String.format("%06X", 0xFFFFFF & button.getCurrentTextColor());

        keys.add("SECONDS_PER_ANIMATION");
        values.add(String.valueOf(secondsNumberPicker.getValue()));
        keys.add("LEDS");
        values.add(String.valueOf(ledsNumberPicker.getValue()));
        keys.add("SPEED");
        values.add(String.valueOf(speedNumberPicker.getValue()));
        keys.add("ANIMATION");
        values.add(String.valueOf(animationsNumberPicker.getValue() - 1));
        keys.add("USE_COLOR");
        values.add(useColorSwitch.isChecked() ? "1" : "0");
        keys.add("COLOR");
        values.add(color);

        sendTask(device, keys, values);
    }

    private void selectColor() {
        final View view = findViewById(android.R.id.content);
        final Button colorButton = findViewById(R.id.colorButton);
        final int color = colorButton.getCurrentTextColor();

        new ColorPickerPopup.Builder(this)
                .initialColor(color)
                .enableBrightness(true)
                .enableAlpha(false)
                .okTitle(getString(R.string.CHOOSE))
                .cancelTitle(getString(R.string.CANCEL))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(view, new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
                        colorButton.setBackgroundColor(color);
                        colorButton.setTextColor(color);
                    }

                    @Override
                    public void onColor(int color, boolean fromUser) {

                    }
                });
    }

    private void processData(JSONObject data) {
        try {
            final Switch powerSwitch = findViewById(R.id.powerSwitch);
            final NumberPicker secondsNumberPicker = findViewById(R.id.secondsNumberPicker);
            final NumberPicker ledsNumberPicker = findViewById(R.id.ledsNumberPicker);
            final NumberPicker speedNumberPicker = findViewById(R.id.speedNumberPicker);
            final NumberPicker animationsNumberPicker = findViewById(R.id.animationsNumberPicker);
            final Switch useColorSwitch = findViewById(R.id.useColorSwitch);
            final Button colorButton = findViewById(R.id.colorButton);
            final int color = Color.parseColor("#" + data.getString("color"));

            powerSwitch.setChecked(data.getString("powered_on").equals("1"));
            secondsNumberPicker.setValue(Integer.parseInt(data.getString("seconds_per_animation")));
            ledsNumberPicker.setValue(Integer.parseInt(data.getString("leds")));
            speedNumberPicker.setValue(Integer.parseInt(data.getString("speed")));
            animationsNumberPicker.setValue(Integer.parseInt(data.getString("animation")) + 1);
            useColorSwitch.setChecked(data.getString("use_color").equals("1"));
            colorButton.setBackgroundColor(color);
            colorButton.setTextColor(color);
        } catch (JSONException ex) {
        }
    }

}
