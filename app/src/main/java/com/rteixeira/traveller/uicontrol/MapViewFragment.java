package com.rteixeira.traveller.uicontrol;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rteixeira.traveller.R;
import com.rteixeira.traveller.storage.LocationStorage;
import com.rteixeira.traveller.tracker.interfaces.LocationRequester;
import com.rteixeira.traveller.tracker.LocationTracker;
import com.rteixeira.traveller.tracker.services.LocationTrackerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Map Fragment responsible for showing everything that is happening on the map. Also responsible
 * to call the correct Engines depending on user input.
 */
public class MapViewFragment extends Fragment implements LocationRequester, OnMapReadyCallback {

    private static final int REQUEST_LOCATION_INDEX = 123;
    private static final float INITIAL_CAM_ZOOM = 17;

    private GoogleMap mMap = null;
    private MapView mMapView = null;
    private Context mContext = null;
    private ProgressBar loader = null;

    private CircleOptions errorCircleOptions = null;
    private PolylineOptions journeyMarker = null;

    private Marker user = null;
    private Circle errorCircle = null;

    private LocationTracker tracker = null;
    private boolean isTracking = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);

        try {
            if (mMap == null) {
                mMapView = (MapView) rootView.findViewById(R.id.mapView);
                loader = (ProgressBar) rootView.findViewById(R.id.loader);
                mMapView.onCreate(savedInstanceState);

                InitMapLine();
                MarginCircleInit();
                setHasOptionsMenu(true);

                mMapView.onResume(); // needed to get the map to display immediately

                MapsInitializer.initialize(getActivity().getApplicationContext());

                loader.bringToFront();
                loader.setVisibility(View.VISIBLE);

                mMapView.getMapAsync(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        tracker = LocationTracker.getInstance(MapViewFragment.this);
        tracker.setAccurateLocationTracking();
        Toast.makeText(getActivity(), R.string.gps_loading, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(Context activity) {
        mContext = activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        MenuItem item = menu.findItem(R.id.map_switch);
        item.setActionView(R.layout.tracker_layout);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Starts tracking the user journey, requests the window to keep active and makes sure the
     * tracker is running on high accuracy.
     */
    public void toggleTrackingOn(){
        if(!isTracking) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isTracking = true;
            tracker.setAccurateLocationTracking();
            Toast.makeText(getActivity(), R.string.START_TRACKING,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Finishes the user journey, requests the save of it and makes sure everything is ready for the
     * next record.
     */
    public void toggleTrackingOff(){
        if(isTracking) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isTracking = false;
            mMap.clear();

            // Given that i am not keeping track of the number of polylines i add to the map,
            // I need to clear it. By Doing so i need to re-add the user location to the map.
            LatLng lastLocation = LocationStorage.getJourneyLastRecordedLocationFromBuffer();
            if (lastLocation != null) {
                user = mMap.addMarker(new MarkerOptions().position(lastLocation).
                        title(getString(R.string.YOU)).icon(BitmapDescriptorFactory.
                        defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }

            LocationStorage.saveCurrentJourney(mContext);
            Toast.makeText(getActivity(), R.string.STOP_TRACKING,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When going into Pause, if the app is already tracking init the Tracking service otherwise set
     * the passive tracking system to BatterySaving to avoid any unnecessary battery usage.
     */
    @Override
    public void onPause() {
        super.onPause();
        if(tracker != null) {
            if (isTracking) {
                Intent serviceIntent = new Intent(mContext, LocationTrackerService.class);
                mContext.startService(serviceIntent);
            } else {

                tracker.setBatterySaverLocationTracking();
            }
        }
    }

    /**
     * on resume if the app was on the background tracking anything it will go grab what it had and
     * add it to the map.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isTracking) {
            List<Location> locations = LocationTrackerService.locations;
            mContext.stopService(new Intent(mContext, LocationTrackerService.class));

            for (Location loc : locations) {
                journeyMarker.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                LocationStorage.addEntry(loc.getLatitude(), loc.getLongitude(), loc.getTime());
            }
            mMap.addPolyline(journeyMarker);

            //setting up the user location after adding all the entries behind, if any.
            if (locations.size() > 0) {
                Location loc = locations.get(locations.size() - 1);
                user.setPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
            }
            //resenting the service list.
            LocationTrackerService.locations = new ArrayList<>();
        } else {
            if (tracker != null){
                tracker.setAccurateLocationTracking();
            }
        }
    }

    @Override
    public void onDestroy() {
        // should it save an journey if its recording anything? it would depend on what the client
        // wanted as behaviour, for now just leaving this note.
        super.onDestroy();
        tracker.destroyTracker();
        tracker = null;
        mContext = null;
        mMapView = null;
        mMap = null;
        errorCircle = null;
        errorCircleOptions = null;
        journeyMarker = null;
        user = null;
        loader = null;
    }

    @Override
    public Context getContext() {
        return this.mContext;
    }

    /**
     * Main "loop" of the app behaviour. This method is responsible for updating the map and
     * calling the necessary parts of the app based on user input.
     *
     * This method will be called every time the Tracker has a new location.
     *
     * @param location Most resent user location.
     */
    @Override
    public void updateUserLocation(Location location) {
        loader.setVisibility(View.GONE);
        if (user == null) {
            user = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude())).title(getString(R.string.YOU)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                    location.getLongitude()), INITIAL_CAM_ZOOM));
        } else {

            // TODO an animation here would be nice instead of "teleporting" the marker
            user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        if (location.getAccuracy() >= 20.0) {
            if (errorCircle == null) {
                errorCircleOptions.center(user.getPosition());
                errorCircle = mMap.addCircle(errorCircleOptions);
            }

            errorCircle.setCenter(user.getPosition());
            errorCircle.setRadius(location.getAccuracy());

        } else {
            if (errorCircle != null) {
                errorCircle.remove();
                errorCircle = null;
            }
        }

        //if the tracking system is on, the new locations are saved and drawn in the map,
        // and the camera is kept centered in the user location.
        if (isTracking) {

            journeyMarker.add(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.addPolyline(journeyMarker);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                    location.getLongitude()), mMap.getCameraPosition().zoom));
            LocationStorage.addEntry(location.getLatitude(), location.getLongitude(),
                    location.getTime());
        }
    }


    @Override
    public void detachTracker() {
        tracker = null;
    }

    @Override
    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_LOCATION_INDEX);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_INDEX:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tracker.setAccurateLocationTracking();
                } else {
                    Snackbar.make(mMapView, getString(R.string.REQUEST_PERMISSIONS),
                            Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(permissions);
                        }
                    }).show();
                }
        }
    }

    @Override
    public void requestTurningGPSOn() {
        Snackbar.make(mMapView, R.string.GPS_ON, Snackbar.LENGTH_INDEFINITE).
                setAction(R.string.TURN_ON, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).show();
    }

    /**
     * Simple init for the polyline used to trace the user journey.
     */
    private void InitMapLine() {
        journeyMarker = new PolylineOptions();
        journeyMarker.color(Color.parseColor(getString(R.string.BLUE_ARGB)));
        journeyMarker.width(8);
        journeyMarker.visible(true);
    }

    /**
     * Simple init to draw a circle representing accuracy around the user location.
     */
    private void MarginCircleInit() {
        errorCircleOptions = new CircleOptions();
        errorCircleOptions.fillColor(Color.argb(75, 30, 144, 255));
        errorCircleOptions.strokeColor(Color.argb(200, 30, 144, 255));
        errorCircleOptions.strokeWidth(4.0f);
    }
}
