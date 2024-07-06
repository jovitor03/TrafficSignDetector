package org.example;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileProcessor {
    public static void clearFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean success = file.delete();
                        if (!success) {
                            System.out.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            System.out.println("The specified path is not a valid directory.");
        }
    }

    public static String mantainFormat(String path) {
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

    public static void handleImageSelection(JFrame frame, String imagesDir) {
        JFileChooser fileChooser = new JFileChooser(new File(imagesDir));
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String relativePath = imagesDir + "/" + selectedFile.getName();

            Mat originalImage = Imgcodecs.imread(relativePath);
            ImageProcessor.displayImage(frame, ImageProcessor.matToResizedBufferedImage(originalImage));

            Timer timer = new Timer(3000, event -> {
                Mat resultImage = TrafficSignDetector.detectTrafficSigns(relativePath);
                if (resultImage != null) {
                    ImageProcessor.displayImage(frame, ImageProcessor.matToResizedBufferedImage(resultImage));
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    public static void handleVideoSelection(JFrame frame, String videosDir) {
        JFileChooser fileChooser = new JFileChooser(new File(videosDir));
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String parentDir = selectedFile.getParentFile().getName();
            String relativePath;
            if (parentDir.equals("morning") || parentDir.equals("afternoon") || parentDir.equals("night")) {
                relativePath = videosDir + "/" + parentDir + "/" + selectedFile.getName();
            } else {
                relativePath = videosDir + "/" + selectedFile.getName();
            }

            ImageProcessor.removeImage(frame);

            JProgressBar progressBar = VideoProcessor.createProgressBar();
            frame.add(progressBar, BorderLayout.SOUTH);

            new Thread(() -> TrafficSignDetector.detectTrafficSignsInVideo(relativePath, VideoProcessor.progressBar, frame)).start();
        }
    }
}
