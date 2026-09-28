package org.py.cube;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Error {

    public static void make(String msg) {

        try {
            System.err.println("["+Settings.getString("log_prefix")+"] " + msg);
        } catch(Exception ignored) {}

        JFrame jf = new JFrame("["+Settings.getString("program_name")+"] Error");
        jf.setBounds(50,50,512,256);
        JPanel jp = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                int padding = 20;
                int lineSpacing = 10;
                FontMetrics metrics = g.getFontMetrics();
                ArrayList<StringBuilder> lines = new ArrayList<>();
                lines.add(new StringBuilder());
                int index = 0;
                for(int i = 0; i < msg.length(); i++) {
                    if(metrics.stringWidth(lines.get(index).toString()+msg.charAt(i)) > jf.getWidth() - padding*2 || msg.charAt(i) == '\n') index++;
                    if(index == lines.size()) lines.add(new StringBuilder());
                    lines.get(index).append(msg.charAt(i));
                }
                for(int i = 0; i < lines.size(); i++) {
                    g.drawString(lines.get(i).toString(),padding,padding+i*(metrics.getHeight()+lineSpacing)+metrics.getAscent());
                }
            }
        };
        jf.add(jp);
        jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jf.setVisible(true);
        jf.repaint();

    }

}
