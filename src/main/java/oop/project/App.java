package oop.project;


import java.net.URL;
import java.util.*;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Prints realtime info from bart GTFS-realtime to the console.
 * This is an example.
 */
public class App {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http://api.bart.gov/gtfsrt/tripupdate.aspx");
        FeedMessage feed = FeedMessage.parseFrom(url.openStream());
        System.out.println("There are " + feed.getEntityCount() + " update(s)");
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                System.out.println(entity.getTripUpdate());
                //debug; should parse and make a map
                String currentUpdate = entity.getTripUpdate().toString();
                String result = StringEscapeUtils.escapeJava(currentUpdate);
                System.out.println(result);
            }
        }
    }
}
