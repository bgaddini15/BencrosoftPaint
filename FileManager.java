package com.example.bencrosoftpaint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FileManager {
    // Not visible to the user. Text element to store directory address of loaded image.
    // Used so user does not need to reopen the file chooser to save the image.
    // Using a String variable made the program unhappy.
    Text openDirectory = new Text("none");

    // Global variables
    public Text stageTitle = new Text("Untitled");
    public boolean needsSaving = false;

    // Opens an image file to display to the canvas
    public void openFile(Canvas canvas){
        // Identifies the main stage of the application
        Stage mainStage = (Stage) canvas.getScene().getWindow();

        // Creates file chooser object
        FileChooser fileChooser = new FileChooser();

        // Limits the file chooser to only choose PNG, JPEG, or BMP
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp"));

        // Sets the file chooser's initial directory to the user's Documents folder
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home + "\\Documents"));

        // Opens the file chooser window
        File file = fileChooser.showOpenDialog(mainStage);

        try{
            // Stores the file path
            String filePath = file.getAbsolutePath();
            openDirectory.setText(filePath);

            // Changes the title of the stage to show the file name
            changeTitle(file.getName().substring(0,file.getName().length()-4), canvas);

            // Loads the selected file in the background
            Image loadedImage = new Image("file:" + filePath, 400, 400, true, true);

            // Declaring variables for image scaling
            double imageHeight = loadedImage.getHeight();
            double imageWidth = loadedImage.getWidth();
            double aspectRatio = imageHeight / imageWidth;
            double scaledWidth, scaledHeight, xOffset, yOffset;

            // Constrains the image to fit within the canvas while maintaining aspect ratio
            if (Math.abs(canvas.getHeight() - imageHeight) < Math.abs(canvas.getWidth() - imageWidth)) {
                scaledHeight = canvas.getHeight();
                scaledWidth = scaledHeight / aspectRatio;
                yOffset = 0.0;
                xOffset = (canvas.getWidth() - scaledWidth) / 2.0;
            }
            else{
                scaledWidth = canvas.getWidth();
                scaledHeight = scaledWidth * aspectRatio;
                xOffset = 0.0;
                yOffset = (canvas.getHeight() - scaledHeight) / 2.0;
            }

            // Clears the canvas and displays the loaded image
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(loadedImage, xOffset, yOffset, scaledWidth, scaledHeight);
        }
        // Used to catch if a file isn't selected
        catch (Exception e){
            System.out.println("exception");
        }
    }

    // Saves the current state of the canvas over an existing image file
    public void saveFile(Canvas canvas){
        System.out.println("Save");
        if (!Objects.equals(openDirectory.getText(), "none")) {
            // Checks for I/O errors
            try {
                // Converts the loaded image to a buffered image (needed in this format to write the image data)
                BufferedImage bufferedImage = convertToBufferedImage(canvas.snapshot(null, null));

                // Creates new file object with the same address and name as the loaded image
                File file = new File(openDirectory.getText());

                // Writes the buffered image data over the loaded image
                ImageIO.write(bufferedImage, "png", file);

                // Identifies that the file has been saved
                needsSaving = false;
                changeTitle(stageTitle.getText().replace("*", ""), canvas);

            }
            // Used to check if a file isn't selected to save over
            catch (IOException e) {
                System.out.println("error");
            }
        }
        // If there was no file opened, treats Save as Save As
        else {
            saveAsFile(canvas);
        }
    }

    // Saves the current state of the canvas to a new image file
    public void saveAsFile(Canvas canvas){
        System.out.println("Save As");
        // Identifies the main stage of the application
        Stage mainStage = (Stage) canvas.getScene().getWindow();

        // Checks for I/O errors
        try {
            // Takes snapshot of the canvas
            Image imageSnapshot = canvas.snapshot(null, null);

            // Converts loaded image to buffered image (needed for image writing)
            BufferedImage bufferedImage = convertToBufferedImage(imageSnapshot);

            // Creates file chooser object
            FileChooser fileChooser = new FileChooser();

            // Limits the file chooser to PNG, JPEG, and BMP file types
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG Image", "*.jpg"),
                    new FileChooser.ExtensionFilter("BMP Image", "*.bmp"));

            // Gives a generic file name that the user can change
            fileChooser.setInitialFileName("Untitled");

            // Sets the file chooser's initial directory to the user's Downloads folder
            String home = System.getProperty("user.home");
            fileChooser.setInitialDirectory(new File(home + "\\Downloads"));

            // Opens the file chooser window to save an image. New file is created at specified location.
            File file = new File(String.valueOf(fileChooser.showSaveDialog(mainStage)));

            // Ensures a file is selected
            if (!file.getName().equals("null")){
                stageTitle.setText(file.getName().substring(0,file.getName().length()-4));

                // Image data of the buffered image is written over the new file.
                ImageIO.write(bufferedImage, "png", file);

                // Identifies that the file has been saved
                needsSaving = false;
                changeTitle(stageTitle.getText().replace("*", ""), canvas);
                System.out.println("Not null");
            }
            else{
                System.out.println("File not found");
            }

        }
        // Used to check if a directory isn't selected to save the file
        catch (IOException e) {
            System.out.println("error");
        }
    }

    // Identifies that changes have been made that need to be saved
    public void checkSaving(Canvas canvas){
        // Ensures only one asterisk is added to the front of the stage title
        if (!needsSaving){
            // Changes the stage title
            stageTitle.setText("*" + stageTitle.getText());
            Stage mainStage = (Stage) canvas.getScene().getWindow();
            mainStage.setTitle(stageTitle.getText() + " - Bencrosoft Paint");
            needsSaving = true;
        }
    }

    // Changes the stage title to indicate
    public void changeTitle(String title, Canvas canvas){
        stageTitle.setText(title);
        Stage mainStage = (Stage) canvas.getScene().getWindow();
        mainStage.setTitle(title + " - Bencrosoft Paint");
    }

    // Converts JavaFX image to a buffered image so that ImageIO can work
    private BufferedImage convertToBufferedImage(Image image) {
        // Saves the width and height dimensions of the image
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Creates new buffered image with the same dimensions as the original image
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Creates new pixel reader object to gather pixel data of the original image
        PixelReader pixelReader = image.getPixelReader();

        // Traverses every pixel in the image and sets the color data to the corresponding pixel in the buffered image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }

        return bufferedImage;
    }
}
