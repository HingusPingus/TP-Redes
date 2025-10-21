import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

public class GUIThread extends Thread{
    JFrame frame;
    File folder;
    String ip;

    public GUIThread(JFrame frame, File folder,String ip) {
        this.frame = frame;
        this.folder = folder;
        this.ip=ip;
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

            Client.startup(false,ip);
            int lennow= folder.listFiles().length;
            if(len!=lennow) {
                len=lennow;
                frame.dispose();
                frame.removeAll();

                GUI.mostrarImgs(ip);
            }
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
