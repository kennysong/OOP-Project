package oop.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.transit.realtime.GtfsRealtime.*;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.*;
import com.twilio.sdk.*;

/**
 * Prints realtime info from bart GTFS-realtime to the console.
 * This is an example of a single entity:
 *
 * trip {                   //TripUpdate.getTrip();
 *   trip_id: "66R11"       //TripDescriptor.getTrip();
 * }
 * stop_time_update {       //TripUpdate.getStopTimeUpdate(i);
 *   stop_sequence: 1       //StopTimeUpdate.getStopSequence();
 *   departure {            //StopTimeUpdate.getDeparture();
 *     delay: 0             //StopTimeEvent.getDelay();
 *     uncertainty: 30      //StopTimeEvent.getUncertainty();
 *   }
 *   stop_id: "FRMT"        //StopTimeUpdate.getStopId();
 * }
 */
public class App {
    /**
     * Prints a list line by line
     */
    public static void printList(List<?> list) {
        for (Object element : list) {
            System.out.println(element);
        }
        System.out.println(list.size() + " elements.");
    }

    public static void main(String[] args) throws Exception {
        // Test our SMSSender class!
        SMSSender.sendSMS("+12016321315", "Sent from App.java.");

        //load trajectories into memory
        //locate csv files
        URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
        URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
        URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
        URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
        URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");

        System.out.print("loading trajectories...");
        ArrayList<Trajectory> trajectories = GTFSParser.parseTrips(calendarPath,
                routesPath, stopTimesPath, stopsPath, tripsPath);
        System.out.println("done");

        FeedListener bartTrips = new FeedListener("http://api.bart.gov/gtfsrt/tripupdate.aspx");
        Thread updateThread = new Thread(bartTrips);
        updateThread.start();
        List<FeedEntity> entities;

        while (true) {
            if (bartTrips.hasNew) {
                System.out.println("YES");
                entities = bartTrips.getEntities();
                printList(entities);
                System.out.println("YES");
                bartTrips.hasNew = false;
            }
        }
    }
}
