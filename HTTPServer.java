package com.example.bencrosoftpaint;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;

public class HTTPServer {
    private static File currentImage = new File("uploaded_image.jpg");  // Image file path
    private static final File defaultImage = new File("default_image.jpg");  // Default blank image path

    // Creates the HTTP Server
    public void createServer() throws IOException {
        // At server startup, reset to default image (or create a blank one)
        resetToDefaultImage();

        // Starts up the server to localhost:8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new ImageHandler());   // Serve images
        server.createContext("/upload", new UploadHandler()); // Upload images
        server.setExecutor(null);  // Default executor
        server.start();
        System.out.println("Server started at http://localhost:8000");
    }

    // Reset the current image to a default image (or blank image)
    private static void resetToDefaultImage() throws IOException {
        currentImage.delete();

    }

    // Handles uploading an image to the web server
    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Checks if the image file exists
                if (currentImage.exists()) {
                    // Determines the file type of the image
                    String mimeType = Files.probeContentType(currentImage.toPath());

                    // Sets the Content Type header
                    exchange.getResponseHeaders().set("Content-Type", mimeType);

                    // Sends 200 OK status with length of image file
                    exchange.sendResponseHeaders(200, currentImage.length());

                    // Reads the image file and writes it to the output stream
                    try (FileInputStream fis = new FileInputStream(currentImage);
                        OutputStream os = exchange.getResponseBody()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    // Handles if the image file doesn't exist
                    String response = "No image found.";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                // Deals with invalid request method
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handles uploading an image to the web server
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Gets the input stream from the request body
                InputStream is = exchange.getRequestBody();

                // Overwrites the current image
                FileOutputStream fos = new FileOutputStream(currentImage);

                // Reads the uploaded image and writes it to the file
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                is.close();

                // Writes response message to output stream
                String response = "Image uploaded";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Deals with invalid request method
                exchange.sendResponseHeaders(405, -1);  // Method not allowed
            }
        }
    }

    // Uploads the image to the server
    public void uploadImageToServer(File imageFile) throws Exception {
        // Determines the server URL to write the image file to
        URL url = new URL("http://localhost:8000/upload");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Writes the image file to the server
        try (OutputStream os = connection.getOutputStream();
             FileInputStream fis = new FileInputStream(imageFile)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        // Checks if the upload was successful
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Image uploaded");
        } else {
            System.out.println("Failed to upload image. Server responded with code: " + responseCode);
        }

        connection.disconnect();
    }
}
