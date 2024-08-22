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

    private final float[] lastAccelerometer = new float[3];
    private final float[] lastMagnetometer = new float[3];

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

    @Override
    public void onSensorChanged(SensorEvent event) {
        var currentTime = LocalTime.now();
        if (currentTime.toNanoOfDay() - lastUpdate.toNanoOfDay() > TIME_GAP_MS * 1000000) {

            if (event.sensor == magnetometer) {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            } else if (event.sensor == accelerometer) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            }

            final var rotationMatrix = new float[9];
            final var orientation = new float[3];

            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            azimuthInRadians = orientation[0];

            godotPlugin.emitSignal(godotPlugin.getSensorChangeSignal().getName(), azimuthInRadians);
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
