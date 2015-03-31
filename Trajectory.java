import java.util.*;

public class Trajectory {
    // Private instance variables for a Trajectory object
    private String tripId;
    private String serviceId;
    private SortedMap<Long, Coordinate> trajectory;

    /* Constructor for a Trajectory */
    public Trajectory(String tripId, String serviceId, SortedMap<Long, Coordinate> trajectory) {
        this.tripId = tripId;
        this.serviceId = serviceId;
        this.trajectory = trajectory;
    }

    /* Returns a coordinate for the given time, null if doesn't exist */
    public Coordinate getPosition(long time) {
        Coordinate position = this.trajectory.get(time);
        return position;
    }

    /* Returns boolean of if the trajectory is active at a specific time */
    public boolean isActive(long time) {
        if (this.getPosition(time) != null) {
            return true;
        }
        return false;
    }
}
