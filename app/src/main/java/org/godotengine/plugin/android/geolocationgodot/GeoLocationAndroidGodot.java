package org.godotengine.plugin.android.geolocationgodot;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Objects;
import java.util.Set;

public class GeoLocationAndroidGodot extends GodotPlugin {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private final SignalInfo locationUpdateSignal = new SignalInfo("onLocationUpdates", Dictionary.class);
    private final SignalInfo lastKnownLocationSignal = new SignalInfo("onLastKnownLocation", Dictionary.class);
    private final SignalInfo errorSignal = new SignalInfo("onLocationError", Integer.class, String.class);
    private final SignalInfo sensorChangeSignal = new SignalInfo("onSensorChangeSignal", Float.class);
    private GpsLocationListener gpsLocationListener;

    /**
     * Base constructor passing a {@link Godot} instance through which the plugin can access Godot's
     * APIs and lifecycle events.
     *
     * @param godot
     */
    public GeoLocationAndroidGodot(Godot godot) {
        super(godot);
    }

    public SignalInfo getLocationUpdateSignal() {
        return locationUpdateSignal;
    }

    @Override
    public void onGodotSetupCompleted() {
        super.onGodotSetupCompleted();
        gpsLocationListener = new GpsLocationListener(this, getGodot());
        requestPermissions();
    }

    public SignalInfo getLastKnownLocationSignal() {
        return lastKnownLocationSignal;
    }

    public SignalInfo getErrorSignal() {
        return errorSignal;
    }

    public SignalInfo getSensorChangeSignal() {
        return sensorChangeSignal;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return BuildConfig.GODOT_PLUGIN_NAME;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @UsedByGodot
    public Dictionary getMobileResolution() {
        var resources = new Dictionary();

        resources.put("widthPixels", Resources.getSystem().getDisplayMetrics().widthPixels);
        resources.put("heightPixels", Resources.getSystem().getDisplayMetrics().heightPixels);

        return resources;
    }

    @UsedByGodot
    public String startGeoLocation() {
        var activity = getGodot().getActivity();
        if (activity == null) {
            return "ERROR activity is null";
        } else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "PERMISSION NOT GRANTED";
        }

        final var locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            emitSignal(errorSignal.getName(), -10, "NO GPS Activity");


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0.1f, gpsLocationListener);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0.1f, gpsLocationListener);
        });

        return "OK";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Set.of(locationUpdateSignal, lastKnownLocationSignal, errorSignal, sensorChangeSignal);
    }

    @Override
    public void emitSignal(String signalName, Object... signalArgs) {
        super.emitSignal(signalName, signalArgs);
    }
}
