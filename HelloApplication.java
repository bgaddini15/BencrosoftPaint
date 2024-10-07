package com.example.bencrosoftpaint;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Establishes the fxml loader, the scene, and the controller
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 700);
        HelloController controller = fxmlLoader.getController();
        FileManager fileManager = new FileManager();

        // Starts the HTTP web server
        controller.startServer();

        // Sets the background of the border pane/scene to gray
        BackgroundFill grayBackground = new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY);
        BackgroundFill whiteBackground = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY);
        controller.borderPane.setBackground(new Background(grayBackground));
        controller.stackPane.setBackground(new Background(whiteBackground));

        // Calls the controller's Draw method when the mouse is moved on the canvas
        controller.borderPane.getChildren().get(1).setOnMouseMoved(e -> {
            controller.Draw();
            e.consume();
        });

        // Triggers when the user tries to close the program
        stage.setOnCloseRequest(windowEvent -> {
            // Prevents the window from closing unless the user permits it to
            if (controller.confirmClose()){
                windowEvent.consume();
    }});

        // Changes the icon of the program
        Image appIcon = new Image("/PaintIcon.png");
        stage.getIcons().add(appIcon);

        // Sets the title and scene and shows the stage
        stage.setTitle("Untitled - Bencrosoft Paint");
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}