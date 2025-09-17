import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {

    public static void main(String args[]) throws Exception
    {
         try {
            Socket sockett = new Socket("localhost", 9099);
        

        // create a socket to connect to the server running on localhost at port number 9090
            File file = null;
            JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "JPG & GIF Images", "jpg", "gif","jpeg","png");
            fc.setFileFilter(filter);
            int returnVal = fc.showOpenDialog(null);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                file=fc.getSelectedFile();
            }
        

            // Setup output stream to send data to the server
            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            DataInputStream in =new DataInputStream(sockett.getInputStream()) ;

            // Setup input stream to receive data from the server

            sendFile(file,out);
            int longitud=in.readInt();
            // Send message to the serve
            File theDir = new File("./imgs2/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            for (int i = 0; i < longitud; i++) {
                receiveFile(in);
            }
            // Receive response from the server
            String response = in.readLine();
            System.out.println("Server says: " + response);

            // Close the socket
            sockett.close();
            gui();
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }
    }
    private static void sendFile(File file, DataOutputStream out)
            throws Exception
    {
        int bytes = 0;

        FileInputStream fileInputStream
                = new FileInputStream(file);
        out.writeUTF(file.getName());
        // Here we send the File to Server
        out.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            out.write(buffer, 0, bytes);
            out.flush();
        }
        // close the file here
        fileInputStream.close();
    }
    private static void receiveFile(DataInputStream in)
            throws Exception
    {

        int bytes = 0;
        String fileNamex=in.readUTF();
        String fileName="./imgs2/"+fileNamex;
        FileOutputStream fileOutputStream
                = new FileOutputStream(fileName);

        long size
                = in.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = in.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        // Here we received file
        System.out.println("File is Received");
        fileOutputStream.close();
    }
    public static void gui(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        int offseth=0;
        int offsetv=0;
        int maxoffset=0;
        JFrame frame = new JFrame("Whiteboard");
        frame.setSize(1000,1000);
        File folder= new File("./imgs2/");
        ArrayList<JLabel> labels=new ArrayList<>();
        int i=0;
        for (final File fileEntry : folder.listFiles()) {
            if((offseth+200)>=width){
                maxoffset=offseth;
                offseth=0;
                offsetv+=200;
            }
            JLabel label = new JLabel();
            labels.add(label);
            labels.get(i).setIcon(new ImageIcon(new ImageIcon(fileEntry.getPath()).getImage().getScaledInstance(200,200, Image.SCALE_AREA_AVERAGING)));
            labels.get(i).setBorder(BorderFactory.createEmptyBorder(offsetv,offseth,0,0));
            frame.add(labels.get(i));
            frame.pack();
            offseth=labels.get(i).getWidth();
            i++;
            if(i==folder.listFiles().length){
                JLabel labelf = new JLabel();
                labels.add(labelf);
                labels.get(i).setBorder(BorderFactory.createEmptyBorder(offsetv+200,maxoffset,0,0));
                frame.add(labels.get(i));
                frame.pack();
            }
        }

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}