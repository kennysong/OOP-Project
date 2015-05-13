package oop.project.view;

import javafx.fxml.FXML;
import javafx.concurrent.Worker.State;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import oop.project.MapApp;

public class SidebarController {
    // VARIABLES
    /**
     * FXML id to access the station Label
     */
    @FXML
    public Label station;

    /**
     * FXML id to access the text RadioButton
     */
    @FXML
    public RadioButton textRadio;

    /**
     * FXML id to access the call RadioButton
     */
    @FXML
    public RadioButton callRadio;

    /**
     * FXML id to radio button ToggleGroup
     */
    @FXML
    public ToggleGroup radioGroup;

    /**
     * FXML id to access notification time TextField
     */
    @FXML
    public TextField notificationTime;

    /**
     * FXML id to access phone number TextField
     */
    @FXML
    public TextField phoneNumber;

    /**
     * FXML id to access the confirm Button
     */
    @FXML
    public Button confirmButton;

    /**
     * FXML id to access the summary Label
     */
    @FXML
    public Label summary;

    /**
     * Reference to the main app
     */
    private MapApp mainApp;

    // METHODS
    /**
     * Initializes this controller. Called after FXML has been loaded.
     */
    public void initialize() {
        System.out.println("Sidebar initialized.");
    }

    /**
     * Method called by main app to give this controller a reference to it
     * @param   app     the main app
     */
    public void setMainApp(MapApp app) {
        this.mainApp = app;
    }

    /**
     * Creates a summary of the current information and putis it in this.summary
     */
    @FXML
    private void summarize() {
        //only allow confirmation when stop is selected, one radio button is
        //selected, and the phone number is valid
        if (this.validStop() && this.validRadio() && this.validTime() && this.validPhone()) {
            if (this.mainApp.notificationTask != null && this.mainApp.notificationTask.getState() != State.SUCCEEDED) {
                this.mainApp.notificationTask.cancel();
            }
            String opt = "";
            if (this.textRadio.isSelected()) {
                opt = "text";
                this.mainApp.launchTextTask(Integer.parseInt(this.notificationTime.getText()));
            } else {
                opt = "call";
                this.mainApp.launchCallTask(Integer.parseInt(this.notificationTime.getText()));
            }
            this.summary.setText("Got it. I will " + opt + " you at " + this.phoneNumber.getText() +
                                 " " + this.notificationTime.getText() + " minutes before your train arrives!");
        } else {
            this.summary.setText("Oop! Before I can let you know when to leave, make sure you've done the following:" +
                                 "\n1. Selected a stop" +
                                 "\n2. Selected 'Text Me' or 'Call Me'" +
                                 "\n3. Entered a notification time" +
                                 "\n4. Entered your phone number");
        }
    }

    /**
     * Checks if a stop has been selected in the main app
     * @return          true if mainApp.selectedStop is not null; false otherwise
     */
    private boolean validStop() {
        return (this.mainApp.getSelectedStop() != null);
    }

    /**
     * Checks if the radio buttons are valid
     * @return          true if one of the radio buttons are selected; false otherwise
     */
    private boolean validRadio() {
        return (this.textRadio.isSelected() || this.callRadio.isSelected());
    }

    /**
     * Checks if the given notification time is valid
     * @return          true if the input text is a number between 1 and 10 inclusive; false otherwise
     */
    private boolean validTime() {
        String time = this.notificationTime.getText();
        if (time.isEmpty()) {
            return false;
        } else {
            try {
                int t = Integer.parseInt(time);
                if (1 <= t && t <= 10) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * Checks if the given phone number is in the correct format
     * @return          true if the input text is a string of numbers; false otherwise
     */
    private boolean validPhone() {
        if (this.phoneNumber.getText().isEmpty()) {
            return false;
        } else {
            String number = this.phoneNumber.getText().substring(1);
            return number.matches("[0-9]+");
        }
    }
}
