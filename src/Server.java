import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

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
        PublicKey publicKeyCli=null;
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
                ObjectOutputStream outobj=new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());

                publicKeyCli=(PublicKey) inobj.readObject();
                outobj.writeObject(claves.getPublic());
                byte[] encryptedMessage=Criptografia.encriptar("RSA", publicKeyCli, claveSimetrica.getEncoded());
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(claveSimetrica.getEncoded());
                byte[] sign=Criptografia.encriptar("RSA", claves.getPrivate(), encodedhash);
                outobj.flush();

                out.writeInt(encryptedMessage.length);
                out.write(encryptedMessage);
                out.writeInt(sign.length);
                out.write(sign);
                out.flush();

            }
            boolean recibir=in.readBoolean();
            if(recibir) {
                receiveFile(in,claveSimetrica,publicKeyCli);
            }

            File folder = new File("./imgs/");
            int longitud = folder.listFiles().length;
            byte[] result =  serialize(longitud);
            byte[] encryptedMessage=Criptografia.encriptar("AES", claveSimetrica, result);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(result);
            byte[] sign=Criptografia.encriptar("RSA", claves.getPrivate(), encodedhash);
            out.writeInt(encryptedMessage.length);
            out.write(encryptedMessage);
            out.writeInt(sign.length);
            out.write(sign);
            out.flush();
            for (File file : folder.listFiles()) {
                sendFile(file, out,claveSimetrica,claves);
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
    private static void sendFile(File file, DataOutputStream out, SecretKey clave, KeyPair claves)
            throws Exception {
        FileInputStream fileInputStream = new FileInputStream(file);
        out.writeUTF(file.getName());
        out.writeLong(file.length());

        byte[] buffer = new byte[16 * 1024]; // Use multiple of 16 for better AES performance
        int bytes;

        System.out.println("Sending file: " + file.getName());

        while ((bytes = fileInputStream.read(buffer)) != -1) {
            // Encrypt the actual bytes read
            byte[] dataToEncrypt = Arrays.copyOf(buffer, bytes);
            byte[] bufferEncriptado = Criptografia.encriptar("AES", clave, dataToEncrypt);

            // Create signature for ORIGINAL data (not encrypted)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(dataToEncrypt);
            byte[] sign = Criptografia.encriptar("RSA", claves.getPrivate(), encodedhash);

            // Send encrypted data length + data
            out.writeInt(bufferEncriptado.length);
            out.write(bufferEncriptado);

            // Send signature length + signature
            out.writeInt(sign.length);
            out.write(sign);
            out.flush();
        }
        fileInputStream.close();
        System.out.println("File sent: " + file.getName());
    }
    private static void receiveFile(DataInputStream in, SecretKey clave, PublicKey publicKeyCli)
            throws Exception {
        String fileNamex = in.readUTF();
        String fileName = "./imgs/" + fileNamex;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = in.readLong();
        long totalRead = 0;

        System.out.println("Receiving file: " + fileNamex + " (" + size + " bytes)");

        while (totalRead < size) {
            // Read encrypted chunk
            int encryptedLength = in.readInt();
            byte[] encryptedChunk = in.readNBytes(encryptedLength);

            // Read signature
            int signLength = in.readInt();
            byte[] encryptedSign = in.readNBytes(signLength);

            // Decrypt the data
            byte[] bufferDesencriptado = Criptografia.desencriptar("AES", clave, encryptedChunk);

            // Verify signature
            byte[] sign = Criptografia.desencriptar("RSA", publicKeyCli, encryptedSign);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(bufferDesencriptado);

            if (Arrays.equals(sign, encodedhash)) {
                fileOutputStream.write(bufferDesencriptado);
                totalRead += bufferDesencriptado.length;
            } else {
                System.out.println("Signature verification failed for chunk in file: " + fileNamex);
                break;
            }
        }

        fileOutputStream.close();
        System.out.println("File received: " + fileNamex);
    }
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

}