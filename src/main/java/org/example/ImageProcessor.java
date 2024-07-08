package org.example;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ImageProcessor class is responsible for processing images.
 */
public class ImageProcessor {
    private static JLabel imageLabel; // JLabel to display the image
    private static final JPanel contentPanel = new JPanel(); // JPanel to hold the image
    private static JLabel resultImageLabel = null; // JLabel to display the result image

    /**
     * Converts a Mat object to a BufferedImage object.
     *
     * @param mat Mat object to be converted
     * @return BufferedImage object
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
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

    /**
     * Converts a Mat object to a resized BufferedImage object.
     *
     * @param mat Mat object to be converted
     * @return BufferedImage object
     */
    public static BufferedImage matToResizedBufferedImage(Mat mat) {
        int maxWidth = 1100;
        int maxHeight = 800;

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

    /**
     * Displays an image on the frame.
     *
     * @param frame JFrame object
     * @param image BufferedImage object to be displayed
     */
    public static void displayImage(JFrame frame, BufferedImage image) {
        // Remove the result image label if it exists
        removeImageLabel(frame);
        // Remove the previous image if it exists
        swapImage(frame, image);
    }

    /**
     * Displays the image with detected traffic signs on the frame.
     *
     * @param frame JFrame object
     * @param image BufferedImage object to be displayed
     * @param imagePath Path of the image
     */
    public static void displayDetectedImage(JFrame frame, BufferedImage image, String imagePath) {
        // Add the result image label
        resultImageLabel = new JLabel("Result video saved at " + imagePath);
        resultImageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(resultImageLabel, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();

        // Remove the previous image if it exists
        swapImage(frame, image);
    }

    /**
     * Swaps the image on the frame.
     *
     * @param frame JFrame object
     * @param image BufferedImage object to be displayed
     */
    private static void swapImage(JFrame frame, BufferedImage image) {
        if (imageLabel == null) {
            imageLabel = new JLabel(new ImageIcon(image));
            contentPanel.add(imageLabel, BorderLayout.CENTER);
        } else {
            imageLabel.setIcon(new ImageIcon(image));
        }
        frame.add(contentPanel);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Removes the result image label from the frame.
     *
     * @param frame JFrame object
     */
    public static void removeImageLabel(JFrame frame) {
        if (resultImageLabel != null) {
            frame.remove(resultImageLabel);
            resultImageLabel = null;
            frame.revalidate();
            frame.repaint();
        }
    }

    /**
     * Removes the image from the frame.
     *
     * @param frame JFrame object
     */
    public static void removeImage(JFrame frame) {
        if (imageLabel != null) {
            contentPanel.remove(imageLabel);
            imageLabel = null;
            contentPanel.revalidate();
            contentPanel.repaint();
            frame.remove(contentPanel);
            frame.revalidate();
            frame.repaint();
        }
    }
}
