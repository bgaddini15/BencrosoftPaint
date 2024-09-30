package com.example.bencrosoftpaint;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class HelloController {
    Drawer drawer = new Drawer();
    FileManager fileManager = new FileManager();
    HTTPServer serverController = new HTTPServer();

    // Stacks used to store Writable Images for undo and redo
    Stack<WritableImage> undoStack = new Stack<>();
    Stack<WritableImage> redoStack = new Stack<>();

    WritableImage copiedImage;
    WritableImage selectedImage;

    // Declaration of global variables
    double startX, startY, regionStartX, regionStartY, regionEndX, regionEndY, initialX, initialY, mouseX, mouseY, regionWidth, regionHeight;
    double lineWidth = 1.0;
    double dashes = 0.0;
    int numSides = 5;
    String paintText = "text";
    public boolean active = false;
    boolean imageCut = false;
    boolean selected = false;
    boolean drawing = false;
    boolean drawn = false;
    boolean started = false;

    @FXML public BorderPane borderPane;
    @FXML public StackPane stackPane;
    @FXML private TabPane tabPane;

    @FXML public Canvas openTempCanvas;
    @FXML private Canvas openCanvas;

    public Canvas tempCanvas = openTempCanvas;
    private Canvas canvas = openCanvas;

    @FXML public ColorPicker colorChooser;

    @FXML private CheckBox autoSaveToggle;

    @FXML private ToggleButton dashesToggle, filledToggle;

    @FXML private ToggleButton penToggle, eraseToggle, textToggle, selectToggle, lineToggle;
    @FXML private ToggleButton rectToggle, squareToggle, triangleToggle, rTriangleToggle;
    @FXML private ToggleButton ovalToggle, circleToggle;
    @FXML private ToggleButton rArrowToggle, lArrowToggle, dArrowToggle, uArrowToggle;
    @FXML private ToggleButton polygonToggle, trapezoidToggle;

    @FXML private Text lineWidthText;
    @FXML private Slider sizeSlider;

    timerHandler myTimerHandler = new timerHandler();
    Timer autoSaveTimer = new Timer();

    @FXML void enableAutoSave(){
        // Ensures that multiple timers are not running at once
        if (!started){
            autoSaveTimer.scheduleAtFixedRate(myTimerHandler, 1000, 2000);
            started = true;
        }
    }

    // Clears the canvas
    @FXML void clearCanvas(){
        // Displays an alert to confirm if the user wants to clear the canvas
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas?");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to clear the canvas?");
        alert.showAndWait();

        // If the user confirms, clears the canvas
        if (alert.getResult() == ButtonType.OK){
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    // Copies the selected region
    @FXML void handleCopy(){
        if (selected){
            // Takes a snapshot of the selection region and saves it to a Writable Image
            Rectangle2D viewPort = new Rectangle2D(startX, startY, regionEndX - startX, regionEndY - startY);
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(viewPort);
            copiedImage = canvas.snapshot(params, null);
        }
    }

    // Cuts the selected region
    @FXML void handleCut(){
        handleCopy();
        imageCut = true;
    }

    // Pastes the previously copied selected region
    @FXML void handlePaste(){
        canvas.getGraphicsContext2D().drawImage(copiedImage, 10, 10, regionEndX - startX, regionEndY - startY);

        // Removes the original image if a cut was performed
        if (imageCut){
            canvas.getGraphicsContext2D().clearRect(startX, startY, regionEndX - startX, regionEndY - startY);
        }
        selected = false;
    }

    // Erases the content in the selected region
    @FXML void handleDelete(){
        if (selectToggle.isSelected()){
            canvas.getGraphicsContext2D().clearRect(startX, startY, regionEndX - startX, regionEndY - startY);
        }
    }

    // Allows the user to undo an action
    @FXML void handleUndo(){
        // Ensures the stack is not empty
        if (!undoStack.isEmpty()){
            // Saves the top of the undo stack
            WritableImage undoImage = undoStack.pop();

            // Pushes the current state of the canvas to the redo stack
            redoStack.push(canvas.snapshot(null, null));

            // Clears the canvas and displays the top of the undo stack
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.getGraphicsContext2D().drawImage(undoImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
        else {
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    // Allows the user to redo an action
    @FXML void handleRedo(){
        // Ensures the stack is not empty
        if (!redoStack.isEmpty()){
            // Saves the top of the redo stack
            WritableImage redoImage = redoStack.pop();

            // Pushes the current state of the canvas to the undo stack
            undoStack.push(canvas.snapshot(null, null));

            // Clears the canvas and displays the top of the redo stack
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.getGraphicsContext2D().drawImage(redoImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    // Actions performed by the line width slider
    @FXML void handleSlider() {
        // Text that displays the current line width rounded to the nearest integer
        lineWidthText.setText(((int) Math.round(sizeSlider.getValue())) + "px");

        // Sets the line width based on the position of the slider
        lineWidth = sizeSlider.getValue();
    }

    // Action performed by pressing Help menu item
    @FXML void handleHelpItem() {
        // Creates a new alert screen
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Sets the title and context of the alert screen
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.setContentText("""
                To open a file: File-Open...
                To save over a file: File-Save
                To save a new file: File-Save As...
                To draw: Select Pen and drag mouse on canvas
                To erase: Select Erase and drag mouse on canvas
                To draw a shape: Select desired shape button and drag mouse on canvas
                To change line width: Drag Line Width slider left or right
                To change color: Select Color Chooser
                To create a cup of coffee: Find a different Java""");

        // Displays the alert window
        alert.show();
    }

    // Action performed by pressing About menu item
    @FXML void handleAboutItem() throws Exception {
        // Creates a new text area to display a string
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        try {
            // Reads in the content from ReleaseNotes.txt to the text area
            File releaseNotes = new File("src\\main\\resources\\ReleaseNotes.txt");
            BufferedReader reader = new BufferedReader(new FileReader(releaseNotes));

            String st;

            while ((st = reader.readLine()) != null) {
                textArea.appendText(st + "\n");
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        // Creates a pop-up alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Sets the dimensions of the alert window
        alert.getDialogPane().setPrefWidth(700);
        alert.getDialogPane().setPrefHeight(600);

        // Sets the title, graphic, and content of the screen
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.getDialogPane().setGraphic(null);
        alert.getDialogPane().setContent(textArea);

        // Displays the alert screen
        alert.show();
    }

    // Action performed by pressing Open... menu item
    @FXML void handleOpenItem() {
        fileManager.openFile(canvas);
    }

    // Action performed by pressing Save As... menu item
    @FXML void handleSaveAsItem() {
        fileManager.saveAsFile(canvas);
    }

    // Action performed by pressing Save menu item
    @FXML public void handleSaveItem() {
        fileManager.saveFile(canvas);
        uploadFile();
    }

    // Action performed by pressing Draw checkbox
    @FXML void handlePressed(ActionEvent e){
        // An array of all the different drawing options
        ToggleButton[] toggleButtons = { penToggle, eraseToggle, textToggle, selectToggle, lineToggle, rectToggle, squareToggle,
                triangleToggle, rTriangleToggle, ovalToggle, circleToggle, rArrowToggle, lArrowToggle, uArrowToggle, dArrowToggle,
                polygonToggle, trapezoidToggle };

        // Stores the id of which button is pressed
        ToggleButton source = (ToggleButton) e.getSource();

        // Cycles through each button option
        for (ToggleButton button : toggleButtons) {
            // Deselects all of the buttons other than the one selected
            if (button != source && source.isSelected()) {
                button.setSelected(false);
            }
        }

        if (source.isSelected()){
            if (source == polygonToggle){
                setNumSides();
            }
            else if (source == textToggle){
                setPaintText();
            }

            if (source != penToggle && source != eraseToggle && source != selectToggle){
                drawing = true;
            }
        }

        // Boolean value used to determine if a drawing feature is currently enabled (helps with smart save)
        active = source.isSelected();
    }

    // Handles dashes and fill of shapes
    @FXML void handleDash(ActionEvent e){
        // Determines which button was pressed
        ToggleButton source = (ToggleButton) e.getSource();

        // If dashes button is enabled, disables the filled button and activates dashes
        if (source == dashesToggle && source.isSelected()){
            filledToggle.setSelected(false);
            dashes = 2.0;
        }
        // If filled button is enabled, disables the dashes button and removes dashes from the line
        else if (source == filledToggle && source.isSelected()){
            dashesToggle.setSelected(false);
            dashes = 0.0;
        }
        // If neither button is activated, disables dashes
        else{
            dashes = 0.0;
        }
    }

    // Allows the user to resize the canvas
    @FXML void resizeCanvas(){
        // Creates alert box
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resize Canvas");
        alert.setHeaderText("Enter dimensions of canvas:");
        alert.setGraphic(null);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Creates text fields to allow user to input canvas size
        TextField widthField = new TextField();
        TextField heightField = new TextField();

        // Labels for the text fields
        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");

        // Sets the text fields to show the canvas's current size
        widthField.setText(Double.toString(canvas.getWidth()));
        heightField.setText(Double.toString(canvas.getHeight()));

        // Adds labels and text fields to the grid
        grid.add(widthLabel, 0, 0);
        grid.add(heightLabel, 0, 1);
        grid.add(widthField, 1, 0);
        grid.add(heightField, 1, 1);

        // Adds the grid to the alert
        alert.getDialogPane().setContent(grid);

        // Displays the alert
        alert.showAndWait();

        // Only changes the canvas size if the user selects the OK button
        if (alert.getResult() == ButtonType.OK){
            try {
                // Sets the canvas size to match the user input
                canvas.setWidth(Double.parseDouble(widthField.getText()));
                canvas.setHeight(Double.parseDouble(heightField.getText()));

                // Sets the temp canvas size to the save dimensions
                tempCanvas.setWidth(canvas.getWidth());
                tempCanvas.setHeight(canvas.getHeight());

                // Sets the stack pane container to the save dimensions (used for background)
                stackPane.setPrefWidth(canvas.getWidth());
                stackPane.setPrefHeight(canvas.getHeight());

            }
            // Ensures valid input
            catch (NumberFormatException e) {
                // Displays an alert informing the user of invalid input
                Alert invalidAlert = new Alert(Alert.AlertType.INFORMATION);
                invalidAlert.setTitle("Invalid input");
                invalidAlert.setHeaderText(null);
                invalidAlert.setContentText("Invalid input. Please enter a number to specify canvas dimensions.");
                invalidAlert.showAndWait();

                // Reopens the resize canvas alert if the user presses OK
                if (invalidAlert.getResult() == ButtonType.OK){
                    resizeCanvas();
                }
            }
        }
    }

    // Creates a new tab to the tab pane
    @FXML void createNewTab(){
        // Creates the tab and sets its properties
        PaintTab newTab = new PaintTab();
        newTab.setText("Untitled Tab");
        newTab.setClosable(true);

        // Creates nodes that go into new tab
        Canvas newCanvas = new Canvas(1200, 550);
        Canvas newTempCanvas = new Canvas(1200, 550);
        StackPane newStackPane = new StackPane();

        // Sets properties of the nodes
        newTempCanvas.setCursor(Cursor.CROSSHAIR);
        BackgroundFill whiteBackground = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY);
        newStackPane.setBackground(new Background(whiteBackground));
        newStackPane.setPrefWidth(1200);
        newStackPane.setPrefHeight(550);

        // Adds the new canvases to the new stack pane
        newStackPane.getChildren().add(newCanvas);
        newStackPane.getChildren().add(newTempCanvas);

        // Adds the new stack pane to a new anchor pane
        AnchorPane newAnchorPane = new AnchorPane();
        newAnchorPane.getChildren().add(newStackPane);

        // Adds the anchor pane to the new tab
        newTab.setContent(newAnchorPane);

        // Activates when a tab is switched
        newTab.setOnSelectionChanged(this::switchTabs);

        newTab.setOnCloseRequest(event -> {
            if (confirmClose()){
                event.consume();
            }
        });

        // Adds the new tab to the tab pane
        tabPane.getTabs().add(newTab);

        // Sets the new tab to be active
        tabPane.getSelectionModel().select(newTab);
    }

    // Allows the user to upload the image file to the server manually
    @FXML void handleUpload(){
        uploadFile();
    }

    // Actions performed from switching tabs
    @FXML void switchTabs(Event event){
        // Determines which tab was selected
        Tab selectedTab = (Tab) event.getSource();
        if (selectedTab.isSelected()){
            // Sets the editable canvas/tempCanvas to the canvases of the selected tab
            AnchorPane activeAnchorPane = (AnchorPane) tabPane.getSelectionModel().getSelectedItem().getContent();
            StackPane activeStackPane = (StackPane) activeAnchorPane.getChildren().getFirst();
            Canvas activeCanvas = (Canvas) activeStackPane.getChildren().getFirst();
            Canvas activeTempCanvas = (Canvas) activeStackPane.getChildren().get(1);
            canvas = activeCanvas;
            tempCanvas = activeTempCanvas;
        }
    }

    // Allows the user to provide text to draw on the canvas
    private void setPaintText(){
        // Creates custom button and label
        ButtonType okay = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        Label sidesText = new Label("Enter text: ");

        // Creates a text field for the user to enter the text
        TextField textField = new TextField();
        textField.setPrefWidth(200);

        // Creates a grid pane to organize the alert window
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(textField, 1, 0);
        grid.add(sidesText, 0, 0);

        // Creates an alert and adds the elements to it
        Alert alert = new Alert(Alert.AlertType.NONE, "Enter text:", okay);
        alert.setTitle("Draw Text");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(grid);

        // Displays the alert
        alert.showAndWait();

        // When the button is pressed, sets the text to the user input
        if (alert.getResult() == okay){
            paintText = textField.getText();
        }
    }

    // Displays an alert box that allows the user to set the number of sides for a regular polygon
    private void setNumSides(){
        // Creates custom button and label
        ButtonType okay = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        Label sidesText = new Label("Enter number of sides: ");

        // Creates a text field for the user to enter the number of sides
        TextField textField = new TextField();
        textField.setText(Integer.toString(numSides));
        textField.setPrefWidth(50);

        // Creates a grid pane to organize the alert window
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(textField, 1, 0);
        grid.add(sidesText, 0, 0);

        // Creates an alert and adds the elements to it
        Alert alert = new Alert(Alert.AlertType.NONE, "Enter number of sides:", okay);
        alert.setTitle("Polygon Sides");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(grid);

        // Displays the alert
        alert.showAndWait();

        // When the button is pressed, sets the number of sides to the number inputted by the user
        if (alert.getResult() == okay){
            try{
                numSides = Integer.parseInt(textField.getText());
            }
            // Default number of sides is 5
            catch (NumberFormatException exception){
                numSides = 5;
            }
        }
    }

    // Uses the mouse to draw a line on the canvas
    public void Draw() {
        // Tool used to manipulate the canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        GraphicsContext tempGC = tempCanvas.getGraphicsContext2D();

        // Determines the starting position of the mouse
        tempCanvas.setOnMousePressed(event -> {
            if (!selected){
                startX = event.getX();
                startY = event.getY();
            }
            if (selected) {
                if (event.getX() >= regionStartX && event.getX() <= regionEndX && event.getY() >= regionStartY && event.getY() <= regionEndY) {
                    System.out.println("in region");
                    mouseX = event.getX() - startX;
                    mouseY = event.getY() - startY;
                }
                /*else {
                    selected = false;
                }*/
            }
        });

        tempCanvas.setOnMouseReleased(event -> {
            System.out.println(selected);
            SetGC(gc);

            // Only check saving if an action button is selected
            if (active){
                fileManager.checkSaving(canvas);
                fileManager.checkSaving(tabPane);
                undoStack.push(canvas.snapshot(null, null));
            }

            if (drawn){

            }

            // Allows the user to create dots by clicking when in pen or erase mode
            if (event.getX() == startX && event.getY() == startY){
                if (penToggle.isSelected() || eraseToggle.isSelected()){
                    gc.setLineDashes(0);
                    if (eraseToggle.isSelected()){
                        gc.setStroke(Color.WHITE);
                    }
                    drawer.drawLine(gc, startX, startY, event.getX(), event.getY());
                }
            }

            // If a region has been selected, draws the selected region to the canvas at the location of the mouse
            if (selected){
                selected = false;
                gc.drawImage(selectedImage, event.getX() - mouseX, event.getY() - mouseY);
            }
            // Special actions performed with the Select feature
            else if (selectToggle.isSelected()){
                // Determines new coordinates of the selected region
                regionEndX = event.getX();
                regionEndY = event.getY();
                initialX = event.getX();
                initialY = event.getY();
                regionWidth = regionEndX - startX;
                regionHeight = regionEndY - startY;

                // Displays the bounding box on the screen
                changeCanvas(tempGC, event);

                // Saves the selected region as a writable image
                Rectangle2D viewPort = new Rectangle2D(startX, startY, regionEndX - startX, regionEndY - startY);
                SnapshotParameters params = new SnapshotParameters();
                params.setViewport(viewPort);
                selectedImage = canvas.snapshot(params, null);

                // Handles boolean values
                selected = true;
                selectToggle.setSelected(false);
            }
            else {
                changeCanvas(gc, event);
                tempGC.clearRect(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight());
            }

        });

        // Checks for dragging of the mouse on the canvas
        tempCanvas.setOnMouseDragged(event -> {
            tempGC.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            SetGC(tempGC);

            // Determines if draw or erase mode is selected
            if (penToggle.isSelected() || eraseToggle.isSelected()){
                // Sets the properties of the Graphics Context
                SetGC(gc);
                gc.setLineDashes(0);

                // Uses a white pen to "erase" (ideal because the canvas is transparent and would otherwise match the background color of the window)
                if (eraseToggle.isSelected()){
                    gc.setStroke(Color.WHITE);
                }

                // Draws a line
                drawer.drawLine(gc, startX, startY, event.getX(), event.getY());

                // Changes the starting position of the mouse to allow for a smooth line
                startX = event.getX();
                startY = event.getY();
            }
            else changeCanvas(tempGC, event);

            if (drawing){
                drawn = false;
            }

            // If a region has been selected...
            if (selected){
                // Shows the selected region actively moving
                tempGC.clearRect(startX, startY, regionEndX - startX, regionEndY - startY);
                tempGC.drawImage(selectedImage, event.getX() - mouseX, event.getY() - mouseY);

                /*tempGC.setStroke(Color.LIGHTGRAY);
                tempGC.setLineDashes(3);
                tempGC.setLineWidth(2);

                drawer.drawRectangle(tempGC, event.getX() - mouseX, event.getY() - mouseY,
                        event.getX() + (regionWidth - mouseX), event.getY() + (regionHeight - mouseY), false);
*/
                // Clears the original location of the selected region
                gc.clearRect(startX, startY, initialX - startX, initialY - startY);

                // Stores the new position of the selected region in the temp canvas
                regionEndX = startX + event.getX();
                regionEndY = startY + event.getY();
                regionStartX = event.getX();
                regionStartY = event.getY();

            }
        });

        tempCanvas.setOnMouseMoved(event -> {
            // Displays the width and color of the pen/eraser over the cursor even when no draw is being performed
            if (penToggle.isSelected() || eraseToggle.isSelected()){
                tempGC.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                SetGC(tempGC);
                if (eraseToggle.isSelected()){
                    tempGC.setStroke(Color.WHITE);
                }
                tempGC.setLineDashes(0);
                drawer.drawLine(tempGC, event.getX(), event.getY(), event.getX(), event.getY());
            }
        });
    }

    // Uses the mouse to determine start and end positions for Drawer functions
    private void changeCanvas(GraphicsContext gc, MouseEvent event) {
        if (lineToggle.isSelected()) {
            drawer.drawLine(gc, startX, startY, event.getX(), event.getY());
        }
        else if (rectToggle.isSelected()) {
            drawer.drawRectangle(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (squareToggle.isSelected()) {
            drawer.drawSquare(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (trapezoidToggle.isSelected()){
            drawer.drawTrapezoid(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (triangleToggle.isSelected()){
            drawer.drawTriangle(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (rTriangleToggle.isSelected()){
            drawer.drawRightTriangle(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (ovalToggle.isSelected()) {
            drawer.drawOval(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (circleToggle.isSelected()){
            drawer.drawCircle(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (polygonToggle.isSelected()){
            drawer.drawPolygon(gc, startX, startY, event.getX(), event.getY(), numSides, filledToggle.isSelected());
        }
        else if (rArrowToggle.isSelected()){
            drawer.drawRightArrow(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (lArrowToggle.isSelected()){
            drawer.drawLeftArrow(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (uArrowToggle.isSelected()){
            drawer.drawUpArrow(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (dArrowToggle.isSelected()){
            drawer.drawDownArrow(gc, startX, startY, event.getX(), event.getY(), filledToggle.isSelected());
        }
        else if (textToggle.isSelected()){
            drawer.drawText(gc, startX, startY, event.getX(), event.getY(), paintText, filledToggle.isSelected());
        }
        else if (selectToggle.isSelected()){
            // Uses a light gray rectangle to display the selected region
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineDashes(3);
            gc.setLineWidth(2);
            drawer.drawRectangle(gc, startX, startY, event.getX(), event.getY(), false);
        }
    }

    /**
     * Displays an alert asking the user if they want to save their work before closing the application.
     * <p></p>
     * The user can choose to Save the current state of the canvas and close the application, Don't Save the
     * current state of the canvas and close the application, or Cancel closing the application and don't save the
     * current state of the canvas.
     * @return a boolean value depending on which button the user selects
     * @see public void saveFile()
     */
    // Informs the user if they are about to close the program without saving
    public boolean confirmClose(){
        if (fileManager.needsSaving){
            // Creates custom button types
            ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.YES);
            ButtonType noSave = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            // Creates the alert dialog box
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save your work?", save, noSave, cancel);
            alert.setTitle("Save Changes?");
            alert.setHeaderText(null);

            // Displays the alert dialog box
            alert.showAndWait();

            // Saves the file if user selects save button
            if (alert.getResult() == save){
                handleSaveItem();
                // Still closes the window
                return false;
            }
            // Closes the window if user selects Don't Save. Keeps it open if user selects Cancel
            else return alert.getResult() != noSave;
        }
        else return false;
    }

    // Sets the properties of the graphics context
    private void SetGC(GraphicsContext graphicsContext){
        // Sets the line width based on the slider
        graphicsContext.setLineWidth(lineWidth);

        // Sets the color to that of the color picker
        graphicsContext.setStroke(colorChooser.getValue());
        graphicsContext.setFill(colorChooser.getValue());
        //gc.setFill(Color.TRANSPARENT);
        // TODO: allow fill color to be different from line color

        // Makes sure the line is rounded (removes jagged edges)
        graphicsContext.setLineCap(StrokeLineCap.ROUND);

        // Lines meet at a corner, not a rounded corner or bevel
        graphicsContext.setLineJoin(StrokeLineJoin.MITER);

        // Sets the line dashes based on the line width (ensures an attractive ratio)
        // If dashes are disabled, dashes = 0 and the line is solid
        graphicsContext.setLineDashes(lineWidth * dashes);
    }

    // Starts/creates a HTTP server to show image data
    public void startServer() throws IOException {
        serverController.createServer();
    }

    // Uploads the open image file to the HTTP server
    private void uploadFile(){
        try {
            System.out.println("Uploading file");

            // Identifies the image to upload
            File serverImageFile = new File(fileManager.openDirectory.getText());

            // Uploads the image to the server
            serverController.uploadImageToServer(serverImageFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Upload didn't work");
        }
    }

    class PaintTab extends Tab{
        boolean needsSaving;

        PaintTab(){
            needsSaving = false;
        }

        public boolean getNeedsSaving(){
            return needsSaving;
        }

        public void setNeedsSaving(boolean state){
            needsSaving = state;
        }
    }

    class timerHandler extends TimerTask {

        @Override
        public void run() {
            // Saves the canvas when auto save is enabled
            if (autoSaveToggle.isSelected()){
                Platform.runLater( () -> handleSaveItem());
            }
        }
    }
}