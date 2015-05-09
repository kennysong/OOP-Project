package oop.project;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.ScheduledService;
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

import oop.project.views.SidebarController;

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
     * Single FXMLLoader for all *.fxml resources
     */
    private FXMLLoader fin = new FXMLLoader();

    /**
     * Container for each route's stops
     */
    private ArrayList<ArrayList<Stop>> routes = null;

    /**
     * Container for trajectories
     */
    private ArrayList<Trajectory> trajectories = null;

    /**
     * Timer for trajectories
     */
    // private Timer trajectoryTimer;

    /**
     * Clock that keeps track of current time for the trajectories in seconds
     */
    private long trajectoryClock;

    /**
     * Interval in seconds for each tick of the clock
     */
    private int interval = 10;

    /**
     * List of active trajectory shapes that are on the map
     */
    private ArrayList<Marker> markersOnMap = new ArrayList<Marker>();

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

        //set title of application
        stage.setTitle("MapApp");

        //disable resize (at least, for now)
        stage.setResizable(false);

        //initialize the app
        this.initialize();

        //set scene and show
        stage.setScene(this.makeScene());
        stage.show();

        //start clock and timer for trajectories
        this.startTrajectoryClock();
        this.startTrajectoryService();
    }

    /**
     * Parse GTFS data and load routes data on a background thread
     */
    private void loadRoutes() {
        Thread th = new Thread(new Task<Void>() {
            protected Void call() {
                URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");
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
     * Loads all trajectories
     */
    private void loadTrajectories() {
        System.out.println("Trajectories loading. (This will take a minute.)");
        Thread th = new Thread(new Task<Void>() {
            protected Void call() {
                URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");
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
     * Starts instance timer for trajectories.
     */
    private void startTrajectoryService() {
        ScheduledService<Void> svc = new ScheduledService<Void>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {
                        Platform.runLater(new Runnable() {
                            public void run() {
                                if (MapApp.this.trajectories != null && MapApp.this.map != null) {
                                    MapApp.this.trajectoryClock += interval;
                                    MapApp.this.updateActiveTrajectories();
                                }
                            }
                        });
                        return null;
                    }
                };
            }
        };
        svc.setPeriod(Duration.seconds(1));
        svc.start();
        // this.trajectoryTimer = new java.util.Timer();

        // this.trajectoryTimer.schedule(new TimerTask() {
        //     public void run() {
        //          Platform.runLater(new Runnable() {
        //             public void run() {
        //                 if (MapApp.this.trajectories != null && MapApp.this.map != null) {
        //                     MapApp.this.trajectoryClock += interval;
        //                     updateActiveTrajectories();
        //                 }
        //             }
        //         });
        //     }
        // }, 0, 1000);
    }

    /**
     * Updates current coordinates of trajectories
     */
    private void updateActiveTrajectories() {
        // Delete previous points
        for (Marker marker : this.markersOnMap) {
            this.map.removeMarker(marker);
        }

        System.out.println(this.trajectoryClock + " ----------------------------------");
        for (Trajectory trajectory : this.trajectories) {
            // Skip weekend services for now
            if (trajectory.getServiceId().equals("SAT") ||
                    trajectory.getServiceId().equals("SUN")) { continue; }

            // See which trajctories are active at this time
            Coordinate currentCoord = trajectory.getPosition(this.trajectoryClock);
            if (currentCoord != null) {
                System.out.println(trajectory.getTripId() + " " + currentCoord);

                // Draw train on this map as a circle
                // This is way too slow to function on my computer, but drawing Markers is okay
                // LatLong centreC = new LatLong(currentCoord.getLat(), currentCoord.getLon());
                // CircleOptions cOpts = new CircleOptions()
                //         .center(centreC)
                //         .radius(500)
                //         .strokeColor("black")
                //         .strokeWeight(2)
                //         .fillColor("black")
                //         .fillOpacity(1);
                // Circle c = new Circle(cOpts);
                // this.map.addMapShape(c);

                // Draw train on this map as a Marker
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLong(currentCoord.getLat(), currentCoord.getLon()));
                Marker marker = new Marker(markerOptions);
                this.map.addMarker(marker);
                this.markersOnMap.add(marker);
            }
        }
    }

    /**
     * Starts clock in secs for trajectories
     */
    private void startTrajectoryClock() {
        // this.trajectoryClock = GTFSParser.getElapsedTime("07:06:00");
        this.trajectoryClock = 54360;
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
            this.rootLayout = (BorderPane) this.fin.load(MapApp.class.getResource("views/root.fxml"));
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
            this.sidebar = (AnchorPane) this.fin.load(MapApp.class.getResource("views/sidebar.fxml"));
            //add sidebar controller
            this.controller = this.fin.getController();
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
                    //test (move to mapInitialized later)
                    case SPACE:
                        if (MapApp.this.routes != null) {
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
                                        .strokeWeight(5);
                                Polyline line = new Polyline(opts);
                                MapApp.this.map.addMapShape(line);
                            }
                        } else {
                            System.out.println("oop");
                        }
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

        //create and add marker for SF
        MarkerOptions markerOptions = new MarkerOptions()
                .position(center)
                .title("SF")
                .visible(true);
        map.addMarker(new Marker(markerOptions));
    }

    /**
     * Taken from GMapsFX Example.
     */
    private void checkCenter(LatLong center) {
    }
}
