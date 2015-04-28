package oop.project;

import java.net.URL;
import java.util.*;
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
        URL url = new URL("http://api.bart.gov/gtfsrt/tripupdate.aspx");
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());
        System.out.println(feed);
        for (FeedEntity entity : feed.getEntityList()) {
            System.out.println(entity);
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
        System.out.println("There are " + feed.getEntityCount() + " update(s)");
    }
}
