package oop.project;

import java.io.*;
import java.net.URL;
import java.util.*;

public class GTFSParserTest {
    /* Some basic tests for the GTFSParser class */
    public static void main(String[] args) {
        try {
            // Open all the GTFS CSV files
            URL calendarPath = GTFSParserTest.class.getResource("bart_gtfs/calendar.csv");
            URL routesPath = GTFSParserTest.class.getResource("bart_gtfs/routes.csv");
            URL stopTimesPath = GTFSParserTest.class.getResource("bart_gtfs/stop_times.csv");
            URL stopsPath = GTFSParserTest.class.getResource("bart_gtfs/stops.csv");
            URL tripsPath = GTFSParserTest.class.getResource("bart_gtfs/trips.csv");

            // Get the trajectories for all the trips
            System.out.println("Running GTFSParser.parseTrips().");
            ArrayList<Trajectory> trajectories = GTFSParser.parseTrips(calendarPath,
                    routesPath, stopTimesPath, stopsPath, tripsPath);

            // Print all trajectories
            // for (Trajectory trajectory : trajectories) {
            //     System.out.println(trajectory);
            // }

            // Print a single trajectory (the extrapolated trajectories are massive)
            System.out.println(trajectories.get(0));

            // Load all stops
            System.out.println("Running GTFSParser.getStopsByRoute().");
            ArrayList<ArrayList<Stop>> stopsByRoute = GTFSParser.getStopsByRoute(calendarPath,
                    routesPath, stopTimesPath, stopsPath, tripsPath);

            // Print out the first route
            ArrayList<Stop> route = stopsByRoute.get(0);
            for (Stop stop : route) {
                System.out.println(stop);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /* Will print out a list of maps */
    public static void printCSV(ArrayList<Map<String, String>> csv) {
        for (Map csvRow : csv) {
            System.out.println(Arrays.toString(csvRow.entrySet().toArray()));
        }
    }
}
