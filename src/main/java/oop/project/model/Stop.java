package oop.project.model;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Stop {
    // Private variables for a stop instance
    int routeId;
    String routeName;
    String routeColor;
    String stopId;
    String stopName;
    Coordinate stopCoords;
    String stopURL;

    /* Constructor method for a new stop */
    public Stop(int routeId, String routeName, String routeColor, String stopId,
                   String stopName, Coordinate stopCoords, String stopURL) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.routeColor = routeColor;
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopCoords = stopCoords;
        this.stopURL = stopURL;
    }

    /* Return a string representation of this stop */
    public String toString() {
        return "{\n"
               + "  routeId: " + this.routeId + ",\n"
               + "  routeName: " + this.routeName + ",\n"
               + "  routeColor: " + this.routeColor + ",\n"
               + "  stopId: " + this.stopId + ",\n"
               + "  stopName: " + this.stopName + ",\n"
               + "  stopCoords: " + this.stopCoords + ",\n"
               + "  stopURL: " + this.stopURL + "\n"
               + "}";
    }

    /**
     * Get this stop's route id
     * @return          int representing the route id of the containing route
     */
    public int getRouteId() {
        return this.routeId;
    }

    /**
     * Get this stop's route color
     * @return          string representing the color of the containing route
     */
    public String getColor() {
        return this.routeColor;
    }

    /**
     * Get this stop's coordinates
     * @return          coordinate object with lat lon of this stop
     */
    public Coordinate getCoord() {
        return this.stopCoords;
    }

    /**
     * Get this stop's name
     * @return          string representing the name of the stop
     */
    public String getName() {
        return this.stopName;
    }
}
