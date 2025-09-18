import javax.swing.*;
import java.io.File;

public class GUIThread extends Thread{
    JFrame frame;
    File folder;

    public GUIThread(JFrame frame, File folder) {
        this.frame = frame;
        this.folder = folder;
    }

    @Override
    public void run(){

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while(frame.isEnabled()){
            int len=folder.listFiles().length;
            Client.decision(false);
            int lennow= folder.listFiles().length;
            if(len!=lennow) {
                len=lennow;
                frame.dispose();
                frame.removeAll();

                GUI.mostrarImgs();
            }
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
