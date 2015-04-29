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

            System.out.print("working...");
            // Get the trajectories for all the trips
            ArrayList<Trajectory> trajectories = GTFSParser.parseTrips(calendarPath,
                    routesPath, stopTimesPath, stopsPath, tripsPath);

            // Print all trajectories
            // for (Trajectory trajectory : trajectories) {
            //     System.out.println(trajectory);
            // }

            // Print a single trajectory (the extrapolated trajectories are massive)
            System.out.println(trajectories.get(0));

            // Load them into memory as a list of hash maps
            // ArrayList<Map<String, String>> calendar = GTFSParser.readCSV(calendarFile);
            // ArrayList<Map<String, String>> routes = GTFSParser.readCSV(routesFile);
            // ArrayList<Map<String, String>> stopTimes = GTFSParser.readCSV(stopTimesFile);
            // ArrayList<Map<String, String>> stops = GTFSParser.readCSV(stopsFile);
            // ArrayList<Map<String, String>> trips = GTFSParser.readCSV(tripsFile);

            // Now print these lists to verify
            // printCSV(calendar);
            // printCSV(routes);
            // printCSV(stopTimes);
            // printCSV(stops);
            // printCSV(trips);

        } catch (Exception e) {
            System.out.println("Invalid file.");
        }
    }

    /* Will print out a list of maps */
    public static void printCSV(ArrayList<Map<String, String>> csv) {
        for (Map csvRow : csv) {
            System.out.println(Arrays.toString(csvRow.entrySet().toArray()));
        }
    }
}
