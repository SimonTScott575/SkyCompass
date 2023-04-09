package com.icarus1.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventCallback;
import android.hardware.SensorManager;

import com.icarus1.util.Debug;

public class CompassSensor {

    private SensorManager sensorManager;
    private final SensorEventCallback sensorListener = new SensorListener();
    private OnOrientationChanged onOrientationChanged;

    public CompassSensor() {
    }

    public CompassSensor(OnOrientationChanged onOrientationChanged) {
        this.onOrientationChanged = onOrientationChanged;
    }

    public void setOnOrientationChanged(OnOrientationChanged onOrientationChanged) {
        this.onOrientationChanged = onOrientationChanged;
    }

    public boolean requested() {
        return sensorManager != null;
    }

    public void request(Context context) {

        if (requested()) {
            throw new RuntimeException("Sensor already requested without being destroyed.");
        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (magneticSensor != null && accelerometerSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                magneticSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            );
            sensorManager.registerListener(
                sensorListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            );
        } else {
            Debug.log("Sensor no accessed.");
        }

    }

    public void destroy() {

        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
            sensorManager = null;
        }

    }

    private class SensorListener extends SensorEventCallback {

        float[] gravity = new float[3];
        float[] geomagnetic = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values;

            if (gravity != null && geomagnetic != null) {

                float[] R = new float[9];
                float[] I = new float[9];

                if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {

                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    if (onOrientationChanged != null) {
                        onOrientationChanged.onOrientationChanged(orientation);
                    }

                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

    public interface OnOrientationChanged {
        void onOrientationChanged(float[] orientation);
    }

}
