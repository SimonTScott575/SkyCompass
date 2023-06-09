package com.skycompass;

import androidx.lifecycle.ViewModel;

public class CompassFragmentViewModel extends ViewModel {

    private boolean rotateToNorth;
    private float northRotation;

    public boolean isRotateToNorth() {
        return rotateToNorth;
    }

    public boolean getRotateToNorth() {
        return rotateToNorth;
    }

    public void setRotateToNorth(boolean rotateToNorth) {
        this.rotateToNorth = rotateToNorth;
    }

    public float getNorthRotation() {
        return northRotation;
    }

    public void setNorthRotation(float northRotation) {
        this.northRotation = northRotation;
    }

}
