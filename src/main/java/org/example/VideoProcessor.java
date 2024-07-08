package org.example;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * VideoProcessor class is responsible for processing videos.
 */
public class VideoProcessor {
    public static JProgressBar progressBar; //JProgressBar to display the progress of the video processing
    private static JLabel resultLabel; //JLabel to display the result video path

    /**
     * Creates a JProgressBar to display the progress of the video processing.
     *
     * @param frame JFrame to display the JProgressBar
     * @return JProgressBar object
     */
    public static JProgressBar createProgressBar(JFrame frame) {
        // Remove the image label from the frame
        ImageProcessor.removeImageLabel(frame);

        // Set progressBar properties
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 100));
        progressBar.setStringPainted(true);
        return progressBar;
    }

    /**
     * Displays the video image on the video panel.
     *
     * @param videoPanel JPanel to display the video image
     * @param image      BufferedImage object to be displayed
     */
    private static void displayVideoImage(JPanel videoPanel, BufferedImage image) {
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        videoPanel.removeAll();
        videoPanel.add(imageLabel);
        videoPanel.revalidate();
        videoPanel.repaint();
    }

    /**
     * Plays the video on the frame.
     *
     * @param frame     JFrame to display the video
     * @param videoPath the path of the video to be played
     */
    public static void playVideo(JFrame frame, String videoPath) {
        // Remove the progress bar from the frame
        frame.remove(progressBar);
        frame.revalidate();
        frame.repaint();

        // Display the result video path
        resultLabel = new JLabel("Result video saved at " + videoPath);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(resultLabel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();

        // Create a video panel to display the video
        JPanel videoPanel = new JPanel();
        frame.add(videoPanel, BorderLayout.CENTER);

        // Play the video
        new Thread(() -> {
            VideoCapture videoCapture = new VideoCapture(videoPath);
            // Check if the video is opened
            if (!videoCapture.isOpened()) {
                System.out.println("Could not open video: " + videoPath);
                return;
            }

            // Read the video frames
            Mat frameMat = new Mat();
            // Create a timer to display the video frames (120 fps)
            Timer timer = new Timer(1000 / 120, null);

            // Add an action listener to the timer
            timer.addActionListener(e -> {
                if (videoCapture.read(frameMat)) {
                    // Resize the frameMat to a BufferedImage
                    BufferedImage image = ImageProcessor.matToResizedBufferedImage(frameMat);
                    // Display the video image
                    SwingUtilities.invokeLater(() -> displayVideoImage(videoPanel, image));
                } else {
                    // Stop the timer and release the videoCapture
                    timer.stop();
                    videoCapture.release();

                    // Remove the video panel and result label from the frame
                    SwingUtilities.invokeLater(() -> {
                        frame.remove(videoPanel);
                        frame.remove(resultLabel);
                        frame.revalidate();
                        frame.repaint();
                    });
                }
            });
            // Start the timer
            timer.start();
        }).start();
    }
}
