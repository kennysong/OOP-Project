package oop.project;

import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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

/**
 * JavaFX Application that uses Google Maps via GMapsFX
 */
public class MapApp extends Application implements MapComponentInitializedListener {
    // VARIABLES
    /**
     * GoogleMapView object for this application
     */
    private GoogleMapView mapComponent;

    /**
     * GoogleMap object for this application
     */
    private GoogleMap map;

    /**
     * Root layout for this application
     */
    private BorderPane rootLayout;

    /**
     * Container for trajectories
     */
    private ArrayList<Trajectory> trajectories = null;

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
        //set title of application
        stage.setTitle("MapApp");

        //initialize components
        //create map view and add listener
        mapComponent = new GoogleMapView();
        mapComponent.addMapInializedListener(this);

        //set rootLayout as border pane (use FXML later)
        rootLayout = new BorderPane();

        //set the map as center
        rootLayout.setCenter(mapComponent);

        //create the scene
        Scene scene = new Scene(rootLayout);

        //add handler for keypresses
        scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                switch (ke.getCode()) {
                    //exit on ESCAPE
                    case ESCAPE:
                        Platform.exit();
                        break;
                    case SPACE:
                        if (trajectories != null) {
                            System.out.println(trajectories.size());
                            int[] inds = {223,534,345};
                            for (int i : inds) {
                                Trajectory t = trajectories.get(i);
                                MVCArray line0 = new MVCArray();
                                for (Map.Entry<Long, Coordinate> entry : t.getTrajectory().entrySet()) {
                                    Coordinate coord = entry.getValue();
                                    LatLong point = new LatLong(coord.getLat(), coord.getLon());
                                    line0.push(point);
                                }
                                PolylineOptions polyOpts = new PolylineOptions()
                                        .path(line0)
                                        .strokeColor("red")
                                        .strokeWeight(2);
                                Polyline poly = new Polyline(polyOpts);
                                map.addMapShape(poly);
                                map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) -> {
                                    LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
                                    System.out.println("You clicked the line at (" + ll.getLatitude() + ", " + ll.getLongitude() + ")");
                                });
                            }



                            // Trajectory t = trajectories.get(0);
                            // MVCArray line0 = new MVCArray();
                            // for (Map.Entry<Long, Coordinate> entry : t.getTrajectory().entrySet()) {
                            //     Coordinate coord = entry.getValue();
                            //     LatLong point = new LatLong(coord.getLat(), coord.getLon());
                            //     line0.push(point);
                            // }
                            // PolylineOptions polyOpts = new PolylineOptions()
                            //         .path(line0)
                            //         .strokeColor("red")
                            //         .strokeWeight(2);
                            // Polyline poly = new Polyline(polyOpts);
                            // map.addMapShape(poly);
                            // map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) -> {
                            //     LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
                            //     System.out.println("You clicked the line at (" + ll.getLatitude() + ", " + ll.getLongitude() + ")");
                            // });
                        } else {
                            System.out.println("oop");
                        }
                }
            }
        });

        //set scene and show
        stage.setScene(scene);
        stage.show();

        new Thread() {
            public void run() {
                // load trajectories into memory
                // locate csv files
                URL calendarPath = App.class.getResource("bart_gtfs/calendar.csv");
                URL routesPath = App.class.getResource("bart_gtfs/routes.csv");
                URL stopTimesPath = App.class.getResource("bart_gtfs/stop_times.csv");
                URL stopsPath = App.class.getResource("bart_gtfs/stops.csv");
                URL tripsPath = App.class.getResource("bart_gtfs/trips.csv");

                trajectories = GTFSParser.parseTrips(calendarPath,
                        routesPath, stopTimesPath, stopsPath, tripsPath);
                System.out.println("trajectories loaded.");
                System.out.println(trajectories.get(0));
            }
        }.start();
    }

    /**
     * Initializes the acutal map. Automaticalled called when has been loaded.
     */
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
        //using lat lon for SF
        LatLong center = new LatLong(37.773972, -122.431297);

        //not sure what this does yet
        mapComponent.addMapReadyListener(() -> {
            // This call will fail unless the map is completely ready.
            checkCenter(center);
        });

        //map options
        MapOptions options = new MapOptions();
        options.center(center)
               .mapMarker(true)
               .zoom(10)
               .overviewMapControl(false)
               .panControl(false)
               .rotateControl(false)
               .scaleControl(false)
               .streetViewControl(false)
               .zoomControl(false)
               .mapType(MapTypeIdEnum.ROADMAP);

        //set map using above options
        map = mapComponent.createMap(options);

        //options for a marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(center)
                .title("SF")
                .animation(Animation.DROP)
                .visible(true);

        //add marker
        map.addMarker(new Marker(markerOptions));

//         //create path for a line starting at center
//         LatLong p2 = new LatLong(37.663972, -122.431297);
//         LatLong p3 = new LatLong(37.373972, -121.521297);
//         LatLong p4 = new LatLong(37.773239, -121.251297);

//         //create mvc array
//         LatLong[] ary = new LatLong[] {center, p2, p3, p4};
//         MVCArray mvc = new MVCArray(ary);

//         PolylineOptions polyOpts = new PolylineOptions()
//                 .path(mvc)
//                 .strokeColor("red")
//                 .strokeWeight(2);

//         Polyline poly = new Polyline(polyOpts);
//         map.addMapShape(poly);
//         map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) -> {
//             LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
// //            System.out.println("You clicked the line at LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
//         });
    }

    /**
     * Taken from GMapsFX Example.
     */
    private void checkCenter(LatLong center) {
    }
}
