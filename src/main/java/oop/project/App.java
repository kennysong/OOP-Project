package oop.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

/**
 * Prints realtime info from bart GTFS-realtime to the console.
 * This is an example.
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
    public static void main(String[] args) throws Exception {
        System.out.println("URL");
        //get updates from bart
        URL url = new URL("http://api.bart.gov/gtfsrt/tripupdate.aspx");
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());
        //loop through all message entities
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                TripUpdate update = entity.getTripUpdate();
                System.out.println(update);

                // TripDescriptor td = update.getTrip();
                // System.out.println(td.getTripId());

                // StopTimeUpdate stu = update.getStopTimeUpdate(0);
                // System.out.println(stu.getStopSequence());
                // StopTimeEvent dep = stu.getDeparture();
                // System.out.println(dep.getDelay());
                // System.out.println(dep.getUncertainty());
                // System.out.println(stu.getStopId());
            }
        }
        System.out.println("There were " + feed.getEntityCount() + " update(s)");

        System.out.println("GTFS");
        try {
            // Open all the GTFS CSV files
            URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
            URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
            URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
            URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
            URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");

            System.out.print("loading trajectories...");
            // Get the trajectories for all the trips
            //only load into memory for now
            ArrayList<Trajectory> trajectories = GTFSParser.parseTrips(calendarPath,
                    routesPath, stopTimesPath, stopsPath, tripsPath);
            System.out.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
