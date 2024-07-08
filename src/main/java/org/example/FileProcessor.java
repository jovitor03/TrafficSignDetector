package org.example;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * FileProcessor class is responsible for processing files.
 */
public class FileProcessor {

    /**
     * Clears all files in the specified folder.
     *
     * @param folderPath the path of the folder to be cleared
     */
    public static void clearFolder(String folderPath) {
        File folder = new File(folderPath);
        // Check if the specified path is a valid directory
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        // Delete the file
                        boolean success = file.delete();
                        // Print a message if the file deletion fails
                        if (!success) {
                            System.out.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        // Print a message if the specified path is not a valid directory
        else {
            System.out.println("The specified path is not a valid directory.");
        }
    }

    /**
     * Maintains the format of the file name by appending "_result" before the extension.
     *
     * @param filePath the path of the file
     * @return the file name with "_result" appended before the extension
     */
    public static String maintainFormat(String filePath) {
        // Extract the extension of the file
        String extension = "";
        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }

        // Extract the file name without the extension
        String fileNameWithoutExtension = "";
        int j = filePath.lastIndexOf(File.separator);
        if (j >= 0 && i > j) {
            fileNameWithoutExtension = filePath.substring(j + 1, i);
        } else if (i > 0) {
            fileNameWithoutExtension = filePath.substring(0, i);
        }

        // Append "_result" before the extension
        return fileNameWithoutExtension + "_result." + extension;
    }

    /**
     * Handles the selection of an image file.
     *
     * @param frame     the frame to display the image
     * @param imagesDir the directory containing the images
     */
    public static void handleImageSelection(JFrame frame, String imagesDir) {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser(new File(imagesDir));
        int returnValue = fileChooser.showOpenDialog(null);
        // Check if the user selects a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Get the relative path of the selected file
            String relativePath = imagesDir + "/" + selectedFile.getName();

            Mat originalImage = Imgcodecs.imread(relativePath);
            // Display the original image
            ImageProcessor.displayImage(frame, ImageProcessor.matToResizedBufferedImage(originalImage));

            // Create a timer to detect traffic signs after 3 seconds
            Timer timer = getTimer(frame, relativePath);
            timer.start();
        }
    }

    /**
     * Creates a timer to detect traffic signs in the image after 3 seconds.
     *
     * @param frame        the frame to display the detected image
     * @param relativePath the relative path of the image
     * @return the timer
     */
    private static Timer getTimer(JFrame frame, String relativePath) {
        Timer timer = new Timer(3000, event -> {
            Mat resultImage = TrafficSignDetector.detectTrafficSigns(relativePath);
            // Display the detected image if not null
            if (resultImage != null) {
                ImageProcessor.displayDetectedImage(frame, ImageProcessor.matToResizedBufferedImage(resultImage), relativePath);
            }
        });
        // Set the timer to run only once
        timer.setRepeats(false);
        return timer;
    }

    /**
     * Handles the selection of a video file.
     *
     * @param frame     the frame to display the video
     * @param videosDir the directory containing the videos
     */
    public static void handleVideoSelection(JFrame frame, String videosDir) {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser(new File(videosDir));
        int returnValue = fileChooser.showOpenDialog(null);
        // Check if the user selects a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String relativePath = getString(videosDir, fileChooser);

            // Remove the image from the frame
            ImageProcessor.removeImage(frame);

            // Create a progress bar
            JProgressBar progressBar = VideoProcessor.createProgressBar(frame);
            // Add the progress bar to the frame
            frame.add(progressBar, BorderLayout.SOUTH);

            // Start a new thread to detect traffic signs in the video
            new Thread(() -> TrafficSignDetector.detectTrafficSignsInVideo(relativePath, VideoProcessor.progressBar, frame)).start();
        }
    }

    /**
     * Gets the relative path of the selected file.
     *
     * @param  videosDir   the directory containing the videos
     * @param  fileChooser the file chooser
     * @return the relative path of the selected file
     */
    private static String getString(String videosDir, JFileChooser fileChooser) {
        File selectedFile = fileChooser.getSelectedFile();
        // Get the parent directory name of the selected file
        String parentDir = selectedFile.getParentFile().getName();

        // Get the relative path of the selected file
        String relativePath;
        // Check if the parent directory is "morning", "afternoon", or "night"
        if (parentDir.equals("morning") || parentDir.equals("afternoon") || parentDir.equals("night")) {
            // Append the parent directory name and the selected file name to the relative path
            relativePath = videosDir + "/" + parentDir + "/" + selectedFile.getName();
        } else {
            // Append the selected file name to the relative path
            relativePath = videosDir + "/" + selectedFile.getName();
        }
        return relativePath;
    }
}
