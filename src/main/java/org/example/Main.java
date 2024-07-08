package org.example;

import org.opencv.core.Core;

/**
 * Main class is responsible for running the application and loading OpenCV library.
 */
public class Main {

    // Load OpenCV library.
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Main method to run the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        InterfaceGUI.createInterface();
    }
}
