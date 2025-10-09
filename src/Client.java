import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client {
    public static void main(String args[]) throws Exception
    {
        decision(true,null);
        GUI.mostrarImgs();

    }
    public static void decision(boolean mandar, SecretKey clave) {
        try {
            Socket sockett = new Socket("localhost", 9999);
            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            DataInputStream in = new DataInputStream(sockett.getInputStream());
            if(clave==null){
                ObjectOutputStream outobj =new ObjectOutputStream(sockett.getOutputStream());
                ObjectInputStream inobj =new ObjectInputStream(sockett.getInputStream());
                KeyPair claves =Clave.generateRSAKkeyPair();
                out.writeBoolean(true);
                outobj.writeObject(claves.getPublic());
                PublicKey publicKeyServ=(PublicKey) inobj.readObject();
                Cipher decrypt=Cipher.getInstance("RSA");
                decrypt.init(Cipher.DECRYPT_MODE, claves.getPublic());
                byte[] decryptedMessage=decrypt.doFinal(in.readAllBytes());
                decrypt.init(Cipher.DECRYPT_MODE, publicKeyServ);
                byte[] sign=decrypt.doFinal(in.readAllBytes());
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(decryptedMessage);
                if(Arrays.equals(sign, encodedhash)){
                    clave= (SecretKey) deserialize(decryptedMessage);
                }
            }


            if (mandar) {

                out.writeBoolean(true);
                mandarNuevo(sockett, out, in,clave);
            } else {
                out.writeBoolean(false);
            }
            actualizarLista(sockett, out, in,clave);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void mandarNuevo(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave) throws Exception {
        File file = null;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Seleccione una imagen", "jpg", "gif","jpeg","png");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == JFileChooser.APPROVE_OPTION){
            file=fc.getSelectedFile();
        }
            sendFile(file,out,clave);

    }
    private static void actualizarLista(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave){
        try {

            int longitud=in.readInt();
            File theDir = new File("./imgs2/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }
            for (int i = 0; i < longitud; i++) {
                receiveFile(in,clave);
            }
            in.close();
            out.close();
            sockett.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void sendFile(File file, DataOutputStream out, SecretKey clave)
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
    private static void receiveFile(DataInputStream in, SecretKey clave)
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
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}