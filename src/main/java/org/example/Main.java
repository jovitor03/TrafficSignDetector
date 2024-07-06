package org.example;

import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String IMAGES_BASE_DIR = "./images";
    private static final String VIDEOS_BASE_DIR = "./videos";
    private static final String PHONE_VIDEOS_BASE_DIR = "./phone videos";

    public static void main(String[] args) {
        JFrame frame = createMainFrame();

        JButton button1 = createButton("Select generic image", e -> {
            FileProcessor.handleImageSelection(frame, IMAGES_BASE_DIR);
        });

        JButton button2 = createButton("Select generic video", e -> {
            FileProcessor.handleVideoSelection(frame, VIDEOS_BASE_DIR);
        });

        JButton button3 = createButton("Select phone video", e -> {
            FileProcessor.handleVideoSelection(frame, PHONE_VIDEOS_BASE_DIR);
        });

        JPanel panel = createPanel(button1, button2, button3);
        frame.add(panel, BorderLayout.NORTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static JFrame createMainFrame() {
        JFrame frame = new JFrame("traffic-sign-detector");
        frame.setLayout(new BorderLayout());
        return frame;
    }

    private static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private static JPanel createPanel(Component... components) {
        JPanel panel = new JPanel();
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }
}
