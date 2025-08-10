package io.github.frostzie.datapackide.screen;

import io.github.frostzie.datapackide.utils.LoggerProvider;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

public class JavaFXTestWindow {

    private static final Logger LOGGER = LoggerProvider.INSTANCE.getLogger("JavaFXTestWindow");
    private static Stage primaryStage = null;
    private static boolean fxInitialized = false;

    public static void initializeJavaFX() {
        if (!fxInitialized) {
            System.setProperty("javafx.allowSystemPropertiesAccess", "true");
            try {
                Platform.startup(() -> {
                    Platform.setImplicitExit(false);
                    fxInitialized = true;
                    createWindow();
                    LOGGER.info("JavaFX Platform initialized and window pre-created!");
                });
            } catch (IllegalStateException e) {
                fxInitialized = true;
                Platform.runLater(() -> {
                    Platform.setImplicitExit(false);
                    createWindow();
                    LOGGER.info("JavaFX Platform was already initialized, window pre-created!");
                });
            }
        }
    }

    public static void showTestWindow() {
        if (!fxInitialized) {
            initializeJavaFX();
            return;
        }
        Platform.runLater(() -> {
            if (primaryStage == null) {
                createWindow();
            }
            primaryStage.show();
            primaryStage.toFront();
            LOGGER.info("JavaFX Test Window shown!");
        });
    }

    private static void createWindow() {
        if (primaryStage != null) return;
        Stage stage = new Stage();
        Rectangle whiteBox = new Rectangle(200.0, 150.0, Color.WHITE);
        whiteBox.setX(10.0);
        whiteBox.setY(10.0);
        Rectangle redBorder = new Rectangle(220.0, 170.0, Color.TRANSPARENT);
        redBorder.setStroke(Color.RED);
        redBorder.setStrokeWidth(2.0);
        redBorder.setX(0.0);
        redBorder.setY(0.0);
        Pane root = new Pane();
        root.getChildren().addAll(redBorder, whiteBox);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
        Scene scene = new Scene(root, 240.0, 200.0);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Minecraft Mod JavaFX Test");
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.UTILITY);
        stage.setX(100.0);
        stage.setY(100.0);

        stage.setOnCloseRequest(e -> {
            e.consume();
            LOGGER.info("Close button pressed, hiding window...");
            Platform.runLater(() -> {
                stage.hide();
                LOGGER.info("Window hidden via close button!");
            });
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ESCAPE")) {
                LOGGER.info("ESC pressed, hiding window...");
                Platform.runLater(() -> {
                    stage.hide();
                    LOGGER.info("Window hidden via ESC key!");
                });
            }
        });

        primaryStage = stage;
        LOGGER.info("JavaFX Test Window created (hidden)!");
    }

    public static void hideTestWindow() {
        Platform.runLater(() -> {
            if (primaryStage != null && primaryStage.isShowing()) {
                primaryStage.hide();
                LOGGER.info("JavaFX Test Window hidden via hideTestWindow()!");
            }
        });
    }

    public static boolean isWindowVisible() {
        return primaryStage != null && primaryStage.isShowing();
    }

    public static void toggleTestWindow() {
        if (!fxInitialized) {
            LOGGER.info("JavaFX not initialized yet, initializing...");
            initializeJavaFX();
            return;
        }
        Platform.runLater(() -> {
            if (primaryStage == null) {
                createWindow();
            }
            if (primaryStage.isShowing()) {
                LOGGER.info("Window is showing, hiding it...");
                primaryStage.hide();
                LOGGER.info("JavaFX Test Window hidden!");
            } else {
                primaryStage.show();
                primaryStage.toFront();
                LOGGER.info("JavaFX Test Window shown!");
            }
        });
    }
}