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
            new DeviceRequestTask(this, this, new NetworkTools(this), device.ip(), true).execute("/ANIMATOR/");

            final TextView deviceTeypeTextView = (TextView) findViewById(R.id.deviceTeypeTextView);
            deviceTeypeTextView.setText(device.toString());

            final Button offButton = (Button) findViewById(R.id.animatorOffButton);
            offButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendTask(device.ip(), "/ANIMATOR/SET?KEY=POWERED_ON&VALUE=0");
                }
            });

            final Button onButton = (Button) findViewById(R.id.animatorOnButton);
            onButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendTask(device.ip(), "/ANIMATOR/SET?KEY=POWERED_ON&VALUE=1");
                }
            });
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

    private void sendTask(String deviceIp, String resource) {
        new DeviceRequestTask(this, this, new NetworkTools(this), deviceIp, true).execute(resource);
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

    private void processData(JSONObject data) {
        try {
            final ImageView stateIcon = (ImageView) findViewById(R.id.animatorIconPoweredOn);
            final EditText speedEditText = (EditText) findViewById(R.id.speedEditText);
            final EditText animatiosEditText = (EditText) findViewById(R.id.animatiosEditText);

            if (data.getString("powered_on").equals("1")) {
                stateIcon.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                stateIcon.setImageResource(android.R.drawable.btn_star_big_off);
            }

            speedEditText.setText(data.getString("speed"));
            animatiosEditText.setText(data.getString("animation"));
        } catch (Exception ex) {
        }
    }

}
