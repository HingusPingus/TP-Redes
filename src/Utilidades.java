import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.logging.Logger;


public class Utilidades {
    static final Logger logger = Logger.getLogger( Utilidades.class.getName() );
    public static byte[] receiveMessage(ObjectInputStream inobj, PublicKey publicKey, SecretKey clave) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        MensajeFirma msjrecibido= (MensajeFirma) inobj.readObject();
        byte[] bufferDesencriptado=msjrecibido.desecriptarMsj(clave);
        byte[] encodedhash = hashear(bufferDesencriptado);
        if (Arrays.equals(msjrecibido.desencriptarFirma(publicKey), encodedhash)) {
            return bufferDesencriptado;
        }
        return null;
    }
    public static long receiveBuffers(ObjectInputStream inobj, SecretKey clave, PublicKey publicKey, FileOutputStream fileOutputStream) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] message =receiveMessage(inobj,publicKey,clave);
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
        byte[] sign = firmar(dataToEncrypt,claves.getPrivate());
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
        logger.info("Sending files");
        ObjectOutputStream objout =new ObjectOutputStream(out);
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            sendBuffer(bytes,buffer,objout,clave,claves);
            objout.flush();
        }
        fileInputStream.close();
        logger.info("File sent: " + file.getName());
    }
    public static void sendBuffer(int bytes,byte[] buffer, ObjectOutputStream objout, SecretKey clave,KeyPair claves) throws IOException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {

        byte[] dataToEncrypt = Arrays.copyOf(buffer, bytes);
        MensajeFirma mensaje=encriptarMensaje(dataToEncrypt,clave,claves);
        objout.writeObject(mensaje);

    }
    public static void sendFile(File file, DataOutputStream out, SecretKey clave, KeyPair claves)
            throws Exception {
        sendSpecs(out,file);
        sendBuffers(out, file, clave, claves);
    }
    public static void receiveFile(DataInputStream in, SecretKey clave, PublicKey publicKey,String directory)
            throws Exception {
        String fileNamex = in.readUTF();
        String fileName = directory+ fileNamex;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        long size = in.readLong();
        long totalRead = 0;
        ObjectInputStream inobj=new ObjectInputStream(in);
        logger.info("Receiving file: " + fileNamex + " (" + size + " bytes)");
        while (totalRead < size) {
            long totalBuffer=receiveBuffers(inobj,clave,publicKey,fileOutputStream);
            if(totalBuffer==-1){
                logger.info("Signature verification failed for chunk in file: " + fileNamex);
                break;
            }
            totalRead+=totalBuffer;
        }
        fileOutputStream.close();
        logger.info("File received: " + fileNamex);
    }
    public static void cerrarTodo(Socket sockett, DataOutputStream out, DataInputStream in) throws IOException {
        in.close();
        out.close();
        sockett.close();
    }
}
