import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import static java.lang.Thread.sleep;
import java.util.logging.Level;

public class Client {
    public static void main(String[] args) throws Exception
    {
        Utilidades.logger.setLevel(Level.parse(args[1]));
        startup(true,args[0]);
        GUI.crearFrame(args[0]);

    }
    public static void startup(boolean mandar, String ip) {
        try {
            File file=null;
            if(mandar){
                file=seleccionarImagen();
            }
            Socket sockett = new Socket(ip, 9999);

            ObjectOutputStream outobj =new ObjectOutputStream(sockett.getOutputStream());
            ObjectInputStream inobj =new ObjectInputStream(sockett.getInputStream());
            outobj.flush();
            KeyPair claves =Clave.generateRSAKkeyPair();
            PublicKey publicKeyServ=intercambioClaves(sockett,claves,outobj,inobj);
            DataInputStream in = new DataInputStream(sockett.getInputStream());
            SecretKey clave = recibirSimetrica(sockett,claves,publicKeyServ,inobj);
            if(clave.equals(null)){
                throw new RuntimeException("Ha ocurrido un error");
            }
            DataOutputStream out = new DataOutputStream(sockett.getOutputStream());
            decisionMandar(mandar,sockett,out,in,publicKeyServ,claves,clave,inobj,file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void decisionMandar(boolean mandar, Socket sockett, DataOutputStream out, DataInputStream in,
                                       PublicKey publicKeyServ, KeyPair claves, SecretKey clave, ObjectInputStream inobj,
                                       File file) throws Exception {
        if (mandar&&file!=null) {
            out.writeBoolean(true);
            mandarImagen(sockett, out, in,clave,claves,file);
        } else {
            out.writeBoolean(false);
        }
        actualizarLista(sockett,inobj, out, in,clave,publicKeyServ,claves);
    }
    private static PublicKey intercambioClaves(Socket sockett,KeyPair claves,ObjectOutputStream outobj,ObjectInputStream inobj) throws IOException, ClassNotFoundException {

        outobj.writeObject(claves.getPublic());
        outobj.flush();
        return(PublicKey) inobj.readObject();

    }
    private static SecretKey recibirSimetrica(Socket sockett,KeyPair claves,PublicKey publicKeyServ,ObjectInputStream inobj) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ClassNotFoundException {

        MensajeFirma msj= (MensajeFirma) inobj.readObject();
        byte[] decryptedMessage=Criptografia.desencriptar("RSA",claves.getPrivate(),msj.getMensajeEncriptado());
        byte[] encodedhash = Utilidades.hashear(decryptedMessage);
        byte[] firma=msj.desencriptarFirma(publicKeyServ);
        if(Arrays.equals(firma, encodedhash)){
            return new SecretKeySpec(decryptedMessage, "AES");
        }
        return null;
    }
    private static void mandarImagen(Socket sockett, DataOutputStream out, DataInputStream in, SecretKey clave, KeyPair claves,File file) throws Exception {

        Utilidades.sendFile(file,out,clave,claves);
    }
    private static void actualizarLista(Socket sockett,ObjectInputStream inobj, DataOutputStream out, DataInputStream in, SecretKey clave, PublicKey publicKeyServ, KeyPair claves) {
        try {
            byte[] message =Utilidades.receiveMessage(inobj,publicKeyServ,clave);
            if (message!=null) {
                int longitud = (int) Utilidades.deserialize(message);
                prepararDirectorio();
                for (int i = 0; i < longitud; i++) {
                    Utilidades.receiveFile(in, clave, publicKeyServ,"./imgs2/");
                }
            }
            Utilidades.cerrarTodo(sockett, out, in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static File seleccionarImagen(){
        File file = null;
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Seleccione una imagen", "jpg", "gif","jpeg","png");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(null);

        if(returnVal == JFileChooser.APPROVE_OPTION){
            return fc.getSelectedFile();
        }
        return null;
    }

    public static void prepararDirectorio(){
        File theDir = new File("./imgs2/");
        crearDirectorio(theDir);
        limpiarDirectorio(theDir);
    }
    public static void limpiarDirectorio(File theDir){
        for(File f:theDir.listFiles()){
            f.delete();
        }
    }
    public static void crearDirectorio(File theDir){
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

}