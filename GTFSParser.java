import java.util.*;
import java.io.*;
import java.text.*;

public class GTFSParser {
    /* Returns a list of trajectories of various trips */
    public static ArrayList<Trajectory> parseTrips(File calendarFile,
            File routesFile, File stopTimesFile, File stopsFile, File tripsFile) {

        // List of trajectory for each tripId, serviceId combination
        ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

        try {
            // Read in all files
            ArrayList<Map<String, String>> calendar = readCSV(calendarFile);
            ArrayList<Map<String, String>> routes = readCSV(routesFile);
            ArrayList<Map<String, String>> stopTimes = readCSV(stopTimesFile);
            ArrayList<Map<String, String>> stops = readCSV(stopsFile);
            ArrayList<Map<String, String>> trips = readCSV(tripsFile);

            // Parse trips from trips file
            for (Map<String, String> trip : trips) {
                // Get attributes for new Trajectory
                String tripId = trip.get("trip_id");
                String serviceId = trip.get("service_id");
                SortedMap<Long, Coordinate> trajectoryMap = getTrajectoryMap(tripId, 
                        serviceId, calendar, routes, stopTimes, stops);

                // Create new Trajectory, add it to the list
                Trajectory trajectory = new Trajectory(tripId, serviceId, trajectoryMap);
                trajectories.add(trajectory);
            }
        } catch (IOException e) {
            System.out.println("Error reading files.");
        }

        return trajectories;
    }

    /* Returns a map of coordinates for a specific tripId, serviceId */
    public static SortedMap<Long, Coordinate> getTrajectoryMap(String tripId, String serviceId,
            ArrayList<Map<String, String>> calendar, ArrayList<Map<String, String>> routes,
            ArrayList<Map<String, String>> stopTimes, ArrayList<Map<String, String>> stops) {

        SortedMap<Long, Coordinate> trajectoryMap = new TreeMap<Long, Coordinate>();

        // Go through all the stop times, collecting the ones for this tripId
        ArrayList<Map<String, String>> tripStopTimes = new ArrayList<Map<String, String>>();
        for (Map<String, String> stopTime : stopTimes) {
            // System.out.println(tripId + ".   " + stopTime.get("trip_id") + ".");
            if (stopTime.get("trip_id").equals(tripId)) {
                tripStopTimes.add(stopTime);
            }
        }

        // Now go through the stopTimes for our tripId, getting time and lat/lon
        for (Map<String, String> stopTime : tripStopTimes) {
            // Find the arrival time at a stop, in seconds past midnight
            String arrivalTime = stopTime.get("arrival_time");
            long arrivalTimeSec = getElapsedTime(arrivalTime);

            // Find the lat/lon of the stop
            String stopId = stopTime.get("stop_id");
            double lat = -1.0, lon = -1.0;
            for (Map<String, String> stop : stops) {
                // Find the entry for this stopId in the stops CSV
                if (stop.get("stop_id").equals(stopId)) {
                    lat = Double.parseDouble(stop.get("stop_lat"));
                    lon = Double.parseDouble(stop.get("stop_lon"));
                }
            }
            Coordinate stopCoords = new Coordinate(lat, lon);

            // Add the stop arrival time, coordinates to our trajectory map
            trajectoryMap.put(arrivalTimeSec, stopCoords);
        }

        return trajectoryMap;
    }

    /* Returns a time HH:mm:ss formatted as UNIX time (seconds)
     * We assume a time zone of PST (since BART is located there) */
    public static long getElapsedTime(String arrivalTime) {
        // Create the SimpleDateFormat object for time parsing
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("PST"));

        // Next, create a Date object for this timestamp 
        Date arrivalDate = null;
        try { 
            arrivalDate = dateFormatter.parse(arrivalTime); 
        } 
        catch (ParseException e) {
            System.out.println("Error parsing arrival time."); 
        }

        // Finally, create a Calendar object, from which we can extract the seconds 
        Calendar cal = Calendar.getInstance();
        cal.setTime(arrivalDate);
        long arrivalTimeSec = cal.getTimeInMillis() / 1000;

        return arrivalTimeSec;
    }

    /* Returns a list of hash maps of each row (column name: value) in the CSV */
    public static ArrayList<Map<String, String>> readCSV(File file) throws IOException {
        // Prepare to read the input CSV file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        // Some variables for the CSV file
        String[] headerRow = br.readLine().split(",");
        ArrayList<Map<String, String>> csvRows = new ArrayList<Map<String, String>>();

        // Iterate through each line in the CSV, adding it to the list
        while ((line = br.readLine()) != null) {
            String[] rowValues = line.split(",", -1); // -1 to accept empty strings
            Map<String, String> row = new HashMap<String, String>();
            for (int i = 0; i < headerRow.length; i++) {
                row.put(headerRow[i], rowValues[i]);
            }
            csvRows.add(row);
        }

        return csvRows;
    }

}
