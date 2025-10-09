import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Criptografia {

    public static byte[] encriptar(String algoritmo, PublicKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encrypt=Cipher.getInstance(algoritmo);
        encrypt.init(Cipher.ENCRYPT_MODE, clave);
        byte[] encryptedMessage=encrypt.doFinal(mensaje);
        return encryptedMessage;
    }
    public static byte[] encriptar(String algoritmo, PrivateKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encrypt=Cipher.getInstance(algoritmo);
        encrypt.init(Cipher.ENCRYPT_MODE, clave);
        byte[] encryptedMessage=encrypt.doFinal(mensaje);
        return encryptedMessage;
    }
    public static byte[] encriptar(String algoritmo, SecretKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encrypt=Cipher.getInstance(algoritmo);
        encrypt.init(Cipher.ENCRYPT_MODE, clave);
        byte[] encryptedMessage=encrypt.doFinal(mensaje);
        return encryptedMessage;
    }

    public static byte[] desencriptar(String algoritmo, PublicKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decrypt=Cipher.getInstance(algoritmo);
        decrypt.init(Cipher.DECRYPT_MODE, clave);
        byte[] decryptedMessage=decrypt.doFinal(mensaje);
        return decryptedMessage;
    }
    public static byte[] desencriptar(String algoritmo, PrivateKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decrypt=Cipher.getInstance(algoritmo);
        decrypt.init(Cipher.DECRYPT_MODE, clave);
        byte[] decryptedMessage=decrypt.doFinal(mensaje);
        return decryptedMessage;
    }
    public static byte[] desencriptar(String algoritmo, SecretKey clave, byte[] mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decrypt=Cipher.getInstance(algoritmo);
        decrypt.init(Cipher.DECRYPT_MODE, clave);
        byte[] decryptedMessage=decrypt.doFinal(mensaje);
        return decryptedMessage;
    }
}
