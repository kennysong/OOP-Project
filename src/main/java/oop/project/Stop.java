package oop.project;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Stop {
    // Private variables for a stop instance
    int routeID;
    String routeName;
    String routeColor;
    String stopID;
    String stopName;
    Coordinate stopCoords;
    String stopURL;

    /* Constructor method for a new stop */
    public Stop(int routeID, String routeName, String routeColor, String stopID,
                   String stopName, Coordinate stopCoords, String stopURL) {
        this.routeID = routeID;
        this.routeName = routeName;
        this.routeColor = routeColor;
        this.stopID = stopID;
        this.stopName = stopName;
        this.stopCoords = stopCoords;
        this.stopURL = stopURL;
    }

    /* Return a string representation of this stop */
    public String toString() {
        return "{\n"
               + "  routeID: " + this.routeID + ",\n"
               + "  routeName: " + this.routeName + ",\n"
               + "  routeColor: " + this.routeColor + ",\n"
               + "  stopID: " + this.stopID + ",\n"
               + "  stopName: " + this.stopName + ",\n"
               + "  stopCoords: " + this.stopCoords + ",\n"
               + "  stopURL: " + this.stopURL + "\n"
               + "}";
    }

}
