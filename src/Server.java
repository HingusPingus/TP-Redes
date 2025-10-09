import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;

public class Server {

    public static void main(String args[]) throws Exception
    {
        KeyGenerator keygenerator=KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keygenerator.init(256,secureRandom);
        SecretKey claveSimetrica = keygenerator.generateKey();
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Server is running and waiting for client connection...");
        KeyPair claves=Clave.generateRSAKkeyPair();
        while(true) {
            boolean aux=false;
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            DataInputStream in = new DataInputStream(
                    clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(
                    clientSocket.getOutputStream());
            boolean recibirclave=in.readBoolean();
            if(recibirclave) {
                Cipher cifrado=Cipher.getInstance("RSA");
                ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream outobj=new ObjectOutputStream(clientSocket.getOutputStream());
                PublicKey publicKeyCli=(PublicKey) inobj.readObject();
                outobj.writeObject(claves.getPublic());
                cifrado.init(Cipher.ENCRYPT_MODE,publicKeyCli);
                byte[] encryptedMessage=cifrado.doFinal(claveSimetrica.getEncoded());
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(claveSimetrica.getEncoded());
                cifrado.init(Cipher.ENCRYPT_MODE,claves.getPrivate());
                byte[] sign=cifrado.doFinal(encodedhash);

                out.write(encryptedMessage);
                out.write(sign);

            }

            boolean recibir=in.readBoolean();
            if(recibir) {
                receiveFile(in);
            }

            File folder = new File("./imgs/");
            int longitud = folder.listFiles().length;
            out.writeInt(longitud);
            for (File file : folder.listFiles()) {
                sendFile(file, out);
            }
            in.close();
            out.close();
            clientSocket.close();
            System.out.println("sali");

            if(aux){
                serverSocket.close();
            }

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
        String fileName="./imgs/"+fileNamex;
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