package com.rteixeira.traveller.storage.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


/***
 * Object representation of Journey. This Model exists first to facilitate serialization and
 * second to keep in mind app expansion. One could later add Name and Category
 * (or list of categories) for each Journey.
 *
 */
public class Journey {
    @Expose
    @SerializedName("list")
    public List<DatedGeoPoint> geoPoints;
    @Expose
    public int id;

    public Journey(List<DatedGeoPoint> geoPoints, int id) {
        this.geoPoints = geoPoints;
        this.id = id;
    }

    public List<DatedGeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public void setGeoPoints(List<DatedGeoPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
