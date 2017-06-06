package com.rteixeira.traveller.storage.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/***
 * Model to storage persistently the Journeys created by the user. Each Journey should be saved as
 * an ordered list of DatedGeoPoints so it easy to calculate travel times. Serialized names where
 * given to shorten the final json.
 */
public class DatedGeoPoint {

    @Expose
    @SerializedName("lat")
    private double latitude;
    @Expose
    @SerializedName("lon")
    private double longitude;
    @Expose
    @SerializedName("dt")
    private long date;

    public DatedGeoPoint(double latitude, double longitude, long date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
