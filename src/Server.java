import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

public class Server {

    public static void main(String args[]) throws Exception
    {
        Utilidades.logger.setLevel(Level.parse(args[0].toUpperCase()));

        SecretKey claveSimetrica = crearClaves();
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Server is running and waiting for client connection...");
        KeyPair claves=Clave.generateRSAKkeyPair();

        while(true) {
            conexionCliente(serverSocket,claves,claveSimetrica);
        }


    }
    public static SecretKey crearClaves() throws NoSuchAlgorithmException {
        KeyGenerator keygenerator=KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keygenerator.init(256,secureRandom);
        return keygenerator.generateKey();
    }
    public static PublicKey intercambioClaves(ObjectOutputStream outobj, ObjectInputStream inobj,KeyPair claves) throws IOException, ClassNotFoundException {
        java.security.PublicKey publicKeyCli =(PublicKey) inobj.readObject();
        outobj.writeObject(claves.getPublic());
        outobj.flush();
        return publicKeyCli;
    }
    public static void enviarClave(PublicKey publicKeyCli,SecretKey claveSimetrica,KeyPair claves,ObjectInputStream inobj,ObjectOutputStream outobj) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        byte[] encryptedMessage=Criptografia.encriptar("RSA", publicKeyCli, claveSimetrica.getEncoded());
        byte[] sign=Utilidades.firmar(claveSimetrica.getEncoded(), claves.getPrivate());
        MensajeFirma mensajeClave=new MensajeFirma(encryptedMessage,sign);
        outobj.writeObject(mensajeClave);
        outobj.flush();
    }
    public static void recibirFile(DataInputStream in,SecretKey claveSimetrica,PublicKey publicKeyCli) throws Exception {
        boolean recibir=in.readBoolean();
        if(recibir) {
            Utilidades.receiveFile(in,claveSimetrica,publicKeyCli,"./imgs/");
        }
    }
    public static void enviarFiles(SecretKey claveSimetrica,KeyPair claves,ObjectOutputStream outobj, DataOutputStream out) throws Exception {
        File folder = new File("./imgs/");
        int longitud = folder.listFiles().length;
        byte[] result =  Utilidades.serialize(longitud);

        MensajeFirma mensajeMandar =Utilidades.encriptarMensaje(result,claveSimetrica,claves);

        outobj.writeObject(mensajeMandar);
        outobj.flush();
        for (File file : folder.listFiles()) {
            Utilidades.sendFile(file, out,claveSimetrica,claves);
        }


    }
    public static void conexionCliente(ServerSocket serverSocket, KeyPair claves, SecretKey claveSimetrica) throws Exception {

        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        ObjectOutputStream outobj=new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream inobj = new ObjectInputStream(clientSocket.getInputStream());
        PublicKey publicKeyCli=intercambioClaves(outobj,inobj,claves);
        enviarClave(publicKeyCli,claveSimetrica,claves,inobj,outobj);
        recibirFile(in,claveSimetrica,publicKeyCli);
        enviarFiles(claveSimetrica,claves,outobj,out);
        Utilidades.cerrarTodo(clientSocket, out, in);
        System.out.println("sali");



    }

}