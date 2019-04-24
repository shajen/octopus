package pl.shajen.octopus.control;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.shajen.octopus.R;

public class ControlAggregatorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.control_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ControlPagerAdapter pagerAdapter = new ControlPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
    }
}
