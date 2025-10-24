import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class Clave {
    private static final String RSA = "RSA";
    public static KeyPair generateRSAKkeyPair() throws Exception{
        SecureRandom secureRandom = new SecureRandom();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);

        keyPairGenerator.initialize(2048, secureRandom);

        return keyPairGenerator.generateKeyPair();
    }
}
