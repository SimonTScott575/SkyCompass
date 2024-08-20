package com.skycompass;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    public enum FragmentView {
        NONE,
        COMPASS,
        INFO
    }

    public FragmentView currentFragment = FragmentView.NONE;
}
