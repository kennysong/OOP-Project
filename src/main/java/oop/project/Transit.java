package oop.project;

import java.util.*;
import java.io.*;

public class Transit {
    // Private variables for a transit system instance
    private ArrayList<Trajectory> trajectories;

    /* Constructor method for a transit system */
    public Transit() {
        ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

        try {
            // Open all the GTFS CSV files
            File calendarFile = new File("bart_gtfs/calendar.csv");
            File routesFile = new File("bart_gtfs/routes.csv");
            File stopTimesFile = new File("bart_gtfs/stop_times.csv");
            File stopsFile = new File("bart_gtfs/stops.csv");
            File tripsFile = new File("bart_gtfs/trips.csv");

            // Get the trajectories for all the trips
            trajectories = GTFSParser.parseTrips(calendarFile,
                    routesFile, stopTimesFile, stopsFile, tripsFile);
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
