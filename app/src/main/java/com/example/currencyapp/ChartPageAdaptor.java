package com.example.currencyapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ChartPageAdaptor  extends FragmentStateAdapter {



    public ChartPageAdaptor(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0 : return CustomChartFragment.newInstance("RON");

            case 1 : return CustomChartFragment.newInstance("EUR");

            case 2 : return CustomChartFragment.newInstance("USD");

            default : return null;

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
