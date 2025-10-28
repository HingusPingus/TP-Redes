import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;

public class GUIThread extends Thread{
    JFrame frame;
    File folder;
    String ip;
    private volatile boolean running = true;
    public GUIThread(JFrame frame, File folder,String ip) {
        this.frame = frame;
        this.folder = folder;
        this.ip=ip;
        this.running=true;
    }

    @Override
    public void run(){
        try {
            int i=0;
            while(running&&i<10){
                sleep(1000);
                i++;
            }
            int len = folder.listFiles().length;
            while (running) {
                if (actualizarFiles(len)) {
                    len = folder.listFiles().length;
                }

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean actualizarImgs(int len, int lennow) {
        if (len != lennow) {
            frame.dispose();
            frame.removeAll();
            GUI.crearFrame(ip);
            return true;
        }
        return false;
    }
    public boolean actualizarFiles(int len){
        Client.startup(false,ip);
        int lennow= folder.listFiles().length;
        if(actualizarImgs(len,lennow)){
            return true;
        }
        return false;
    }

    public void stopThread(){
        running=false;
    }
}
