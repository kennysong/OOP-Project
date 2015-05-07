package oop.project.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import oop.project.MapApp;

public class SidebarController {
    // VARIABLES
    /**
     * FXML id to access the title Label
     */
    @FXML
    public Label title;

    // METHODS
    /**
     * Initializes this controller. Called after FXML has been loaded.
     */
    public void initialize() {
        System.out.println("Sidebar initialized.");
    }
}
