import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {
    public static void main(String args[]) throws Exception
    {
        decision(true,null,null,null);


    }
    public static void decision(boolean mandar, SecretKey clave,PublicKey publicKeyServ, KeyPair claves) {
        try {
            Socket sockett = new Socket("localhost", 9999);

            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            if(clave==null){

                out.writeBoolean(true);
                out.flush();

                ObjectOutputStream outobj =new ObjectOutputStream(sockett.getOutputStream());
                ObjectInputStream inobj =new ObjectInputStream(sockett.getInputStream());
                claves =Clave.generateRSAKkeyPair();

                outobj.writeObject(claves.getPublic());

                publicKeyServ=(PublicKey) inobj.readObject();
                outobj.flush();
                DataInputStream in = new DataInputStream(sockett.getInputStream());
                int len=in.readInt();
                byte[] decryptedMessage=Criptografia.desencriptar("RSA",claves.getPrivate(),in.readNBytes(len));
                int lenSign=in.readInt();
                byte[] sign=Criptografia.desencriptar("RSA",publicKeyServ,in.readNBytes(lenSign));
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(decryptedMessage);
                if(Arrays.equals(sign, encodedhash)){
                    clave = new javax.crypto.spec.SecretKeySpec(decryptedMessage, "AES");
                }
            }
            else{
                out.writeBoolean(false);
            }

            DataInputStream in = new DataInputStream(sockett.getInputStream());
            if (mandar) {

                out.writeBoolean(true);
                mandarNuevo(sockett, out, in,clave,claves);
            } else {
                out.writeBoolean(false);
            }
            actualizarLista(sockett, out, in,clave,publicKeyServ,claves);
            if(mandar){
                GUI.mostrarImgs(clave,publicKeyServ,claves);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void mandarNuevo(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave, KeyPair claves) throws Exception {
        File file = null;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Seleccione una imagen", "jpg", "gif","jpeg","png");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == JFileChooser.APPROVE_OPTION){
            file=fc.getSelectedFile();
        }
            sendFile(file,out,clave,claves);

    }
    private static void actualizarLista(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave, PublicKey publicKeyServ, KeyPair claves) {
        try {
            int len = in.readInt();
            byte[] longitudb = Criptografia.desencriptar("AES", clave, in.readNBytes(len));

            int lenSign = in.readInt();
            byte[] sign = Criptografia.desencriptar("RSA", publicKeyServ, in.readNBytes(lenSign));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(longitudb);

            if (Arrays.equals(sign, encodedhash)) {
                int longitud = (int) deserialize(longitudb);
                File theDir = new File("./imgs2/");
                if (!theDir.exists()) {
                    theDir.mkdirs();
                }
                for (int i = 0; i < longitud; i++) {
                    receiveFile(in, clave, publicKeyServ);
                }
            }

            in.close();
            out.close();
            sockett.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void sendFile(File file, DataOutputStream out, SecretKey clave, KeyPair claves)
            throws Exception {
        FileInputStream fileInputStream = new FileInputStream(file);
        out.writeUTF(file.getName());
        out.writeLong(file.length());

        byte[] buffer = new byte[16 * 1024]; // Use multiple of 16
        int bytes;

        System.out.println("Sending file: " + file.getName());

        while ((bytes = fileInputStream.read(buffer)) != -1) {
            // Encrypt the actual bytes read
            byte[] dataToEncrypt = Arrays.copyOf(buffer, bytes);
            byte[] bufferEncriptado = Criptografia.encriptar("AES", clave, dataToEncrypt);

            // Create signature for ORIGINAL data
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
    private static void receiveFile(DataInputStream in, SecretKey clave, PublicKey publicKeyServ)
            throws Exception {
        String fileNamex = in.readUTF();
        String fileName = "./imgs2/" + fileNamex;
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
            byte[] sign = Criptografia.desencriptar("RSA", publicKeyServ, encryptedSign);
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
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
}