package oop.project;

import com.lynden.gmapsfx.*;
import com.lynden.gmapsfx.elevation.*;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.*;
import com.lynden.gmapsfx.zoom.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
                }
            }
        });

        //set scene and show
        stage.setScene(scene);
        stage.show();
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
    }

    /**
     * Taken from GMapsFX Example.
     */
    private void checkCenter(LatLong center) {
    }
}
