package org.example;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;

/**
 * TrafficSignDetector class is responsible for detecting traffic signs in images and videos.
 * It uses a pre-trained Haar cascade classifier to detect traffic signs in images.
 * It highlights the detected traffic signs with a green rectangle.
 */
public class TrafficSignDetector {
    // Cascade classifier for traffic signs
    private final static CascadeClassifier cascade = new CascadeClassifier("cascades/haarcascade_traffic_signs.xml");
    private final static Scalar color = new Scalar(0, 255, 0); // Color green
    private final static JPanel containerPanel = new JPanel(new GridBagLayout()); // JPanel container for labels
    private final static JPanel labelsPanel = new JPanel(); // JPanel for labels
    private static JLabel processingLabel; // JLabel for processing message
    private static JLabel timeLabel; // JLabel for estimated time

    /**
     * Detects traffic signs in an image and highlights them with a green rectangle.
     *
     * @param imagePath path to the image
     * @return Mat object representing the image with highlighted traffic signs
     */
    public static Mat detectTrafficSigns(String imagePath) {
        // Maintain the format of the image
        String resultImagePath = FileProcessor.maintainFormat(imagePath);

        Mat image = Imgcodecs.imread(imagePath);

        if (image.empty()) {
            System.out.println("\nCould not load image: " + imagePath);
            return null;
        }

        // Highlight traffic signs in the image
        highlightTrafficSigns(image, cascade, color);

        // Save the image with highlighted traffic signs
        Imgcodecs.imwrite(resultImagePath, image);

        return image;
    }

    /**
     * Adds labels on the interface to inform the user about the processing of the video.
     *
     * @param frame JFrame object representing the main frame
     * @param estimatedTimeText estimated time to process the video
     */
    public static void addLabelsOnInterface(JFrame frame, String estimatedTimeText) {
        SwingUtilities.invokeLater(() -> {
            // Processing label
            processingLabel = new JLabel("Processing video... It may take a while.");
            processingLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            processingLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // Estimated time label
            timeLabel = new JLabel(estimatedTimeText);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            // Labels panel
            labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
            labelsPanel.add(processingLabel);
            labelsPanel.add(timeLabel);
            labelsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

            // Container panel
            containerPanel.add(labelsPanel);
            frame.add(containerPanel, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();
        });
    }

    /**
     * Detects traffic signs in a video and highlights them with a green rectangle.
     *
     * @param videoPath path to the video
     * @param progressBar JProgressBar object representing the progress bar
     * @param frame JFrame object representing the main frame
     */
    public static void detectTrafficSignsInVideo(String videoPath, JProgressBar progressBar, JFrame frame) {
        // Clear the frames folder
        FileProcessor.clearFolder("frames");

        // Estimated time to process the video
        String estimatedTimeText;
        if (videoPath.contains("/phone")) {
            estimatedTimeText = "Estimated time: ~2-3 minutes";
        } else if (videoPath.contains("/videos")) {
            estimatedTimeText = "Estimated time: ~1 minute";
        } else {
            estimatedTimeText = "Estimated time: unknown";
        }

        // Add labels on the interface
        SwingUtilities.invokeLater(() -> addLabelsOnInterface(frame, estimatedTimeText));

        // Process the video
        VideoCapture videoCapture = new VideoCapture(videoPath);
        if (!videoCapture.isOpened()) {
            System.out.println("\nCould not open video: " + videoPath);
            return;
        }

        // Get video properties
        int frameWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int frameRate = (int) videoCapture.get(Videoio.CAP_PROP_FPS);
        int totalFrames = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);

        // Maintain the format of the video
        String resultVideoPath = FileProcessor.maintainFormat(videoPath);

        // Create video writer
        VideoWriter videoWriter = new VideoWriter(resultVideoPath, VideoWriter.fourcc('X', '2', '6', '4'), frameRate, new Size(frameWidth, frameHeight), true);
        if (!videoWriter.isOpened()) {
            System.out.println("\nCould not create video writer");
            return;
        }

        Mat frameMat = new Mat();

        // Variables when processing the video
        int processedFrames = 0;
        int percentage = 0;
        int frameNumber = 0;

        while (videoCapture.read(frameMat)) {
            // Highlight traffic signs in the frame
            int signs = highlightTrafficSigns(frameMat, cascade, color);

            // Save the frame if traffic signs are detected
            if (signs > 0) {
                String frameFileName = String.format("frames/frame_%03d.png", frameNumber);
                Imgcodecs.imwrite(frameFileName, frameMat);
                // Increase the frame number
                frameNumber++;
            }

            // Write the frame to the video
            videoWriter.write(frameMat);

            // Update the progress bar
            processedFrames++;
            int newPercentage = (processedFrames * 100) / totalFrames;
            if (newPercentage > percentage) {
                SwingUtilities.invokeLater(() -> progressBar.setValue(newPercentage));
                percentage = newPercentage;
            }
        }

        // Release the video capture and video writer
        videoCapture.release();
        videoWriter.release();

        SwingUtilities.invokeLater(() -> {
            // Remove labels and progress bar
            progressBar.setValue(100);
            labelsPanel.remove(processingLabel);
            labelsPanel.remove(timeLabel);
            containerPanel.remove(labelsPanel);
            frame.remove(containerPanel);
            frame.revalidate();
            frame.repaint();
        });

        // Play the result video
        SwingUtilities.invokeLater(() -> VideoProcessor.playVideo(frame, resultVideoPath));
    }

    /**
     * Highlights traffic signs in an image with a green rectangle.
     *
     * @param image Mat object representing the image
     * @param cascade CascadeClassifier object representing the cascade classifier
     * @param color Scalar object representing the color of the rectangle
     * @return number of traffic signs detected
     */
    public static int highlightTrafficSigns(Mat image, CascadeClassifier cascade, Scalar color) {
        MatOfRect trafficSigns = new MatOfRect();
        // Detect traffic signs in the image
        cascade.detectMultiScale(image, trafficSigns);

        // Highlight traffic signs with a green rectangle
        for (Rect rect : trafficSigns.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color, 4);
        }

        // Return the number of traffic signs detected
        return trafficSigns.toArray().length;
    }
}