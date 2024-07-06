package org.example;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessor {
    private static JLabel imageLabel;
    private static final JPanel contentPanel = new JPanel();

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

    public static BufferedImage matToResizedBufferedImage(Mat mat) {
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

    public static void displayImage(JFrame frame, BufferedImage image) {
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
