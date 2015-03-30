import java.util.*;
import java.io.*;

public class GTFSParserTest {
    /* Some basic tests for the GTFSParser class */
    public static void main(String[] args) {
        try {
            // Open all the GTFS CSV files
            File calendarFile = new File("bart_gtfs/calendar.txt");
            File routesFile = new File("bart_gtfs/routes.txt");
            File stopTimesFile = new File("bart_gtfs/stop_times.txt");
            File stopsFile = new File("bart_gtfs/stops.txt");
            File tripsFile = new File("bart_gtfs/trips.txt");

            // Load them into memory as a list of hash maps
            ArrayList<Map<String, String>> calendar = GTFSParser.readCSV(calendarFile);
            ArrayList<Map<String, String>> routes = GTFSParser.readCSV(routesFile);
            ArrayList<Map<String, String>> stopTimes = GTFSParser.readCSV(stopTimesFile);
            ArrayList<Map<String, String>> stops = GTFSParser.readCSV(stopsFile);
            ArrayList<Map<String, String>> trips = GTFSParser.readCSV(tripsFile);

            // Now print these lists to verify
            printCSV(calendar);
            printCSV(routes);
            printCSV(stopTimes);
            printCSV(stops);
            printCSV(trips);

        } catch (IOException e) {
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
