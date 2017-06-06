package com.rteixeira.traveller.tracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.rteixeira.traveller.tracker.interfaces.LocationRequester;

/***
 * Singleton GPS Tracker configurable to meet the needs of the project.
 */
public class LocationTracker implements LocationListener {

    private static LocationTracker instance = null;

    private LocationManager mLocationManager = null;

    private LocationRequester requester = null;

    private static final int NOT_TRACKING = 0;
    private static final int HIGH_ACCURACY_TRACKING = 1;
    private static final int IDLE_TRACKING = 2;
    private static final int BATTERY_SAVER_TRACKING = 3;

    private int currentTrackerOptions = IDLE_TRACKING;

    private LocationTracker() {
    }

    /***
     * Instance controller for this Tracker as it is implemented as Singleton.
     *
     * @param requester Implementation of interface needed to use this tracker.
     * @return LocationTracker singleton.
     */
    public static LocationTracker getInstance(LocationRequester requester) {
        if (instance == null) {
            instance = new LocationTracker();
        } else {
            if (instance.requester != null) {
                instance.requester.detachTracker();
            }
        }
        instance.requester = requester;
        return instance;
    }

    /***
     * Full instance shutdown leaving nothing on memory, to use only when the app is going to be
     * closed.
     */
    public void destroyTracker() {
        stopLocationTracking();
        requester.detachTracker();
        requester = null;
    }

    /***
     * Set or re-set of tracker options for High accuracy. Intended to be used when tracking and
     * registering user's location.
     */
    public void setAccurateLocationTracking() {
        currentTrackerOptions = HIGH_ACCURACY_TRACKING;
        initLocationTracking();
    }
    /***
     * Set or re-set of tracker options for Passive accuracy. Intended to be used when the app
     * is sent to background and it is not tracking the user's position.
     */
    public void setBatterySaverLocationTracking() {
        currentTrackerOptions = BATTERY_SAVER_TRACKING;
        initLocationTracking();
    }

    /**
     * Will remove the User's location updates. Pausing the tracker completely.
     */
    public void stopLocationTracking() {
        currentTrackerOptions = NOT_TRACKING;
        if(mLocationManager != null){
            mLocationManager.removeUpdates(this);
        }
    }

    /**
     * The general behaviour of this Location Tracker is very unstable, I would try an alternative
     * to this implementation if I had time. A quick search online shows that multiple people have
     * issues with this implementation.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        requester.updateUserLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    /**
     * Resume tracking (or not, depending on "currentTrackerOptions") as soon as the Location
     * Services are back online.
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        initLocationTracking();
    }

    /**
     * request Location Services to go back online.
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        requester.requestTurningGPSOn();
    }


    /**
     * Single point of control to start tracking the user location. It will verify if it was
     * correctly initialized, if the GPS is on and if it has permissions. If so it will start
     * retrieving the user Location, otherwise it will notify the requester of whats missing.
     */
    private void initLocationTracking() {

        if (currentTrackerOptions == NOT_TRACKING){
            return;
        }

        if(mLocationManager == null) {
            mLocationManager = (LocationManager) requester.getContext()
                    .getSystemService(Context.LOCATION_SERVICE);

        } else {
            mLocationManager.removeUpdates(this);
        }

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if ((ContextCompat.checkSelfPermission(requester.getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                 ContextCompat.checkSelfPermission(requester.getContext(),
                         Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if(currentTrackerOptions == BATTERY_SAVER_TRACKING) {
                    mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
                }else {
                    /**
                     * This exists because of an Android bug.
                     * See more at: https://issuetracker.google.com/issues/36975498
                     *
                     */
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }

            } else {
                requester.requestPermissions(new String[]{android.Manifest.permission.
                        ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        } else {
            requester.requestTurningGPSOn();
        }
    }
}
