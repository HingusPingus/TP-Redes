import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {

    public static void main(String args[]) throws Exception
    {


        File file = null;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Seleccione una imagen", "jpg", "gif","jpeg","png");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == JFileChooser.APPROVE_OPTION){
            file=fc.getSelectedFile();
        }
        try {
            Socket sockett = new Socket("localhost", 9099);


            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            DataInputStream in =new DataInputStream(sockett.getInputStream()) ;


            sendFile(file,out);
            int longitud=in.readInt();
            File theDir = new File("./imgs2/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            for (int i = 0; i < longitud; i++) {
                receiveFile(in);
            }
            String response = in.readLine();
            in.close();
            out.close();
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
        out.writeLong(file.length());
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            out.write(buffer, 0, bytes);
            out.flush();
        }
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
                = in.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = in.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
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