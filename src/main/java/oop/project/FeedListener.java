package oop.project;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * Creates an object that continuously reads a BART GTFS Realtime Feed
 */
public class FeedListener implements Runnable {
    // VARIABLES
    /**
     * Amount in milliseconds to wait between requests
     */
    private final int SLEEP_TIME = 60000; //60000 = 1 minute

    /**
     * Location of a GTFS Realtime Feed
     */
    private URL url;

    /**
     * Container for the feed entities
     */
    private List<FeedEntity> currentEntities;

    /**
     * Flag for whether or not there is new data in this.currentEntities
     */
    public volatile boolean hasNew;

    // CONSTRUCTOR
    /**
     * Creates this listener
     * @param   url     string representing the location of a GTFS Realtime Feed
     */
    public FeedListener(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            this.url = null;
        }
        try {
            this.currentEntities = this.getEntityList();
        } catch (IOException e) {
            e.printStackTrace();
            this.currentEntities = new ArrayList<FeedEntity>();
        }
        this.hasNew = true;
        System.out.println("Created FeedListener on " + url);
    }

    // METHODS
    /**
     * Continually reads a GTFS Realtime Feed and updates itself if needed
     */
    public void run() {
        //ensure the listening url is valid
        while (this.url != null) {
            //sleep for specified amount of time
            try {
                Thread.sleep(SLEEP_TIME);
                System.out.print("REQUESTING...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //get the feed and check to see if there is new data
            try {
                List<FeedEntity> temp = this.getEntityList();
                if (this.isOutdated(temp)) {
                    System.out.println("different!");
                    this.currentEntities = temp;
                    this.hasNew = true;
                } else {
                    System.out.println("no updates");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads a GTFS Realtime Feed and returns a list of FeedEntity Objects
     * @return          a list of FeedEntity Objects
     */
    private List<FeedEntity> getEntityList() throws IOException {
        return FeedMessage.parseFrom(this.url.openStream()).getEntityList();
    }

    /**
     * Checks whether or not this.currentEntities is outdated by comparing against a new feed
     * @param   newList a list of FeedEntity Objects
     * @return          true if there are differences, false otherwise
     */
    private boolean isOutdated(List<FeedEntity> newList) {
        if (this.currentEntities.size() != newList.size()) {
            return true;
        } else {
            //create hash set for the entity ids in the new list and compare with current
            HashSet<String> newListIdSet = this.getEntityIdSet(newList);
            for (FeedEntity entity : this.currentEntities) {
                String id = entity.getId();
                if (newListIdSet.contains(id)) {
                    newListIdSet.remove(id);
                } else {
                    return true;
                }
            }
            // set should be empty if lists are the same
            if (newListIdSet.size() != 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns a hash set of a FeedMessage Object's enity ids
     * @param   fm      FeedMessage Object containing many entities
     * @return          a hash set containing entity ids
     */
    private static HashSet<String> getEntityIdSet(List<FeedEntity> list) {
        HashSet<String> set = new HashSet<String>();
        for (FeedEntity entity : list) {
            set.add(entity.getId());
        }
        return set;
    }

    /**
     * Returns this.currentEntities
     * @return          a list of FeedEntity Objects
     */
    public List<FeedEntity> getEntities() {
        return this.currentEntities;
    }
}
