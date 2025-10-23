import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class MensajeFirma implements Serializable {
    private byte [] mensajeEncriptado;
    private byte [] firma;

    public MensajeFirma(byte[] mensajeEncriptado, byte[] firma) {
        this.mensajeEncriptado = mensajeEncriptado;
        this.firma = firma;
    }

    public byte[] getMensajeEncriptado() {
        return mensajeEncriptado;
    }

    public void setMensajeEncriptado(byte[] mensajeEncriptado) {
        this.mensajeEncriptado = mensajeEncriptado;
    }

    public byte[] getFirma() {
        return firma;
    }

    public void setFirma(byte[] firma) {
        this.firma = firma;
    }
    public byte[] desecriptarMsj(SecretKey clave) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return Criptografia.desencriptar("AES", clave, mensajeEncriptado);
    }

    public byte[] desencriptarFirma(PublicKey publicKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return Criptografia.desencriptar("RSA", publicKey, firma);
    }
}
