package com.neovaccine;

/**
 * MainApp.java — The ENTRY POINT of our JavaFX application
 * =========================================================
 * Think of this like the front door of a building.
 * Every time someone runs our app, they walk through this door first.
 *
 * JavaFX applications MUST extend the Application class.
 * The start() method is called automatically when the app launches.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    // ── CONSTANTS ──────────────────────────────────────────────────────
    public static final String APP_TITLE   = "NeoVaccine Designer v1.0";
    public static final double WINDOW_W    = 1280;  // Window width  in pixels
    public static final double WINDOW_H    = 800;   // Window height in pixels
    public static final String BACKEND_URL = "http://localhost:5000";  // Python server

    /**
     * start() is called by JavaFX automatically when the app launches.
     * 
     * @param primaryStage — Think of Stage as the app WINDOW.
     *                       Like a theater stage where our app "performs".
     */
    @Override
    public void start(Stage primaryStage) throws IOException {

        // ── STEP 1: Load the FXML layout file ──────────────────────────
        // FXML is like an HTML file but for JavaFX.
        // It describes WHAT UI elements exist (buttons, tables, etc.)
        // The controller handles WHAT HAPPENS when you click them.
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/neovaccine/fxml/MainView.fxml")
        );

        // ── STEP 2: Create a Scene (the "canvas" inside the stage) ─────
        // Scene holds all the UI elements.
        // Think of Stage = window frame, Scene = what's inside the window.
        Scene scene = new Scene(loader.load(), WINDOW_W, WINDOW_H);

        // ── STEP 3: Apply CSS styling ───────────────────────────────────
        // CSS makes our app look beautiful — dark theme, colors, fonts.
        scene.getStylesheets().add(
            Objects.requireNonNull(
                getClass().getResource("/com/neovaccine/css/dark-theme.css")
            ).toExternalForm()
        );

        // ── STEP 4: Configure the window ───────────────────────────────
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();  // Opens in the center of the screen

        // ── STEP 5: Show the window! ────────────────────────────────────
        primaryStage.show();
    }

    /**
     * main() — The very first method Java calls.
     * It just passes control to JavaFX's launch() method.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
