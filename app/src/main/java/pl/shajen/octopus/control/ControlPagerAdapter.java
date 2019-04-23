package pl.shajen.octopus.control;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import pl.shajen.octopus.control.ui.BaseControlFragment;
import pl.shajen.octopus.control.ui.DevicesFragment;
import pl.shajen.octopus.control.ui.GpioFragment;
import pl.shajen.octopus.control.ui.PinSchedulerFragment;
import pl.shajen.octopus.control.ui.ThermometerFragment;
import pl.shajen.octopus.control.ui.ThermostatFragment;
import pl.shajen.octopus.control.ui.Ws2812Fragment;

public class ControlPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<BaseControlFragment> m_fragments = new ArrayList<>();

    public ControlPagerAdapter(FragmentManager fm) {
        super(fm);
        m_fragments.add(new DevicesFragment());
        m_fragments.add(new GpioFragment());
        m_fragments.add(new PinSchedulerFragment());
        m_fragments.add(new ThermometerFragment());
        m_fragments.add(new ThermostatFragment());
        m_fragments.add(new Ws2812Fragment());
    }

    @Override
    public Fragment getItem(int position) {
        return m_fragments.get(position);
    }

    @Override
    public int getCount() {
        return m_fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return m_fragments.get(position).title();
    }
}
