package com.example.bencrosoftpaint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

public class Drawer {

    // Draws a line from the starting position to the ending position
    public void drawLine(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        gc.strokeLine(startX, startY, endX, endY);
    }

    // Draws a rectangle with points determined by start and end positions
    public void drawRectangle(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double[] xPoints = {startX, startX, endX, endX};
        double[] yPoints = {startY, endY, endY, startY};

        if (filled){
            gc.fillPolygon(xPoints, yPoints, 4);
        }
        else{
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }

    // Draws a square based on specified start and end positions
    public void drawSquare(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double[] xPoints;
        double[] yPoints;

        // Determines the height and width of a rectangle formed by start and end positions
        double width = endX - startX;
        double height = endY - startY;

        // If the width is less than the height, determines y-coordinates of the square based on the x-coordinates
        if (Math.abs(width) < Math.abs(height)) {
            xPoints = new double[]{startX, endX, endX, startX};

            if ((width / (Math.abs(width))) == (height / (Math.abs(height)))) {
                yPoints = new double[]{startY, startY, startY + width, startY + width};
            }
            else{
                yPoints = new double[]{startY, startY, startY - width, startY - width};
            }
        }
        // If the height is less than the width, determines x-coordinates of the square based on the y-coordinates
        else{
            yPoints = new double[]{startY, endY, endY, startY};

            if ((height / (Math.abs(height))) == (width / (Math.abs(width)))) {
                xPoints = new double[]{startX, startX, startX + height, startX + height};
            }
            else{
                xPoints = new double[]{startX, startX, startX - height, startX - height};
            }
        }

        // Displays the square on the canvas
        if (filled){
            gc.fillPolygon(xPoints, yPoints, 4);
        }
        else {
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }

    // Draws an isosceles triangle based on specified start and end positions
    public void drawTriangle(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        // Determines the x-points of the triangle to have one half-way between the others
        double[] triangleXPoints = {startX, endX, startX + ((endX - startX) / 2)};

        // Determines the y-points of the triangle to have the central point of the triangle pointing vertically
        double[] triangleYPoints = {endY, endY, startY};

        // Draws the triangle on the canvas
        if (filled){
            gc.fillPolygon(triangleXPoints, triangleYPoints, 3);
        }
        else {
            gc.strokePolygon(triangleXPoints, triangleYPoints, 3);
        }
    }

    // Draws a right triangle with the start and end positions marking the hypotenuse
    public void drawRightTriangle(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled){
        // Determines x points of right triangle
        double[] xPoints = {startX, endX, startX};

        // Determines y points of right triangle
        double[] yPoints = {startY, endY, endY};

        // Draws the right triangle on the canvas
        if (filled){
            gc.fillPolygon(xPoints, yPoints, 3);
        }
        else {
            gc.strokePolygon(xPoints, yPoints, 3);
        }
    }

    // Draws an oval based on specified start and end positions
    public void drawOval(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        double ovalX = Math.min(endX, startX);
        double ovalY = Math.min(endY, startY);

        if (filled){
            gc.fillOval(ovalX, ovalY, width, height);
        }
        else {
            gc.strokeOval(ovalX, ovalY, width, height);
        }
    }

    // Draws a circle based on specified start and end positions
    public void drawCircle(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double ovalX, ovalY;

        // Determines the height and width of a rectangle formed by the start and end positions
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        // If the width is less than the height, uses the width to determine the dimensions of the circle
        if (width < height){
            height = width;
            ovalX = Math.min(endX, startX);
            if (endY < startY){
                ovalY = startY - width;
            }
            else{
                ovalY = startY;
            }
        }

        // If the height is less than the width, uses the height to determine the dimensions of the circle
        else {
            width = height;
            ovalY = Math.min(endY, startY);
            if (endX < startX){
                ovalX = startX - height;
            }
            else{
                ovalX = startX;
            }
        }

        // Draws the circle on the canvas
        if (filled){
            gc.fillOval(ovalX, ovalY, width, height);
        }
        else{
            gc.strokeOval(ovalX, ovalY, width, height);
        }
    }

    // Hopefully a future shape, not yet implemented
    public void drawStar(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        // What if you get the points of a pentagon using drawPentagon, then draw another pentagon smaller
        // and rotated to use as the inner points of the star?
    }

    // Draws a trapezoid given a specified start and end positions
    public void drawTrapezoid(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double width = endX - startX;

        double[] xPoints = {startX + width * 0.25, startX + width * 0.75, endX, startX};
        double[] yPoints = {startY, startY, endY, endY};

        if (filled){
            gc.fillPolygon(xPoints, yPoints, 4);
        }
        else{
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }

    // Draws an arrow pointing right based on specified start and end positions
    public void drawRightArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double arrowXStart = Math.min(endX, startX);
        double width = Math.abs(endX - startX);

        // Determines the coordinates of each point of the arrow
        double[] arrowXPoints = { arrowXStart + (width / 2), arrowXStart + (width / 2), Math.max(endX, startX),
                arrowXStart + (width / 2), arrowXStart + (width / 2), arrowXStart, arrowXStart};
        double[] arrowYPoints = {startY + (endY - startY) * 0.25, startY, startY + ((endY - startY) / 2),
                endY, startY + (endY - startY) * 0.75, startY + (endY - startY) * 0.75,
                startY + (endY - startY) * 0.25};

        // Draws the arrow on the canvas
        if (filled){
            gc.fillPolygon(arrowXPoints, arrowYPoints, 7);
        }
        else {
            gc.strokePolygon(arrowXPoints, arrowYPoints, 7);
        }
    }

    // Draws an arrow pointing left based on specified start and end positions
    public void drawLeftArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double arrowXStart = Math.max(endX, startX);
        double width = -Math.abs(endX - startX);

        // Determines the coordinates of each point of the arrow
        double[] arrowXPoints = { arrowXStart + (width / 2), arrowXStart + (width / 2), Math.min(endX, startX),
                arrowXStart + (width / 2), arrowXStart + (width / 2), arrowXStart, arrowXStart};
        double[] arrowYPoints = {startY + (endY - startY) * 0.25, startY, startY + ((endY - startY) / 2),
                endY, startY + (endY - startY) * 0.75, startY + (endY - startY) * 0.75,
                startY + (endY - startY) * 0.25};

        // Draws the arrow on the canvas
        if (filled){
            gc.fillPolygon(arrowXPoints, arrowYPoints, 7);
        }
        else {
            gc.strokePolygon(arrowXPoints, arrowYPoints, 7);
        }
    }

    // Draws an arrow pointing up based on specified start and end positions
    public void drawUpArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double arrowYStart = Math.max(endY, startY);
        double height = -Math.abs(endY - startY);

        // Determines the coordinates of each point of the arrow
        double[] arrowYPoints = { arrowYStart + (height / 2), arrowYStart + (height / 2), Math.min(endY, startY),
                arrowYStart + (height / 2), arrowYStart + (height / 2), arrowYStart, arrowYStart};
        double[] arrowXPoints = {startX + (endX - startX) * 0.25, startX, startX + ((endX - startX) / 2),
                endX, startX + (endX - startX) * 0.75, startX + (endX - startX) * 0.75, startX + (endX - startX) * 0.25};

        // Draws the arrow on the canvas
        if (filled){
            gc.fillPolygon(arrowXPoints, arrowYPoints, 7);
        }
        else {
            gc.strokePolygon(arrowXPoints, arrowYPoints, 7);
        }
    }

    // Draws an arrow pointing down based on specified start and end positions
    public void drawDownArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, boolean filled) {
        double arrowYStart = Math.min(endY, startY);
        double height = Math.abs(endY - startY);

        // Determines the coordinates of each point of the arrow
        double[] arrowYPoints = { arrowYStart + (height / 2), arrowYStart + (height / 2), Math.max(endY, startY),
                arrowYStart + (height / 2), arrowYStart + (height / 2), arrowYStart, arrowYStart};
        double[] arrowXPoints = {startX + (endX - startX) * 0.25, startX, startX + ((endX - startX) / 2),
                endX, startX + (endX - startX) * 0.75, startX + (endX - startX) * 0.75, startX + (endX - startX) * 0.25};

        // Draws the arrow on the canvas
        if (filled){
            gc.fillPolygon(arrowXPoints, arrowYPoints, 7);
        }
        else {
            gc.strokePolygon(arrowXPoints, arrowYPoints, 7);
        }
    }

    // Draws a regular polygon of a variable number of sides
    public void drawPolygon(GraphicsContext gc, double startX, double startY, double endX, double endY, int numSides, boolean filled) {
        // Determines the width and height of a rectangle specified by starting and ending points
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        // Determines the radius of a circle made by the start and end points
        double radius = Math.min(width, height) / 2.0;

        // Variables used to position the polygon on the canvas
        double translateX = radius;
        double translateY = radius;

        // Ensures the polygon is drawn properly depending on which quadrant the end points are relative to the starting points
        if (endX < startX){
            translateX = -radius;
        }
        if (endY < startY){
            translateY = -radius;
        }

        // Arrays used to hold the coordinates of the polygon's vertices
        double[] xPoints = new double[numSides];
        double[] yPoints = new double[numSides];

        // Math used to determine the vertex coordinates
        for (int i = 0; i < numSides; i++) {
            xPoints[i] = (radius * Math.sin((2 * Math.PI * i) / numSides)) + startX + translateX;
            yPoints[i] = -(radius * Math.cos((2 * Math.PI * i) / numSides)) + startY + translateY;
        }

        // Draws the polygon on the canvas
        if (filled){
            gc.fillPolygon(xPoints, yPoints, numSides);
        }
        else{
            gc.strokePolygon(xPoints, yPoints, numSides);
        }
    }

    public void drawText(GraphicsContext gc, double startX, double startY, double endX, double endY, String text, boolean filled) {
        Font newFont = new Font(Math.abs(endY - startY));
        gc.setFont(newFont);
        gc.setLineWidth(1.0);

        if (endX < startX){
            if (filled){
                gc.fillText(text, endX, endY);
            }
            else{
                gc.strokeText(text, endX, endY);
            }
        }
        else{
            if (filled){
                gc.fillText(text, startX, endY);
            }
            else{
                gc.strokeText(text, startX, endY);
            }
        }
    }
}