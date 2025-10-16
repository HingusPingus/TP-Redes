import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

public class GUIThread extends Thread{
    JFrame frame;
    File folder;
    SecretKey clave;
    PublicKey publicKeyServ;
    KeyPair claves;

    public GUIThread(JFrame frame, File folder, SecretKey clave, PublicKey publicKeyServ, KeyPair claves) {
        this.frame = frame;
        this.folder = folder;
        this.clave = clave;
        this.publicKeyServ = publicKeyServ;
        this.claves = claves;
    }

    @Override
    public void run(){

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
            int len=folder.listFiles().length;
        while(frame.isEnabled()){

            Client.decision(false,clave,publicKeyServ,claves);
            int lennow= folder.listFiles().length;
            if(len!=lennow) {
                len=lennow;
                frame.dispose();
                frame.removeAll();

                GUI.mostrarImgs(clave,publicKeyServ,claves);
            }
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
