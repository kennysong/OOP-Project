package oop.project.util;

import java.io.*;
import java.net.URL;
import java.text.*;
import java.util.*;
import oop.project.model.*;

public class GTFSParser {
    /* Returns a list of trajectories of various trips */
    public static ArrayList<Trajectory> parseTrips(URL calendarPath,
            URL routesPath, URL stopTimesPath, URL stopsPath, URL tripsPath) {

        // List of trajectory for each tripId, serviceId combination
        ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

        try {
            // Read in all files
            ArrayList<Map<String, String>> calendar = readCSV(calendarPath);
            ArrayList<Map<String, String>> routes = readCSV(routesPath);
            ArrayList<Map<String, String>> stopTimes = readCSV(stopTimesPath);
            ArrayList<Map<String, String>> stops = readCSV(stopsPath);
            ArrayList<Map<String, String>> trips = readCSV(tripsPath);

            // Parse trips from trips file
            for (Map<String, String> trip : trips) {
                // Get attributes for new Trajectory
                String tripId = trip.get("trip_id");
                String serviceId = trip.get("service_id");
                int routeId = Integer.parseInt(trip.get("route_id"));
                SortedMap<Long, Coordinate> trajectoryMap = getTrajectoryMap(tripId,
                        serviceId, calendar, routes, stopTimes, stops);

                // Create new Trajectory, add it to the list
                Trajectory trajectory = new Trajectory(tripId, serviceId, routeId, trajectoryMap);
                trajectories.add(trajectory);
            }
        } catch (IOException e) {
            System.out.println("Error reading files.");
        }

        return trajectories;
    }

    /* Returns a list of lists of stations, where each list of stations is a route */
    public static ArrayList<ArrayList<Stop>> getStopsByRoute(URL calendarPath,
            URL routesPath, URL stopTimesPath, URL stopsPath, URL tripsPath) {

        // List of routes, which are lists of stops
        ArrayList<ArrayList<Stop>> stopsByRoute = new ArrayList<ArrayList<Stop>>();

        try {
            // Read in all files
            // ArrayList<Map<String, String>> calendar = readCSV(calendarPath);
            ArrayList<Map<String, String>> routes = readCSV(routesPath);
            ArrayList<Map<String, String>> stopTimes = readCSV(stopTimesPath);
            ArrayList<Map<String, String>> stops = readCSV(stopsPath);
            // ArrayList<Map<String, String>> trips = readCSV(tripsPath);

            // We offset each route by a certain latitude so they don't overlap
            double offset = 0.000200;
            int i = 0;

            // Go through each route, collecting the stops for each
            for (Map<String, String> routeCSV : routes) {
                ArrayList<Stop> route = new ArrayList<Stop>();
                int routeID = Integer.parseInt(routeCSV.get("route_id"));
                String routeName = routeCSV.get("route_long_name");
                String routeColor = routeCSV.get("route_color");

                // Hardcoded list of trips that correspond to a full route
                HashMap<Integer, String> routeToTrip = new HashMap<Integer, String>() {{
                    put(1, "01SFO10");
                    put(3, "01DCM20");
                    put(5, "02OAK10");
                    put(7, "01R11");
                    put(11, "01DC11");
                    put(19, "01DCM10");
                }};
                String tripID = routeToTrip.get(routeID);

                // Find all the stops that correspond to this trip
                for (Map<String, String> stopTime : stopTimes) {
                    if (!tripID.equals(stopTime.get("trip_id"))) { continue; }

                    // Get stop info from CSV
                    String stopID = stopTime.get("stop_id");
                    String stopName = null;
                    String stopURL = null;
                    Coordinate stopCoords = null;
                    for (Map<String, String> stop : stops) {
                        if (!stopID.equals(stop.get("stop_id"))) { continue; }

                        // Get stop info
                        stopName = stop.get("stop_name");
                        stopURL = stop.get("stop_url");
                        double stopLat = Double.parseDouble(stop.get("stop_lat"));
                        double stopLon = Double.parseDouble(stop.get("stop_lon")) + offset*i;
                        stopCoords = new Coordinate(stopLat, stopLon);
                        break;
                    }

                    // Add stop object to route
                    route.add(new Stop(routeID, routeName, routeColor, stopID,
                                       stopName, stopCoords, stopURL));
                }

                // Increase latitude offset for next route
                i++;

                // Add this route to our list of routes
                stopsByRoute.add(route);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        return stopsByRoute;
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

        // Finally, extrapolate these trajectories to every second
        trajectoryMap = extrapolateTrajectory(trajectoryMap);

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
        try { arrivalDate = dateFormatter.parse(arrivalTime); }
        catch (ParseException e) { System.out.println("Error parsing arrival time."); }

        // Finally, create a Calendar object, from which we can extract the seconds
        Calendar cal = Calendar.getInstance();
        cal.setTime(arrivalDate);
        long arrivalTimeSec = cal.getTimeInMillis() / 1000;

        return arrivalTimeSec;
    }

    /* Extrapolates a trajectory map to have points for every second */
    public static SortedMap<Long, Coordinate> extrapolateTrajectory(SortedMap<Long, Coordinate> trajectoryMap) {
        // Set up some variables for iteration
        long prevTime = -1;
        Coordinate prevCoordinate = new Coordinate(0.0, 0.0);
        SortedMap<Long, Coordinate> newTrajectoryMap = new TreeMap<Long, Coordinate>();
        newTrajectoryMap.putAll(trajectoryMap);

        // Iterate through each stop in the trajectory
        for(Map.Entry<Long, Coordinate> entry : trajectoryMap.entrySet()) {
            // End points for current path
            long stopTime = entry.getKey();
            Coordinate coordinate = entry.getValue();

            // Skip the first point
            if (prevTime == -1) {
                prevTime = stopTime;
                prevCoordinate = coordinate;
                continue;
            }

            // Calculate the extrapolated points for each second, and add into trajectoryMap
            long dTime = stopTime - prevTime;
            double dLat = (coordinate.getLat() - prevCoordinate.getLat()) / dTime;
            double dLon = (coordinate.getLon() - prevCoordinate.getLon()) / dTime;
            double prevLat = prevCoordinate.getLat();
            double prevLon = prevCoordinate.getLon();
            for (int dt = 1; dt < dTime; dt++) {
                long currentTime = prevTime + dt;
                double currentLat = prevLat + dLat;
                double currentLon = prevLon + dLon;
                Coordinate currentCoordinate = new Coordinate(currentLat, currentLon);
                newTrajectoryMap.put(currentTime, currentCoordinate);

                prevLat = currentLat;
                prevLon = currentLon;
            }
            prevCoordinate = coordinate;
            prevTime = stopTime;
        }

        return newTrajectoryMap;
    }

    /* Returns a list of hash maps of each row (column name: value) in the CSV */
    public static ArrayList<Map<String, String>> readCSV(URL path) throws IOException {
        // Prepare to read the input CSV file
        BufferedReader br = new BufferedReader(new InputStreamReader(path.openStream()));
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
