public class Coordinate {
    // Private instance variables for a Coordinate object
    private double lat;
    private double lon;

    /* Constructor for a Coordinate */
    public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /* Returns the latitude of the Coordinate */
    public double getLat() {
        return this.lat;
    }

    /* Returns the longitude of the Coordinate */
    public double getLon() {
        return this.lon;
    }
}
