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
import java.io.File;

public class TrafficSignDetector {
    private final CascadeClassifier cascade;
    private final Scalar color = new Scalar(0, 255, 0);
    private final JPanel containerPanel = new JPanel(new GridBagLayout());
    private final JPanel labelsPanel = new JPanel();
    private JLabel processingLabel;
    private JLabel timeLabel;

    public TrafficSignDetector(CascadeClassifier cascade) {
        this.cascade = cascade;
    }

    public Mat detectTrafficSigns(String imagePath) {
        String resultImagePath = mantainFormat(imagePath);

        Mat image = Imgcodecs.imread(imagePath);

        if (image.empty()) {
            System.out.println("\nCould not load image: " + imagePath);
            return null;
        }

        highlightTrafficSigns(image, cascade, color);

        Imgcodecs.imwrite(resultImagePath, image);
        System.out.println("\nResult image saved at " + resultImagePath);

        return image;
    }

    public static void clearFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        } else {
            System.out.println("The specified path is not a valid directory.");
        }
    }

    public void addLabelsOnInterface(JFrame frame, String estimatedTimeText) {
        SwingUtilities.invokeLater(() -> {
            processingLabel = new JLabel("Processing video... It may take a while.");
            processingLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            processingLabel.setHorizontalAlignment(SwingConstants.CENTER);

            timeLabel = new JLabel(estimatedTimeText);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

            labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
            labelsPanel.add(processingLabel);
            labelsPanel.add(timeLabel);

            labelsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            labelsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

            containerPanel.add(labelsPanel);

            frame.add(containerPanel, BorderLayout.CENTER);

            frame.revalidate();
            frame.repaint();
        });
    }


    public void detectTrafficSignsInVideo(String videoPath, JProgressBar progressBar, JFrame frame) {
        clearFolder("frames");

        String estimatedTimeText;
        if (videoPath.contains("/phone")) {
            estimatedTimeText = "Estimated time: ~5 minutes";
        } else if (videoPath.contains("/videos")) {
            estimatedTimeText = "Estimated time: ~1 minute";
        } else {
            estimatedTimeText = "Estimated time: unknown";
        }

        SwingUtilities.invokeLater(() -> addLabelsOnInterface(frame, estimatedTimeText));

        VideoCapture videoCapture = new VideoCapture(videoPath);
        if (!videoCapture.isOpened()) {
            System.out.println("\nCould not open video: " + videoPath);
            return;
        }

        int frameWidth = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int frameHeight = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int frameRate = (int) videoCapture.get(Videoio.CAP_PROP_FPS);
        int totalFrames = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);

        String resultVideoPath = mantainFormat(videoPath);

        VideoWriter videoWriter = new VideoWriter(resultVideoPath, VideoWriter.fourcc('X', '2', '6', '4'), frameRate, new Size(frameWidth, frameHeight), true);
        if (!videoWriter.isOpened()) {
            System.out.println("\nCould not create video writer");
            return;
        }

        System.out.println("\nProcessing video... It may take a while.");

        Mat frameMat = new Mat();

        int processedFrames = 0;
        int percentage = 0;
        int frameNumber = 0;

        while (videoCapture.read(frameMat)) {
            int signs = highlightTrafficSigns(frameMat, cascade, color);

            if (signs > 0) {
                String frameFileName = String.format("frames/frame_%03d.png", frameNumber);
                Imgcodecs.imwrite(frameFileName, frameMat);
                frameNumber++;
            }

            videoWriter.write(frameMat);

            processedFrames++;
            int newPercentage = (processedFrames * 100) / totalFrames;
            if (newPercentage > percentage) {
                StringBuilder progressBarConsole = new StringBuilder("[");
                int progress = newPercentage / 2;
                for (int i = 0; i < 50; i++) {
                    if (i <= progress) {
                        progressBarConsole.append("=");
                    } else {
                        progressBarConsole.append(" ");
                    }
                }
                progressBarConsole.append("] ").append(newPercentage).append("%");
                System.out.print("\r" + progressBarConsole);

                SwingUtilities.invokeLater(() -> progressBar.setValue(newPercentage));
                percentage = newPercentage;
            }
        }

        videoCapture.release();
        videoWriter.release();

        System.out.println("\nResult video saved at " + resultVideoPath);

        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            labelsPanel.remove(processingLabel);
            labelsPanel.remove(timeLabel);
            containerPanel.remove(labelsPanel);
            frame.remove(containerPanel);
            frame.revalidate();
            frame.repaint();
        });

        SwingUtilities.invokeLater(() -> Main.playVideo(frame, resultVideoPath));
    }

    public String mantainFormat(String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
        }

        String fileNameWithoutExtension = "";
        int j = path.lastIndexOf(File.separator);
        if (j >= 0 && i > j) {
            fileNameWithoutExtension = path.substring(j + 1, i);
        } else if (i > 0) {
            fileNameWithoutExtension = path.substring(0, i);
        }

        return fileNameWithoutExtension + "_result." + extension;
    }

    public int highlightTrafficSigns(Mat image, CascadeClassifier cascade, Scalar color) {
        MatOfRect trafficSigns = new MatOfRect();
        cascade.detectMultiScale(image, trafficSigns);

        for (Rect rect : trafficSigns.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color, 4);
        }

        return trafficSigns.toArray().length;
    }
}