package org.example;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoProcessor {
    public static JProgressBar progressBar;
    private static JLabel resultLabel;

    public static JProgressBar createProgressBar() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 100));
        progressBar.setStringPainted(true);
        return progressBar;
    }

    private static void displayVideoImage(JPanel videoPanel, BufferedImage image) {
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        videoPanel.removeAll();
        videoPanel.add(imageLabel);
        videoPanel.revalidate();
        videoPanel.repaint();
    }

    public static void playVideo(JFrame frame, String videoPath) {
        frame.remove(progressBar);
        frame.revalidate();
        frame.repaint();

        resultLabel = new JLabel("Result video saved at " + videoPath);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(resultLabel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
        JPanel videoPanel = new JPanel();
        frame.add(videoPanel, BorderLayout.CENTER);

        new Thread(() -> {
            VideoCapture videoCapture = new VideoCapture(videoPath);
            if (!videoCapture.isOpened()) {
                System.out.println("Could not open video: " + videoPath);
                return;
            }

            Mat frameMat = new Mat();
            Timer timer = new Timer(1000 / 60, null);

            timer.addActionListener(e -> {
                if (videoCapture.read(frameMat)) {
                    BufferedImage image = ImageProcessor.matToResizedBufferedImage(frameMat);
                    SwingUtilities.invokeLater(() -> displayVideoImage(videoPanel, image));
                } else {
                    timer.stop();
                    videoCapture.release();

                    SwingUtilities.invokeLater(() -> {
                        frame.remove(videoPanel);
                        frame.remove(resultLabel);
                        frame.revalidate();
                        frame.repaint();
                    });
                }
            });

            timer.start();
        }).start();
    }
}
