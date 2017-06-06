package com.rteixeira.traveller.tracker.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.rteixeira.traveller.tracker.interfaces.LocationRequester;
import com.rteixeira.traveller.tracker.LocationTracker;

import java.util.ArrayList;
import java.util.List;

/***
 * Simple Service to keep the LocationTracker Running while the app is in the background, it will
 * add the new Locations to the Storager.
 *
 * Note: this service is blind, it assumes it has to work it its instantiated. This should only go
 * online if the app is about to go to background AND the app is already tracking the user. It also
 * assumes that the GPS is on and permissions where given to the app to use it.
 */
public class LocationTrackerService extends Service implements LocationRequester {

    private LocationTracker tracker;
    public static List<Location> locations = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tracker = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tracker = LocationTracker.getInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public Context getContext() {
        return this.getBaseContext();
    }

    @Override
    public void updateUserLocation(Location location) {
        locations.add(location);
    }

    @Override
    public void detachTracker() {
        tracker = null;
    }

    @Override
    public void requestPermissions(String[] permission) {
        // Should never be fired here in the Service
    }

    @Override
    public void requestTurningGPSOn() {
        // Should never Happen in the server layer
    }
}
