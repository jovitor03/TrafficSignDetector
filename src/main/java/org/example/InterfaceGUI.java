package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * InterfaceGUI class is responsible for creating the graphical user interface.
 */
public class InterfaceGUI {
    private static final String IMAGES_BASE_DIR = "./images"; // Path to the directory where images are stored
    private static final String VIDEOS_BASE_DIR = "./videos"; // Path to the directory where videos are stored
    private static final String PHONE_VIDEOS_BASE_DIR = "./phone videos"; // Path to the directory where phone videos are stored

    /**
     * Creates the graphical user interface.
     */
    public static void createInterface() {
        // Create the main frame
        JFrame frame = createMainFrame();

        // Create buttons
        JButton button1 = createButton("Select generic image", e -> FileProcessor.handleImageSelection(frame, IMAGES_BASE_DIR));
        JButton button2 = createButton("Select generic video", e -> FileProcessor.handleVideoSelection(frame, VIDEOS_BASE_DIR));
        JButton button3 = createButton("Select phone video", e -> FileProcessor.handleVideoSelection(frame, PHONE_VIDEOS_BASE_DIR));

        // Create panel
        JPanel panel = createPanel(button1, button2, button3);
        frame.add(panel, BorderLayout.NORTH);

        // Set frame properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /**
     * Creates the main frame.
     *
     * @return the main frame
     */
    private static JFrame createMainFrame() {
        JFrame frame = new JFrame("traffic-sign-detector");
        frame.setLayout(new BorderLayout());
        return frame;
    }

    /**
     * Creates a button with the specified text and action listener.
     *
     * @param text     the text of the button
     * @param listener the action listener
     * @return the button
     */
    private static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Creates a panel with the specified components.
     *
     * @param components the components
     * @return the panel
     */
    private static JPanel createPanel(Component... components) {
        JPanel panel = new JPanel();
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }
}
