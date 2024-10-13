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

    private static final int TIME_GAP_MS = 100;

    private final GeoLocationAndroidGodot godotPlugin;
    private final Sensor magnetometer;
    private final Sensor accelerometer;
    private final SensorManager sensorManager;

    private LocalTime lastUpdate = LocalTime.now().minusMinutes(1);
    private float azimuthInRadians = 0.0F;

    private float[] gData = new float[3]; // accelerometer
    private float[] mData = new float[3]; // magnetometer
    private final float[] rMat = new float[9];
    private final float[] iMat = new float[9];
    private final float[] orientation = new float[3];

    public CompassSensorEventListener(final GeoLocationAndroidGodot godotPlugin, final Godot godot) {
        this.godotPlugin = godotPlugin;
        sensorManager = (SensorManager) Objects.requireNonNull(godot.getActivity()).getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        var currentTime = LocalTime.now();
        if (currentTime.toNanoOfDay() - lastUpdate.toNanoOfDay() > TIME_GAP_MS * 1000000) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    gData = event.values.clone();
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mData = event.values.clone();
            }

            if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {
                azimuthInRadians = SensorManager.getOrientation(rMat, orientation)[0];
                godotPlugin.emitSignal(godotPlugin.getSensorChangeSignal().getName(), azimuthInRadians);
            }
            lastUpdate = currentTime;
        }
    }

    public float getAzimuthInRadians() {
        return azimuthInRadians;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
