package oop.project.model;

import com.lynden.gmapsfx.javascript.object.LatLong;

public class Coordinate {
    // Private instance variables for a Coordinate object
    private double lat;
    private double lon;

    /* Constructor for a Coordinate */
    public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /* Returns the latitude of the Coordinate */
    public double getLat() {
        return this.lat;
    }

    /* Returns the longitude of the Coordinate */
    public double getLon() {
        return this.lon;
    }

    /* Converts coordinate to a string representation */
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }

    /**
     * Convenience to convert from Coordinate to LatLong
     * @return          a LatLong Object
     */
    public LatLong toLatLong() {
        return new LatLong(this.lat, this.lon);
    }
}
