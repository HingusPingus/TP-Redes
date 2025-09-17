import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    public static void main(String args[]) throws Exception
    {
        // create a server socket on port number 9090
        ServerSocket serverSocket = new ServerSocket(9099);
        System.out.println("Server is running and waiting for client connection...");
        while(true) {
            boolean aux=false;
            // Accept incoming client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Setup input and output streams for communication with the client
            DataInputStream in = new DataInputStream(
                    clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(
                    clientSocket.getOutputStream());

            receiveFile(in);

            // Send response to the client
            File folder = new File("./imgs/");
            int longitud = folder.listFiles().length;
            out.writeInt(longitud);
            for (File file : folder.listFiles()) {
                sendFile(file, out);
            }
            in.close();
            out.close();
            // Close the client socket
            //clientSocket.close();
            if(aux){
                serverSocket.close();
            }

        }
            // Close the server socket


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
        String fileName="./imgs/"+fileNamex;
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

}