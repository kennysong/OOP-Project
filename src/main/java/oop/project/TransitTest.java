package oop.project;

import java.util.*;

public class TransitTest {
    /* Some basic tests for the GTFSParser class */
    public static void main(String[] args) {
        // Load and instantiate a Transit object
        System.out.println("Creating new Transit instance.");
        Transit BART = new Transit();

        // Print out a trajectory
        System.out.println("Printing out a trajectory from Transit.trajectories.");
        ArrayList<Trajectory> trajectories = BART.getTrajectories();
        System.out.println(trajectories.get(0));

        // Print out all stops in a route
        System.out.println("Printing out all stops in a route from Transit.stopsByRoute.");
        ArrayList<ArrayList<Stop>> stopsByRoute = BART.getStopsByRoute();
        ArrayList<Stop> route = stopsByRoute.get(0);
        for (Stop stop : route) {
            System.out.println(stop);
        }
    }
}
