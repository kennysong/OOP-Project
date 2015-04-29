package oop.project;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Transit {
    // Private variables for a transit system instance
    private ArrayList<Trajectory> trajectories;

    /* Constructor method for a transit system */
    public Transit() {
        ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

        try {
            // Open all the GTFS CSV files
            URL calendarPath = Transit.class.getResource("bart_gtfs/calendar.csv");
            URL routesPath = Transit.class.getResource("bart_gtfs/routes.csv");
            URL stopTimesPath = Transit.class.getResource("bart_gtfs/stop_times.csv");
            URL stopsPath = Transit.class.getResource("bart_gtfs/stops.csv");
            URL tripsPath = Transit.class.getResource("bart_gtfs/trips.csv");

            // Get the trajectories for all the trips
            trajectories = GTFSParser.parseTrips(calendarPath, routesPath,
                stopTimesPath, stopsPath, tripsPath);
        } catch (Exception e) {
            System.out.println("Invalid file.");
        }

        this.trajectories = trajectories;
    }

    /* Returns the active trajectories at a specified time */
    public ArrayList<Trajectory> activeTrajectories(long time) {
        // Go through all our Trajectory objects, going through each point in
        // the Trajectory to see if it has a point at the specified time
        ArrayList<Trajectory> trajectoryList = new ArrayList<Trajectory>();
        for (Trajectory trajectory : this.trajectories) {
            if (trajectory.isActive(time)) {
                trajectoryList.add(trajectory);
            }
        }

        return trajectoryList;
    }
}
