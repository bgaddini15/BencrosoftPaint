package com.example.bencrosoftpaint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class Rotator {
    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_HORIZONTAL = -1;

    // Creates a rotation transform to a graphics context with a given angle and starting coordinate
    private void rotate(GraphicsContext gc, double angle, double px, double py){
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }

    // Rotates the content of the graphics context
    public void rotateImage(GraphicsContext gc, Image image, double angle, double tlpx, double tlpy){
        gc.save();
        rotate(gc, angle, tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2);
        gc.drawImage(image, tlpx, tlpy);
        gc.restore();
    }

    // Flips the content of the graphics context along a specified direction (h or v)
    public void flipImage(GraphicsContext gc, Image image, int direction, double tlpx, double tlpy){
        double xScale = 1.0;
        double yScale = 1.0;

        // Saves the current state of the graphics context
        gc.save();

        // Sets scaling and positioning of transform (-1.0 means mirrored)
        switch(direction) {
            case FLIP_HORIZONTAL:
                xScale = -1.0;
                // Translate to the bottom of the canvas
                gc.translate(image.getWidth(), 0);
                break;
            case FLIP_VERTICAL:
                yScale = -1.0;
                // Translate to the bottom of the canvas
                gc.translate(0, image.getHeight());
                break;
        }

        // Flips the image
        gc.scale(xScale, yScale);

        // Redraw the content of the canvas
        gc.drawImage(image, tlpx, tlpy);

        // Restore the graphics context state
        gc.restore();
    }
}
