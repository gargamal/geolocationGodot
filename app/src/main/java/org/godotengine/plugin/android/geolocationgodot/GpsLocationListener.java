package org.godotengine.plugin.android.geolocationgodot;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;

import java.time.LocalTime;

public class GpsLocationListener implements LocationListener {

    private static final int TIME_GAP_MS = 500;

    private static LocalTime lastUpdate = LocalTime.now().minusMinutes(1);

    private final GeoLocationAndroidGodot godotPlugin;
    private final CompassSensorEventListener compassSensorEventListener;

    public GpsLocationListener(final GeoLocationAndroidGodot godotPlugin, final Godot godot) {
        this.godotPlugin = godotPlugin;
        compassSensorEventListener = new CompassSensorEventListener(godotPlugin, godot);
        compassSensorEventListener.start();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        var currentUpdate = LocalTime.now();
        if (currentUpdate.toNanoOfDay() - lastUpdate.toNanoOfDay() > TIME_GAP_MS * 1000000) {
            var longitude = location.getLongitude();
            var latitude = location.getLatitude();
            var accuracy = location.getAccuracy();
            var altitude = location.getAltitude();
            var speed = location.getSpeed();
            var time = location.getTime();
            var verticalAccuracyMeters = location.getVerticalAccuracyMeters();
            var bearing = location.getBearing();
            var locationDictionary = new Dictionary();

            locationDictionary.put("longitude", longitude);
            locationDictionary.put("latitude", latitude);
            locationDictionary.put("accuracy", accuracy);
            locationDictionary.put("verticalAccuracyMeters", verticalAccuracyMeters);
            locationDictionary.put("altitude", altitude);
            locationDictionary.put("speed", speed);
            locationDictionary.put("time", time);
            locationDictionary.put("bearing", bearing);
            locationDictionary.put("orientation", compassSensorEventListener.getAzimuthInRadians());

            lastUpdate = currentUpdate;
            godotPlugin.emitSignal(godotPlugin.getLocationUpdateSignal().getName(), locationDictionary);
        }
    }
}
