package oop.project;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import com.lynden.gmapsfx.*;
import com.lynden.gmapsfx.elevation.*;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.Animation;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.javascript.object.MapShape;
import com.lynden.gmapsfx.shapes.*;
import com.lynden.gmapsfx.zoom.*;

import oop.project.model.*;
import oop.project.view.SidebarController;
import oop.project.util.*;

/**
 * JavaFX Application that uses Google Maps via GMapsFX
 */
public class MapApp extends Application implements MapComponentInitializedListener {
    // VARIABLES
    /**
     * Root layout for this application
     */
    private BorderPane rootLayout;

    /**
     * Pane for the map
     */
    private GoogleMapView mapComponent;

    /**
     * Actual map to be shown
     */
    private GoogleMap map;

    /**
     * Pane for the sidebar
     */
    private AnchorPane sidebar;

    /**
     * Controller for the sidebar
     */
    private SidebarController controller;

    /**
     * Container for each route's stops
     */
    private ArrayList<ArrayList<Stop>> routes = null;

    /**
     * User selected stop
     */
    private Stop selectedStop = null;

    /**
     * Container for trajectories
     */
    private ArrayList<Trajectory> trajectories = null;

    /**
     * Scheduled service to update the trajectories
     */
    private ScheduledService<Void> trajectoryService;

    /**
     * Scheduled service to continually get realtime update
     */
    private ScheduledService<Void> bartService;

    /**
     * Task to check future trajectory times until notification can be sent
     */
    public Task notificationTask = null;

    /**
     * Container for the feed entities
     */
    private List<FeedEntity> currentEntities = null;

    /**
     * Clock that keeps track of current time for the trajectories in seconds
     */
    private long trajectoryClock;

    /**
     * Interval in seconds for each tick of the clock, set in startTrajectoryClock()
     */
    private int clockInterval;

    /**
     * List of active trajectory markers that are on the map
     */
    private ArrayList<Marker> markersOnMap = new ArrayList<Marker>();

    /**
     * List of active trips from the real time feed
     */
    private ArrayList<String> feedActiveTrips = new ArrayList<String>();

    /**
     * Toggle for if the map is real time or not
     */
    private boolean isRealTime = false;

    // METHODS
    /**
     * Launches this application
     * @param   args    string command line arguements
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Standard start method for JavaFX Applications
     */
    public void start(Stage stage) {
        //load BART routes and trajectories
        this.loadRoutes();
        this.loadTrajectories();

        //start clock for trajectories
        this.startTrajectoryClock();

        //create and start services
        this.createTrajectoryService();
        this.createBartService();
        this.trajectoryService.start();
        this.bartService.start();

        //initialize the app
        this.initialize();

        //set scene and show
        stage.setTitle("BART Interactive Visualization");
        stage.setResizable(false);
        stage.setScene(this.makeScene());
        stage.show();
    }

    /**
     * Parse GTFS data and load routes data on a background thread
     */
    private void loadRoutes() {
        Thread th = new Thread(new Task<Void>() {
            protected Void call() {
                URL calendarPath = MapApp.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = MapApp.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = MapApp.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = MapApp.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = MapApp.class.getResource("bart_gtfs/trips.csv");
                MapApp.this.routes = GTFSParser.getStopsByRoute(calendarPath,
                                                    routesPath,
                                                    stopTimesPath,
                                                    stopsPath,
                                                    tripsPath);
                System.out.println("Stops by route loaded.");
                return null;
            }
        });
        th.setDaemon(true);
        th.start();
    }

    /**
     * Creates a task to text a user about a train some amount of minutes before it arrives
     * @param   num     the phone number should be texted
     * @param   min     the number of minutes before a train arrives
     */
    public void launchTextTask(String num, int min) {
        this.notificationTask = new Task<Void>() {
            protected Void call() {
                boolean done = false;
                while (!done) {
                    try {
                        Thread.sleep(1000);
                        for (Trajectory trajectory : MapApp.this.trajectories) {
                            // Skip weekend services for now
                            if (trajectory.getServiceId().equals("SAT") ||
                                    trajectory.getServiceId().equals("SUN")) { continue; }

                            if (trajectory.getRouteId() == MapApp.this.selectedStop.getRouteId()) {
                                // See which trajctories are active at this time
                                String tripId = trajectory.getTripId();
                                Coordinate currentCoord = trajectory.getPosition(MapApp.this.trajectoryClock + 60*min);
                                if (currentCoord != null) {
                                    double d = MapApp.this.distanceBetween(currentCoord, MapApp.this.selectedStop.getCoord());
                                    System.out.println(d);
                                    if (d < 0.0001) {
                                        System.out.println("sending text to " + num);
                                        SMSSender.sendSMS(num, "Sent from MapApp.java.");
                                        done = true;
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        Thread th = new Thread(this.notificationTask);
        th.setDaemon(true);
        th.start();
    }

    /**
     * Creates a task to call a user about a train some amount of minutes before it arrives
     * @param   num     the phone number should be texted
     * @param   min     the number of minutes before a train arrives
     */
    public void launchCallTask(String num, int min) {
        this.notificationTask = new Task<Void>() {
            protected Void call() {
                //switch used to continue loop; set to true after notification has been sent.
                boolean done = false;
                while (!done) {
                    try {
                        Thread.sleep(1000);
                        for (Trajectory trajectory : MapApp.this.trajectories) {
                            // Skip weekend services for now
                            if (trajectory.getServiceId().equals("SAT") ||
                                    trajectory.getServiceId().equals("SUN")) { continue; }

                            if (trajectory.getRouteId() == MapApp.this.selectedStop.getRouteId()) {
                                // See which trajctories are active at this time
                                String tripId = trajectory.getTripId();
                                Coordinate currentCoord = trajectory.getPosition(MapApp.this.trajectoryClock + 60*min);
                                if (currentCoord != null) {
                                    double d = MapApp.this.distanceBetween(currentCoord, MapApp.this.selectedStop.getCoord());
                                    System.out.println(d);
                                    if (d < 0.0001) {
                                        System.out.println("sending call to " + num);
                                        MakeCall.makeCall(num);
                                        done = true;
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        Thread th = new Thread(this.notificationTask);
        th.setDaemon(true);
        th.start();
    }

    /**
     * Loads all trajectories
     */
    private void loadTrajectories() {
        System.out.println("Trajectories loading. (This will take a minute.)");
        Thread th = new Thread(new Task<Void>() {
            protected Void call() {
                URL calendarPath = MapApp.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = MapApp.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = MapApp.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = MapApp.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = MapApp.class.getResource("bart_gtfs/trips.csv");
                MapApp.this.trajectories = GTFSParser.parseTrips(calendarPath,
                                                          routesPath,
                                                          stopTimesPath,
                                                          stopsPath,
                                                          tripsPath);
                System.out.println("Trajectories loaded.");
                return null;
            }
        });
        th.setDaemon(true);
        th.start();
    }

    /**
     * Creates a scheduled service to update the trajectories every second
     */
    private void createTrajectoryService() {
        this.trajectoryService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Platform.runLater(new Runnable() {
                            public void run() {
                                if (MapApp.this.trajectories != null && MapApp.this.map != null &&
                                    MapApp.this.bartService.getState() != Worker.State.RUNNING) {
                                    MapApp.this.updateActiveTrajectories();
                                }
                            }
                        });
                        return null;
                    }
                };
            }
        };
        this.trajectoryService.setPeriod(Duration.seconds(1));
    }

    /**
     * Creates a scheduled service to get bart realtime updates every 5 seconds
     */
    private void createBartService() {
        this.bartService = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        // If not real time, don't bother fetching the data
                        if (!MapApp.this.isRealTime) { return null; }

                        // Fetch data from BART feed
                        try {
                            URL url = new URL("http://api.bart.gov/gtfsrt/tripupdate.aspx");
                            MapApp.this.currentEntities = FeedMessage.parseFrom(url.openStream()).getEntityList();
                        } catch (Exception e) { e.printStackTrace(); }

                        // Update feedActiveTrips with the latest feed data
                        MapApp.this.feedActiveTrips = new ArrayList<String>();
                        for (FeedEntity entity : MapApp.this.currentEntities) {
                            MapApp.this.feedActiveTrips.add(entity.getId());
                        }

                        return null;
                    }
                };
            }
        };
        this.bartService.setPeriod(Duration.seconds(5));
    }

    /**
     * Updates trajectoryClock and current coordinates of trajectories
     */
    private void updateActiveTrajectories() {
        // Update trajectory clock
        this.trajectoryClock += this.clockInterval;

        // Delete previous points
        for (Marker marker : this.markersOnMap) {
            this.map.removeMarker(marker);
        }

        // System.out.println("Current trajectory clock: " + this.trajectoryClock);
        for (Trajectory trajectory : this.trajectories) {
            // Skip weekend services for now
            if (trajectory.getServiceId().equals("SAT") ||
                    trajectory.getServiceId().equals("SUN")) { continue; }

            // See which trajctories are active at this time
            String tripId = trajectory.getTripId();
            Coordinate currentCoord = trajectory.getPosition(this.trajectoryClock);
            if (currentCoord != null) {
                // If real time, match active trip IDs to the latest feed data
                if (this.isRealTime) {
                    if (!this.feedActiveTrips.contains(tripId)) {
                        System.out.println("GTFS scheduled trip not in real time feed: " + tripId);
                        continue;
                    }
                }

                // Draw train on this map as a Marker
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLong(currentCoord.getLat(), currentCoord.getLon()));
                Marker marker = new Marker(markerOptions);
                this.map.addMarker(marker);
                this.markersOnMap.add(marker);

                // Check which trains are currently very near a stop
                // for (ArrayList<Stop> route : this.routes) {
                //     for (Stop stop : route) {
                //         if (trajectory.getRouteId() != stop.getRouteId()) { continue; }

                //         // Check if current coordinates are a stop coordinate
                //         LatLong stopLatLong = stop.getCoord().toLatLong();
                //         LatLong currentLatLong = currentCoord.toLatLong();
                //         double d = this.distanceBetween(stopLatLong, currentLatLong);
                //         if (d < 0.0000001) {
                //             String stopTime = this.unixToHourMin(this.trajectoryClock * 1000);
                //             System.out.println("Stop Announcement: Train " + tripId + " is arriving at stop " + stop.getName() + " at " + stopTime + ".");
                //             // TextToSpeech.speak("Train " + tripId + " is arriving at stop " + stop.getName() + " at " + stopTime + ".");
                //         }
                //     }
                // }
            }
        }
    }

    /**
     * Starts clock in secs for trajectories and sets this.clockInterval
     */
    private void startTrajectoryClock() {
        if (this.isRealTime) {
            // Set clock to current time in SF
            Calendar c = new GregorianCalendar(TimeZone.getTimeZone("PST"));
            String timeStr = String.format("%02d:%02d:%02d", c.get(Calendar.HOUR),
                                    c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
            this.trajectoryClock = GTFSParser.getElapsedTime(timeStr);
            System.out.println("Current time in SF: " + timeStr + " (" + this.trajectoryClock + ")");

            // Set clock interval to 1 second for real time animation
            this.clockInterval = 1;
        } else {
            // Set clock to 8:00am SF time
            this.trajectoryClock = GTFSParser.getElapsedTime("07:06:00");

            // Set clock interval to 10 seconds for faster animation
            this.clockInterval = 10;
        }
    }

    /**
     * Initializes the root layout, sidebar, and map
     */
    private void initialize() {
        //load root layout
        this.loadRoot();
        //load sidebar and controller
        this.loadSidebar();
        //add sidebar to the right
        this.rootLayout.setRight(this.sidebar);
        // create map
        this.initMap();
        // add map to the center
        this.rootLayout.setCenter(this.mapComponent);
    }

    /**
     * Loads the root layout from root.fxml
     */
    private void loadRoot() {
        try {
            FXMLLoader fin = new FXMLLoader();
            fin.setLocation(MapApp.class.getResource("view/root.fxml"));
            this.rootLayout = (BorderPane) fin.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Load the sidebar from sidebar.fxml
     */
    private void loadSidebar() {
        try {
            FXMLLoader fin = new FXMLLoader();
            fin.setLocation(MapApp.class.getResource("view/sidebar.fxml"));
            this.sidebar = (AnchorPane) fin.load();
            //add sidebar controller
            this.controller = fin.getController();
            this.controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Initializes the map
     */
    private void initMap() {
        this.mapComponent = new GoogleMapView();
        this.mapComponent.addMapInializedListener(this);
    }

    /**
     * Makes a scene from this.rootLayout
     */
    private Scene makeScene() {
        //create scene
        Scene s = new Scene(this.rootLayout);
        //add handler for keypresses
        s.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                switch (ke.getCode()) {
                    //exit on ESCAPE
                    case ESCAPE:
                        Platform.exit();
                        break;
                }
            }
        });
        //return scene
        return s;
    }

    /**
     * Initializes the actual map. Automatically called when has been loaded.
     */
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
        //using lat lon for SF
        // LatLong center = new LatLong(37.773972, -122.431297);
        LatLong center = new LatLong(37.733972, -122.251297);

        //not sure what this does yet
        this.mapComponent.addMapReadyListener(() -> {
            // This call will fail unless the map is completely ready.
            checkCenter(center);
        });

        //map options
        MapOptions options = new MapOptions();
        options.center(center)
               .mapMarker(true)
               .mapType(MapTypeIdEnum.ROADMAP)
               .mapTypeControl(true)
               .overviewMapControl(false)
               .panControl(true)
               .rotateControl(false)
               .scaleControl(false)
               .streetViewControl(true)
               .zoom(11)
               .zoomControl(true);
        //set map using above options
        this.map = this.mapComponent.createMap(options);

        //draw lines
        for (ArrayList<Stop> route : MapApp.this.routes) {
            //get color of current route
            String color = route.get(0).getColor();
            //build array
            MVCArray lineArray = new MVCArray();
            for (Stop s : route) {
                Coordinate coord = s.getCoord();
                LatLong loc = new LatLong(coord.getLat(), coord.getLon());
                lineArray.push(loc);
            }
            PolylineOptions opts = new PolylineOptions()
                    .path(lineArray)
                    .strokeColor(color)
                    .strokeWeight(4);
            Polyline line = new Polyline(opts);
            MapApp.this.map.addMapShape(line);
            MapApp.this.map.addUIEventHandler(line, UIEventType.click, (JSObject obj) -> {
                LatLong click = new LatLong((JSObject) obj.getMember("latLng"));
                MapApp.this.selectedStop = MapApp.this.findStop(click, route);
                MapApp.this.controller.station.setText(MapApp.this.selectedStop.getName());
            });
        }
    }

    /**
     * Finds and returns the closest Stop to a location, from a list of Stops
     * @param   loc     a LatLong Object representing a location
     * @param   stops   a list of Stops
     * @return          the Stop closest to loc
     */
    private Stop findStop(LatLong loc, ArrayList<Stop> stops) {
        Stop closestStop = stops.get(0);
        double stopDistance = this.distanceBetween(loc, closestStop.getCoord().toLatLong());
        for (int i = 1; i < stops.size(); i++) {
            Stop tmpStop = stops.get(i);
            double tmpDistance = this.distanceBetween(loc, tmpStop.getCoord().toLatLong());
            if (tmpDistance < stopDistance) {
                closestStop = tmpStop;
                stopDistance = tmpDistance;
            }
        }
        return closestStop;
    }

    /**
     * Calculates the distance between LatLong coordinates using equirectangular approximation
     * @param   loc1    the first LatLong Object
     * @param   loc2    the second LatLong Object
     */
    private double distanceBetween(LatLong loc1, LatLong loc2) {
        double dLat, dLon;
        dLat = loc2.latToRadians() - loc1.latToRadians();
        dLon = (loc2.longToRadians() - loc1.longToRadians())*Math.cos(0.5*(loc1.latToRadians() + loc2.latToRadians()));
        return Math.sqrt(dLat*dLat + dLon*dLon);
    }

    /**
     * Calculates the distance between Coordinate objects using equirectangular approximation
     * @param   loc1    the first Coordinate Object
     * @param   loc2    the second Coordinate Object
     */
    private double distanceBetween(Coordinate loc1, Coordinate loc2) {
        double dLat, dLon;
        dLat = Math.toRadians(loc2.getLat()) - Math.toRadians(loc1.getLat());
        dLon = (Math.toRadians(loc2.getLon()) - Math.toRadians(loc1.getLon()))*Math.cos(0.5*(Math.toRadians(loc2.getLat()) + Math.toRadians(loc1.getLat())));
        return Math.sqrt(dLat*dLat + dLon*dLon);
    }

    /**
     * Taken from GMapsFX Example.
     */
    private void checkCenter(LatLong center) {
    }

    /**
     * Converts a unix time to a HH:MM time
     */
    private String unixToHourMin(long unixTime) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("PST"));
        c.setTimeInMillis(unixTime);
        return String.format("%02d:%02d", c.get(Calendar.HOUR), c.get(Calendar.MINUTE));
    }

    /**
     * Returns the this.selectedStop. If no stop is selected, null is returned.
     * @return          the selected Stop
     */
    public Stop getSelectedStop() {
        return this.selectedStop;
    }
}
