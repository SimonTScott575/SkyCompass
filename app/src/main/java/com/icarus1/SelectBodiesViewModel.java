package com.icarus1;

import androidx.lifecycle.ViewModel;

import com.icarus1.compass.CelestialObject;

public class SelectBodiesViewModel extends ViewModel {

    private final boolean[] viewChecks;

    public SelectBodiesViewModel() {
        viewChecks = new boolean[CelestialObject.values().length];
    }

    public boolean getViewable(CelestialObject body) {
        return viewChecks[body.ordinal()];
    }

    public void setViewable(CelestialObject body, boolean check) {
        viewChecks[body.ordinal()] = check;
    }

}