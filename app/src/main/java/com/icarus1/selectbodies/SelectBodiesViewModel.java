package com.icarus1.selectbodies;

import androidx.lifecycle.ViewModel;

import com.icarus1.compass.CelestialBody;

public class SelectBodiesViewModel extends ViewModel {

    private final boolean[] viewChecks;

    public SelectBodiesViewModel() {
        viewChecks = new boolean[CelestialBody.values().length];
    }

    public boolean getCheck(CelestialBody body) {
        return viewChecks[body.getIndex()];
    }

    public void setView(CelestialBody body, boolean check) {
        viewChecks[body.getIndex()] = check;
    }

}