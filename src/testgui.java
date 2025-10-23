import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class testgui {
    public static void main(String[] args) {
        crearFrame("localhost");
    }
    public static void crearFrame(String ip) {
        JFrame frame = new JFrame("Whiteboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        File folder = new File("./imgs2/");

        // Debug: Check if folder exists and what files it contains
        System.out.println("Folder exists: " + folder.exists());
        System.out.println("Folder path: " + folder.getAbsolutePath());

        if (!folder.exists()) {
            folder.mkdirs(); // Create directory if it doesn't exist
        }

        File[] files = folder.listFiles();
        if (files != null) {
            System.out.println("Number of files found: " + files.length);
            for (File file : files) {
                System.out.println("File: " + file.getName() + " - Size: " + file.length() + " bytes");
            }
        } else {
            System.out.println("No files found or folder is empty");
        }

        GUIThread hilo = new GUIThread(frame, folder, ip);
        hilo.start();

        mostrarImgs(folder, frame, ip, hilo);
    }

    public static void mostrarImgs(File folder, JFrame frame, String ip, GUIThread hilo) {
        // Clear the frame first
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create panel for images with GridLayout for consistent sizing
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0, 4, 5, 5)); // 4 columns, auto rows, with gaps
        imagePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setPreferredSize(new Dimension(1000, 800));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Load images
        File[] files = folder.listFiles();
        int loadedImages = 0;

        if (files != null) {
            for (File fileEntry : files) {
                if (fileEntry.isFile() && isImageFile(fileEntry)) {
                    try {
                        ImageIcon originalIcon = new ImageIcon(fileEntry.getPath());

                        // Check if image loaded successfully
                        if (originalIcon.getIconWidth() > 0 && originalIcon.getIconHeight() > 0) {
                            Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                            ImageIcon scaledIcon = new ImageIcon(scaledImage);

                            JLabel label = new JLabel(scaledIcon);
                            label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                            label.setHorizontalAlignment(SwingConstants.CENTER);
                            label.setVerticalAlignment(SwingConstants.CENTER);

                            imagePanel.add(label);
                            loadedImages++;
                            System.out.println("Successfully loaded: " + fileEntry.getName());
                        } else {
                            System.out.println("Failed to load image: " + fileEntry.getName());
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading image " + fileEntry.getName() + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("Total images loaded: " + loadedImages);

        // Add button panel
        JPanel buttonPanel = new JPanel();
        crearBoton(ip, frame, hilo, buttonPanel);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);

        // Force refresh
        frame.revalidate();
        frame.repaint();
    }

    // Helper method to check if file is an image
    private static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    public static void crearBoton(String ip, JFrame frame, GUIThread hilo, JPanel buttonPanel) {
        JButton button = new JButton("Cargar Imagen");
        button.setPreferredSize(new Dimension(200, 50));
        button.setFocusable(false);

        button.addActionListener(e -> {
            hilo.stopThread();
            try {
                hilo.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            Client.startup(true, ip);
            frame.dispose();
            crearFrame(ip);
        });

        buttonPanel.add(button);
    }
}