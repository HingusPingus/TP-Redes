import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {

    public static void main(String args[]) throws Exception
    {
        decision(true);
        GUI.mostrarImgs();

    }
    public static void decision(boolean mandar) {
        try {
            Socket sockett = new Socket("localhost", 9999);


            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            DataInputStream in = new DataInputStream(sockett.getInputStream());
            if (mandar) {
                out.writeBoolean(true);
                mandarNuevo(sockett, out, in);
            } else {
                out.writeBoolean(false);
            }
            actualizarLista(sockett, out, in);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void mandarNuevo(Socket sockett, DataOutputStream out, DataInputStream in) throws Exception {
        File file = null;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Seleccione una imagen", "jpg", "gif","jpeg","png");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == JFileChooser.APPROVE_OPTION){
            file=fc.getSelectedFile();
        }
            sendFile(file,out);

    }
    private static void actualizarLista(Socket sockett, DataOutputStream out, DataInputStream in){
        try {

            int longitud=in.readInt();
            File theDir = new File("./imgs2/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            for (int i = 0; i < longitud; i++) {
                receiveFile(in);
            }
            in.close();
            out.close();
            sockett.close();
        } catch (Exception e) {
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

}