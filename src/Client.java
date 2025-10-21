import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.SecureDirectoryStream;
import java.security.*;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class Client {
    public static void main(String[] args) throws Exception
    {
        startup(true,args[0]);
        GUI.mostrarImgs(args[0]);

    }
    public static void startup(boolean mandar, String ip) {
        try {
            Socket sockett = new Socket(ip, 9999);
            DataInputStream in = new DataInputStream(sockett.getInputStream());

            KeyPair claves =Clave.generateRSAKkeyPair();
            PublicKey publicKeyServ=intercambioClaves(sockett,claves);
            SecretKey clave = recibirSimetrica(sockett,claves,publicKeyServ,in);
            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            if(clave.equals(null)){
                throw new RuntimeException("Ha ocurrido un error");
            }
            decision(mandar,sockett,out,in,publicKeyServ,claves,clave);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void decision(boolean mandar,Socket sockett, DataOutputStream out, DataInputStream in,
    PublicKey publicKeyServ,KeyPair claves,SecretKey clave) throws Exception {
        if (mandar) {
            out.writeBoolean(true);
            mandarNuevo(sockett, out, in,clave,claves);
        } else {
            out.writeBoolean(false);
        }
        actualizarLista(sockett, out, in,clave,publicKeyServ,claves);
    }
    private static PublicKey intercambioClaves(Socket sockett,KeyPair claves) throws IOException, ClassNotFoundException {
        ObjectOutputStream outobj =new ObjectOutputStream(sockett.getOutputStream());
        ObjectInputStream inobj =new ObjectInputStream(sockett.getInputStream());
        outobj.writeObject(claves.getPublic());
        outobj.flush();
        return(PublicKey) inobj.readObject();

    }
    private static SecretKey recibirSimetrica(Socket sockett,KeyPair claves,PublicKey publicKeyServ,DataInputStream in) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {
        ObjectInputStream inobj =new ObjectInputStream(sockett.getInputStream());

        MensajeFirma msj= (MensajeFirma) inobj.readObject();
        byte[] decryptedMessage=Criptografia.desencriptar("RSA",claves.getPrivate(),msj.getMensajeEncriptado());
        byte[] encodedhash = Utilidades.hashear(decryptedMessage);
        if(Arrays.equals(msj.desencriptarFirma(publicKeyServ), encodedhash)){
            return new SecretKeySpec(decryptedMessage, "AES");
        }
        return null;
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
        Utilidades.sendFile(file,out,clave,claves);
    }
    private static void actualizarLista(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave, PublicKey publicKeyServ, KeyPair claves) {
        try {
            byte[] message =Utilidades.receiveMessage(in,publicKeyServ,clave);
            if (message!=null) {
                int longitud = (int) Utilidades.deserialize(message);
                prepararDirectorio();
                for (int i = 0; i < longitud; i++) {
                    Utilidades.receiveFile(in, clave, publicKeyServ);
                }
            }
            cerrarTodo(sockett, out, in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void prepararDirectorio(){
        File theDir = new File("./imgs2/");
        limpiarDirectorio(theDir);
        crearDirectorio(theDir);
    }
    public static void limpiarDirectorio(File theDir){
        String[] entries = theDir.list();
        if(entries!=null) {
            for (String s : entries) {
                File currentFile = new File(theDir.getPath(), s);
                currentFile.delete();
            }
        }
    }
    public static void crearDirectorio(File theDir){
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

}