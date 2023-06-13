package com.skycompass;

import androidx.lifecycle.ViewModel;

public class CompassFragmentViewModel extends ViewModel {

    private boolean rotateToNorth;
    private float targetRotation;

    public boolean isRotateToNorth() {
        return rotateToNorth;
    }

    public boolean getRotateToNorth() {
        return rotateToNorth;
    }

    public void setRotateToNorth(boolean rotateToNorth) {
        this.rotateToNorth = rotateToNorth;
    }

    public float getTargetRotation() {
        return targetRotation;
    }

    public void setTargetRotation(float targetRotation) {
        this.targetRotation = targetRotation;
    }

}
