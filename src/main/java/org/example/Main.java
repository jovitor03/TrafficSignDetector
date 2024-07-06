package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    private static JLabel imageLabel;
    private static JProgressBar progressBar;
    private static JLabel resultLabel;

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        TrafficSignDetector trafficSignDetector = new TrafficSignDetector(new CascadeClassifier("cascade/haarcascade_traffic_signs.xml"));

        String imagesBaseDir = "./images";
        String videosBaseDir = "./videos";
        String phoneVideosBaseDir = "./phone videos";

        JFrame frame = new JFrame("traffic-sign-detector");
        JPanel panel = new JPanel();
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 100));
        progressBar.setStringPainted(true);

        JButton button1 = new JButton("Select generic image");
        button1.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(new File(imagesBaseDir));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String relativePath = imagesBaseDir + "/" + selectedFile.getName();

                Mat originalImage = Imgcodecs.imread(relativePath);
                displayImage(frame, matToBufferedImage(originalImage));

                Timer timer = new Timer(3000, event -> {
                    Mat resultImage = trafficSignDetector.detectTrafficSigns(relativePath);
                    if (resultImage != null) {
                        displayImage(frame, matToBufferedImage(resultImage));
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        panel.add(button1);

        JButton button2 = new JButton("Select generic video");
        button2.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(new File(videosBaseDir));
            int returnValue = fileChooser.showOpenDialog(null);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String relativePath = videosBaseDir + "/" + selectedFile.getName();

                if(resultLabel!= null){
                    frame.remove(resultLabel);
                }

                progressBar = new JProgressBar(0, 100);
                progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 100));
                progressBar.setStringPainted(true);
                frame.add(progressBar, BorderLayout.SOUTH);

                new Thread(() -> trafficSignDetector.detectTrafficSignsInVideo(relativePath, progressBar, frame)).start();
            }
        });
        panel.add(button2);

        JButton button3 = new JButton("Select phone video");
        button3.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(new File(phoneVideosBaseDir));
            int returnValue = fileChooser.showOpenDialog(null);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String parentDirName = selectedFile.getParentFile().getName();
                String relativePath = phoneVideosBaseDir + "/" + parentDirName + "/" + selectedFile.getName();

                if(resultLabel!= null){
                    frame.remove(resultLabel);
                }

                progressBar = new JProgressBar(0, 100);
                progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 100));
                progressBar.setStringPainted(true);
                frame.add(progressBar, BorderLayout.SOUTH);

                new Thread(() -> trafficSignDetector.detectTrafficSignsInVideo(relativePath, progressBar, frame)).start();
            }
        });
        panel.add(button3);

        frame.add(panel, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static BufferedImage matToBufferedImage(Mat mat){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private static BufferedImage matToResizedBufferedImage(Mat mat){
        int maxWidth = 1280;
        int maxHeight = 1080;

        int originalWidth = mat.width();
        int originalHeight = mat.height();

        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Mat resizedMat = new Mat();
        Size newSize = new Size(newWidth, newHeight);
        Imgproc.resize(mat, resizedMat, newSize, 0, 0, Imgproc.INTER_AREA);

        return matToBufferedImage(resizedMat);
    }

    private static void displayImage(JFrame frame, BufferedImage image) {
        if (imageLabel == null) {
            ImageIcon icon = new ImageIcon(image);
            imageLabel = new JLabel(icon);
            frame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        } else {
            imageLabel.setIcon(new ImageIcon(image));
        }
        frame.revalidate();
        frame.repaint();
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

        new Thread(() -> {
            VideoCapture videoCapture = new VideoCapture(videoPath);
            if (!videoCapture.isOpened()) {
                System.out.println("Could not open video: " + videoPath);
                return;
            }

            Mat frameMat = new Mat();
            while (videoCapture.read(frameMat)) {
                BufferedImage image = matToResizedBufferedImage(frameMat);
                SwingUtilities.invokeLater(() -> displayImage(frame, image));
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            videoCapture.release();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
