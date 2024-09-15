package com.skycompass;

import androidx.lifecycle.ViewModel;

public class MainViewModel2 extends ViewModel {

    public enum FragmentView {
        NONE,
        COMPASS,
        INFO
    }

    public enum OptionsFragment {
        INFO,
        MAP,
        CALENDAR,
        CLOCK
    }

    public FragmentView currentFragment = FragmentView.NONE;
    public OptionsFragment currentOption = OptionsFragment.INFO;

}
