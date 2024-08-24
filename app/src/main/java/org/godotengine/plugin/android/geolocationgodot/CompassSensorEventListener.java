package org.godotengine.plugin.android.geolocationgodot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.godotengine.godot.Godot;

import java.time.LocalTime;
import java.util.Objects;

public class CompassSensorEventListener implements SensorEventListener {

    private static final int TIME_GAP_MS = 200;

    private final GeoLocationAndroidGodot godotPlugin;
    private final Sensor magnetometer;
    private final Sensor accelerometer;
    private final SensorManager sensorManager;

    private LocalTime lastUpdate = LocalTime.now().minusMinutes(1);
    private float azimuthInRadians = 0.0F;

    public CompassSensorEventListener(final GeoLocationAndroidGodot godotPlugin, final Godot godot) {
        this.godotPlugin = godotPlugin;
        sensorManager = (SensorManager) Objects.requireNonNull(godot.getActivity()).getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private float[] mGravity = null;
    private float[] mGeomagnetic = null;

    @Override
    public void onSensorChanged(SensorEvent event) {
        var currentTime = LocalTime.now();
        if (currentTime.toNanoOfDay() - lastUpdate.toNanoOfDay() > TIME_GAP_MS * 1000000) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mGravity = event.values;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mGeomagnetic = event.values;
            }

            if (mGravity != null && mGeomagnetic != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuthInRadians = (orientation[0]);
                }

                godotPlugin.emitSignal(godotPlugin.getSensorChangeSignal().getName(), azimuthInRadians);
                lastUpdate = currentTime;
            }
        }
    }

    public float getAzimuthInRadians() {
        return azimuthInRadians;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
