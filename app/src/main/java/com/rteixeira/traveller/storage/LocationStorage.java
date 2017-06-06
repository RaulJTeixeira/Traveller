package com.rteixeira.traveller.storage;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rteixeira.traveller.storage.models.DatedGeoPoint;
import com.rteixeira.traveller.storage.models.Journey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple static class that handles the current journey being tracked and saves it with all the
 * other already saved in the system.
 */
public class LocationStorage {

    private static String APP_CONTEXT = "TravellerControl";
    private static String JOURNEY_LIST_KEY = "Journey List";

    private static List<DatedGeoPoint> currentJourney = new ArrayList<>();
    private static Gson parser = new GsonBuilder().create();

    public static List<DatedGeoPoint> getCurrentJourney() {
        return currentJourney;
    }

    /**
     * Adds a new entry to the current journey. Note that the journey being recorded is saved in
     * memory. Only after completed the journey is saved persistently.
     *
     * @param latitude Latitude as a double for the point to add to the journey
     * @param longitude Longitude as a double for the point to add to the journey
     * @param date Date as long for the point to add to the journey
     */
    public static void addEntry(double latitude, double longitude, long date){
        currentJourney.add(new DatedGeoPoint(latitude,longitude,date));
    }


    /**
     * Saves Current Journey to the system and completes it.
     * @param context
     */
    public static void saveCurrentJourney(Context context) {

        if (currentJourney.size() > 1) {
            String list = readValue(context, JOURNEY_LIST_KEY, "");
            //if the list string is empty (default value) it means it is the first to be added.
            if (list.isEmpty()) {
                Journey jn = new Journey(currentJourney, 1);
                writeValue(context, JOURNEY_LIST_KEY, parser.toJson(new Journey[]{jn}, Journey[].class));
            } else {
                //if a list already exists, load it, add the new journey and re-write it.
                Journey[] old_list = parser.fromJson(list, Journey[].class);
                Journey jn = new Journey(currentJourney, old_list.length + 1);

                Journey[] new_list = Arrays.copyOf(old_list, old_list.length + 1);
                System.arraycopy(new Journey[]{jn}, 0, new_list, old_list.length, 1);

                writeValue(context, JOURNEY_LIST_KEY, parser.toJson(new_list, Journey[].class));
            }
            currentJourney = new ArrayList<>();
        }
    }

    /**
     * Returns all the saved Journeys.
     * @param context
     * @return
     */
    public static List<Journey> getAllSavedJourneys (Context context){
        String list = readValue(context, JOURNEY_LIST_KEY, "");
        if(list.isEmpty()){
            return new ArrayList<>();
        } else {
            Journey[] items = parser.fromJson(list, Journey[].class);
            List<Journey> finalList = Arrays.asList(items);

            //quick sort to return the list ordered by older first (using the ID for each journey)
            Collections.sort(finalList , new Comparator<Journey>() {
                @Override
                public int compare(Journey o1, Journey o2) {
                    return o1.id < o2.id ? 1 : -1;
                }
            });

            return finalList;
        }
    }

    public static LatLng getJourneyLastRecordedLocationFromBuffer(){
     return currentJourney != null && currentJourney.size() > 0 ?
             new LatLng(currentJourney.get(currentJourney.size() - 1).getLatitude(),
                     currentJourney.get(currentJourney.size() - 1).getLongitude()) : null;
    }

    private static String readValue(Context ctx, String key, String def) {
        SharedPreferences prefs = ctx.getSharedPreferences(APP_CONTEXT, Context.MODE_PRIVATE);
        return prefs.getString(key, def);
    }

    private static void writeValue(Context ctx, String key, String value) {
        SharedPreferences prefs = ctx.getSharedPreferences(APP_CONTEXT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
