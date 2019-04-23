package pl.shajen.octopus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import pl.shajen.octopus.control.ControlAggregatorFragment;

public class MainActivity extends AppCompatActivity {
    Fragment m_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        m_fragment = fm.findFragmentByTag("control_fragment");
        if (m_fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            m_fragment = new ControlAggregatorFragment();
            ft.add(android.R.id.content, m_fragment, "control_fragment");
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.control_menu, menu);
        return true;
    }
}
