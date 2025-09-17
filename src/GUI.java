import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class GUI {
    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        int offseth=0;
        int offsetv=0;
        int maxoffset=0;
        JFrame frame = new JFrame("Whiteboard");
        frame.setSize(1000,1000);
        File folder= new File("./imgs/");
        ArrayList<JLabel> labels=new ArrayList<>();
        int i=0;
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
