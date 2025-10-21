import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;

public class Utilidades {
    public static byte[] receiveMessage(DataInputStream in, PublicKey publicKey, SecretKey clave) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        ObjectInputStream inobj=new ObjectInputStream(in);
        MensajeFirma msjrecibido= (MensajeFirma) inobj.readObject();
        byte[] bufferDesencriptado=msjrecibido.desecriptarMsj(clave);
        byte[] encodedhash = hashear(bufferDesencriptado);
        if (Arrays.equals(msjrecibido.desencriptarFirma(publicKey), encodedhash)) {
            return bufferDesencriptado;
        }
        return null;
    }
    public static long receiveBuffers(DataInputStream in, SecretKey clave, PublicKey publicKey, FileOutputStream fileOutputStream) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] message =receiveMessage(in,publicKey,clave);
        if (message!=null) {
            fileOutputStream.write(message);
            return message.length;
        } else {
            return-1;
        }
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

    public static MensajeFirma encriptarMensaje(byte[]dataToEncrypt,SecretKey clave, KeyPair claves)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        byte[] bufferEncriptado = Criptografia.encriptar("AES", clave, dataToEncrypt);
        byte[] sign = firmar(bufferEncriptado,claves.getPrivate());
        return new MensajeFirma(bufferEncriptado,sign);
    }

    public static byte[] hashear(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    public static byte[] firmar(byte[] data,PrivateKey clavePrivada) throws NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return Criptografia.encriptar("RSA", clavePrivada, hashear(data));
    }
    public static void sendSpecs(DataOutputStream out,File file) throws IOException {
        out.writeUTF(file.getName());
        out.writeLong(file.length());
        out.flush();
    }
    public static void sendBuffers(DataOutputStream out,File file,SecretKey clave,KeyPair claves)
            throws IOException, NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] buffer = new byte[16 * 1024];
        int bytes;
        FileInputStream fileInputStream = new FileInputStream(file);
        System.out.println("Sending file: " + file.getName());
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            sendBuffer(bytes,buffer,out,clave,claves);
        }
        fileInputStream.close();
        System.out.println("File sent: " + file.getName());
    }
    public static void sendBuffer(int bytes,byte[] buffer, DataOutputStream out, SecretKey clave,KeyPair claves) throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        ObjectOutputStream objout =new ObjectOutputStream(out);
        byte[] dataToEncrypt = Arrays.copyOf(buffer, bytes);
        MensajeFirma mensaje=encriptarMensaje(dataToEncrypt,clave,claves);
        objout.writeObject(mensaje);
        objout.flush();
    }
    public static void sendFile(File file, DataOutputStream out, SecretKey clave, KeyPair claves)
            throws Exception {
        sendSpecs(out,file);
        sendBuffers(out, file, clave, claves);
    }
    public static void receiveFile(DataInputStream in, SecretKey clave, PublicKey publicKey)
            throws Exception {
        String fileNamex = in.readUTF();
        String fileName = "./imgs2/" + fileNamex;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        long size = in.readLong();
        long totalRead = 0;
        System.out.println("Receiving file: " + fileNamex + " (" + size + " bytes)");
        while (totalRead < size) {
            long totalBuffer=receiveBuffers(in,clave,publicKey,fileOutputStream);
            if(totalBuffer==-1){
                System.out.println("Signature verification failed for chunk in file: " + fileNamex);
                break;
            }
            totalRead+=totalBuffer;
        }
        fileOutputStream.close();
        System.out.println("File received: " + fileNamex);
    }
    public static void cerrarTodo(Socket sockett, DataOutputStream out, DataInputStream in) throws IOException {
        in.close();
        out.close();
        sockett.close();
    }
}
