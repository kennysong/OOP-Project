package oop.project;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
    // private ArrayList<Trajectory> trajectories = null;

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
        //load BART routes
        this.loadRoutes();
        //set title of application
        stage.setTitle("MapApp");
        //disable resize (at least, for now)
        stage.setResizable(false);
        //initialize the app
        this.initialize();
        //set scene and show
        stage.setScene(this.makeScene());
        stage.show();
    }

    /**
     * Parse GTFS data and load routes data on a background thread
     */
    private void loadRoutes() {
        Thread th = new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");
                routes = GTFSParser.getStopsByRoute(calendarPath,
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
                    //test
                    case SPACE:
                        if (routes != null) {
                            double offset = 0.000100;
                            int i = 0;
                            for (ArrayList<Stop> route : routes) {
                                //get color of current route
                                String color = route.get(0).getColor();
                                //build array
                                MVCArray lineArray = new MVCArray();
                                for (Stop s : route) {
                                    Coordinate coord = s.getCoord();
                                    LatLong loc = new LatLong(coord.getLat() + offset*i, coord.getLon());
                                    lineArray.push(loc);
                                }
                                PolylineOptions opts = new PolylineOptions()
                                        .path(lineArray)
                                        .strokeColor(color)
                                        .strokeWeight(5);
                                Polyline line = new Polyline(opts);
                                map.addMapShape(line);
                                i++;
                            }
                        } else {
                            System.out.println("oop");
                        }
                    //     if (trajectories != null) {
                    //         System.out.println(trajectories.size());
                    //         int[] inds = {223,534,345};
                    //         for (int i : inds) {
                    //             Trajectory t = trajectories.get(i);
                    //             MVCArray line0 = new MVCArray();
                    //             for (Map.Entry<Long, Coordinate> entry : t.getTrajectory().entrySet()) {
                    //                 Coordinate coord = entry.getValue();
                    //                 LatLong point = new LatLong(coord.getLat(), coord.getLon());
                    //                 line0.push(point);
                    //             }
                    //             PolylineOptions polyOpts = new PolylineOptions()
                    //                     .path(line0)
                    //                     .strokeColor("red")
                    //                     .strokeWeight(2);
                    //             Polyline poly = new Polyline(polyOpts);
                    //             map.addMapShape(poly);
                    //             map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) -> {
                    //                 LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
                    //                 System.out.println("You clicked the line at (" + ll.getLatitude() + ", " + ll.getLongitude() + ")");
                    //             });
                    //         }
                    //     } else {
                    //         System.out.println("oop");
                    //     }
                }
            }
        });
        //return scene
        return s;
    }

    /**
     * Initializes the acutal map. Automaticalled called when has been loaded.
     */
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
        //using lat lon for SF
        LatLong center = new LatLong(37.773972, -122.431297);

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
               .zoom(13)
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

    /**
     * Convenience method to convert from Coordinate to LatLong
     * @param   coord   a Coordinate Object
     * @return          a LatLong Object
     */
    private static LatLong convertCoord(Coordinate coord) {
        return new LatLong(coord.getLat(), coord.getLon());
    }

}
