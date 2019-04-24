package pl.shajen.octopus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import pl.shajen.octopus.control.ControlAggregatorFragment;
import pl.shajen.octopus.control.ControlService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(Constants.CONTROL_AGGREGATOR_FRAGMENT);
        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new ControlAggregatorFragment();
            ft.add(android.R.id.content, fragment, Constants.CONTROL_AGGREGATOR_FRAGMENT);
            ft.commit();
        }

        Intent intent = new Intent(this, ControlService.class);
        intent.putExtra(Constants.HOST, "tcp://mqtt.shajen.pl:3380");
        intent.putExtra(Constants.USERNAME, "test");
        intent.putExtra(Constants.PASSWORD, "azerty123");
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.control_menu, menu);
        return true;
    }
}
