package com.rteixeira.traveller.tracker.interfaces;

import android.content.Context;
import android.location.Location;

/***
 * Simple interface required to be implemented by anything that wants to use LocationTracker.
 * This way the tracker can tell its needs and results to any requester without caring exactly how
 * the request will use the information.
 */
public interface LocationRequester {

    Context getContext();
    void updateUserLocation(Location location);
    void detachTracker();
    void requestPermissions(String[] permission);
    void requestTurningGPSOn();
}
