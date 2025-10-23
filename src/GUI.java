import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class GUI {
    public static void crearFrame(String ip){

        JFrame frame = new JFrame("Whiteboard");
        frame.setSize(1000,1000);
        File folder= new File("./imgs2/");
        long cantImgs=folder.length();
        GUIThread hilo=new GUIThread(frame,folder,ip);
        hilo.start();
        mostrarImgs(folder,frame,ip,hilo);



    }
    public static void mostrarImgs(File folder, JFrame frame,String ip,GUIThread hilo){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        int offseth=200;
        int offsetv=0;
        int maxoffset=0;
        ArrayList<JLabel> labels=new ArrayList<>();
        int i=0;
        crearBoton(ip,frame,hilo);
        for (final File fileEntry : folder.listFiles()) {
            if((offseth+200)>=width){
                maxoffset=offseth;
                offseth=0;
                offsetv+=200;
            }
            labels=insertarimagenes(offsetv,offseth,labels,frame,fileEntry,i);
            frame=actualizarFrame(frame,labels,i);
            offseth+=200;
            i++;
            labels=hacerLugar(i,folder,labels,frame,offsetv,maxoffset);
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
        while(frame.isEnabled()){
        }
        hilo.stopThread();
    }
    public static JFrame actualizarFrame(JFrame frame, ArrayList<JLabel>labels, int i){
        frame.add(labels.get(i));
        frame.pack();
        return frame;
    }
    public static ArrayList<JLabel> insertarimagenes(int offsetv, int offseth,ArrayList<JLabel> labels,JFrame frame,File fileEntry, int i){
        JLabel label = new JLabel();
        labels.add(label);
        labels.get(i).setIcon(new ImageIcon(new ImageIcon(fileEntry.getPath()).getImage().getScaledInstance(200,200, Image.SCALE_AREA_AVERAGING)));
        labels.get(i).setBorder(BorderFactory.createEmptyBorder(offsetv,offseth,0,0));
        return labels;
    }
    public static ArrayList<JLabel> hacerLugar(int i,File folder, ArrayList<JLabel> labels,JFrame frame,int offsetv, int maxoffset){

        return labels;
    }
    public static void crearBoton(String ip, JFrame frame, GUIThread hilo){
        JButton button=new JButton("Cargar Imagen");
        button.setSize(200,200);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        button.addActionListener(e -> {
            hilo.stopThread();
            try {
                hilo.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            Client.startup(true, ip);
            frame.dispose();
            frame.removeAll();
            crearFrame(ip);
        });
        frame.add(button);
    }
}
