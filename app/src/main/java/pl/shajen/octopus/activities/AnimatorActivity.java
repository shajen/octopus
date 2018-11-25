package pl.shajen.octopus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
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
        List<Pair<String, String>> tasks = new LinkedList<>();
        tasks.add(new Pair<>(key, value));
        sendTask(device, tasks);
    }

    private void sendTask(Device device, List<Pair<String, String>> data) {
        final List<String> tasks = new LinkedList<>();
        for (Pair<String, String> d : data) {
            final String url = String.format("/ANIMATOR/SET?KEY=%s&VALUE=%s", d.first, d.second);
            tasks.add(url);
        }
        new DeviceRequestTask(this, this, new NetworkTools(this), device.ip(), true).execute(tasks.toArray(new String[tasks.size()]));
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

    private void setData(Device device) {
        final List<Pair<String, String>> data = new LinkedList<>();

        final NumberPicker secondsNumberPicker = findViewById(R.id.secondsNumberPicker);
        final NumberPicker ledsNumberPicker = findViewById(R.id.ledsNumberPicker);
        final NumberPicker speedNumberPicker = findViewById(R.id.speedNumberPicker);
        final NumberPicker animationsNumberPicker = findViewById(R.id.animationsNumberPicker);

        data.add(new Pair<>("SECONDS_PER_ANIMATION", String.valueOf(secondsNumberPicker.getValue())));
        data.add(new Pair<>("LEDS", String.valueOf(ledsNumberPicker.getValue())));
        data.add(new Pair<>("SPEED", String.valueOf(speedNumberPicker.getValue())));
        data.add(new Pair<>("ANIMATIONS", String.valueOf(animationsNumberPicker.getValue())));

        sendTask(device, data);
    }

    private void selectColor() {
        final View view = findViewById(android.R.id.content);
        final Button button = findViewById(R.id.colorButton);
        final int color = button.getCurrentTextColor();

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
                        button.setBackgroundColor(color);
                        button.setTextColor(color);
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

            powerSwitch.setChecked(data.getString("powered_on").equals("1"));
            secondsNumberPicker.setValue(Integer.parseInt(data.getString("seconds_per_animation")));
            ledsNumberPicker.setValue(Integer.parseInt(data.getString("leds")));
            speedNumberPicker.setValue(Integer.parseInt(data.getString("speed")));
            animationsNumberPicker.setValue(Integer.parseInt(data.getString("animation")) + 1);
        } catch (Exception ex) {
        }
    }

}
