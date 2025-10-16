import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

public class GUI {
    public static void mostrarImgs(SecretKey clave, PublicKey publicKeyServ, KeyPair claves){

        JFrame frame = new JFrame("Whiteboard");
        frame.setSize(1000,1000);
        File folder= new File("./imgs2/");
        long cantImgs=folder.length();
        actualizarImgs(folder,frame,clave,publicKeyServ,claves);
        GUIThread hilo=new GUIThread(frame,folder,clave,publicKeyServ,claves);
        hilo.start();


    }
    public static void actualizarImgs(File folder, JFrame frame, SecretKey clave, PublicKey publicKeyServ, KeyPair claves){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();


        int offseth=0;
        int offsetv=0;
        int maxoffset=0;
        ArrayList<JLabel> labels=new ArrayList<>();
        int i=0;
        JButton button=new JButton("Cargar Imagen");
        button.setSize(200,200);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(offsetv,offseth,0,0));

        button.addActionListener(e -> {
            Client.decision(true,clave,publicKeyServ,claves);
            frame.dispose();
            frame.removeAll();});
        frame.add(button);
        offseth+=200;
        for (final File fileEntry : folder.listFiles()) {
            if((offseth+200)>=width){
                maxoffset=offseth;
                offseth=0;
                offsetv+=200;
            }

            JLabel label = new JLabel();
            labels.add(label);
            labels.get(i).setIcon(new ImageIcon(new ImageIcon(fileEntry.getPath()).getImage().getScaledInstance(200,200, Image.SCALE_AREA_AVERAGING)));
            labels.get(i).setBorder(BorderFactory.createEmptyBorder(offsetv,offseth,0,0));
            frame.add(labels.get(i));
            frame.pack();
            offseth=labels.get(i).getWidth();
            i++;
            if(i==folder.listFiles().length){
                JLabel labelf = new JLabel();

                labels.add(labelf);
                labels.get(i).setBorder(BorderFactory.createEmptyBorder(offsetv+200,maxoffset,0,0));
                frame.add(labels.get(i));

                frame.pack();

            }
        }




        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
