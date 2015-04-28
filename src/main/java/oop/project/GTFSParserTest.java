import java.util.*;
import java.io.*;

public class GTFSParserTest {
    /* Some basic tests for the GTFSParser class */
    public static void main(String[] args) {
        try {
            // Open all the GTFS CSV files
            File calendarFile = new File("bart_gtfs/calendar.csv");
            File routesFile = new File("bart_gtfs/routes.csv");
            File stopTimesFile = new File("bart_gtfs/stop_times.csv");
            File stopsFile = new File("bart_gtfs/stops.csv");
            File tripsFile = new File("bart_gtfs/trips.csv");

            // Get the trajectories for all the trips
            ArrayList<Trajectory> trajectories = GTFSParser.parseTrips(calendarFile,
                    routesFile, stopTimesFile, stopsFile, tripsFile);

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
