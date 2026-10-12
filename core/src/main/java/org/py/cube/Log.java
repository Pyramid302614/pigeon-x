package org.py.cube;

import org.py.cmm5.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class Log {

    private static JFrame jf;
    private static String content = "";
    private static String typing = "";

    private final static Font font = new Font("monospaced",Font.PLAIN,12);

    public static void flare() { // The most important method holding the whole project up
        log("FLARE!");
    }

    public static void log(String msg) {

        System.out.println("[" + Settings.getString("log_prefix") + "] " + msg);
        content += msg + "\n";
        if(jf != null) jf.repaint();

    }
    public static void log() {
        log("\n");
    }

    public static void window() {

        if(jf != null) return;

        if(commands.isEmpty()) initCommands();
        jf = new JFrame(Settings.getString("program_name") + " : Log");
        JPanel jp = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,getWidth(),getHeight());
                g.setFont(font);
                FontMetrics metrics = g.getFontMetrics();
                int padding = 20;
                int lineSpacing = 10;
                ArrayList<StringBuilder> lines = new ArrayList<>();
                lines.add(new StringBuilder());
                int index = 0;
                for(int i = 0; i < content.length(); i++) {
                    if(metrics.stringWidth(lines.get(index).toString()+content.charAt(i)) > getWidth() - padding*2 || content.charAt(i) == '\n') index++;
                    if(index == lines.size()) lines.add(new StringBuilder());
                    lines.get(index).append(content.charAt(i));
                }
                g.setColor(Color.GREEN);
                for(int i = 0; i < lines.size(); i++) {
                    g.drawString(lines.get(i).toString(),padding,padding+i*(metrics.getHeight()+lineSpacing)+metrics.getAscent());
                }
                g.setColor(Color.BLUE);
                g.drawString(typing,padding,getHeight()-padding+metrics.getDescent());

            }
        };
        jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jf = null; // Eventual dereference
            }
        });
        jf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { // e.getKeyCode() doesn't work with keyTyped() for some reason; it always returns 0
                if(e.getKeyCode() == 10) { // Enter
                    if(parseCommand(typing)) typing = ""; // If it ran good, clear it
                    if(jf != null) jf.repaint();
                } else
                if(e.isControlDown() && e.getKeyCode() == 88) { // Ctrl+X
                    content = "< Console cleared >\n";
                    jf.repaint();
                } else
                if(e.getKeyCode() == 27) { // Esc
                    typing = "";
                    jf.repaint();
                } else
                if(e.getKeyCode() == 8 && !typing.isEmpty()) { // Backspace
                    typing = typing.substring(0,typing.length()-1);
                    jf.repaint();
                } else
                if(!e.isActionKey() && e.getKeyCode() >= 32) {
                    typing += e.getKeyChar(); // Automatically accounts for case
                    jf.repaint();
                }
            }
        });
        jf.setBounds(50,50,500,800); // I messed up the coordinates here but it turned out fine
        jf.add(jp);
        jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jf.setVisible(true);
        jf.repaint();

    }

    public static HashMap<String,Function<String[],String>> commands = new HashMap<>();

    private static void initCommands() {
        commands.clear(); // Just to be safe
        commands.put("echo",args -> {
            if(args.length == 0) return "echo <text>";
            return args[0];
        });
        commands.put("termalink",args -> {
            if(args.length == 0) return "termalink <get/set> <name> <set:value>";
            if(args[0].equals("list")) return Arrays.toString(Termalink.list());
            if(!Termalink.exists(args[1])) return "Unknown name: " + args[1];
            switch(args[0]) {
                case "get":
                    return Termalink.get(args[1]).get();
                case "set":
                    if(args.length < 3) return "Please specify a value to set '" + args[1] + "' to.";
                    Termalink.get(args[1]).set(args[2]);
                    return "Successfully updated value.";
                default:
                    return "Unknown method: " + args[0] + "; use 'get' or 'set'";
            }
        });
        commands.put("cmm5",args -> {
            Dev.cmm5 = true;
            org.py.cmm5.Main.init();
            return "Successfully booted ControlsManager Model 5.";
        });
        commands.put("cmm5-config",args -> {
            try {
                Desktop.getDesktop().open(new File(Main.configPath));
            } catch(IOException e) {
                return e.getMessage();
            }
            return "Launched file in desktop.";
        });
        commands.put("exit",args -> {
            closeWindow();
            return "<-";
        });
    }
    private static String runCommand(String name, String[] args) {
        return commands.get(name).apply(args);
    }
    // Returns if it successfully ran or not
    private static boolean parseCommand(String raw) {
        String[] split = raw.split(" ");
        if(!commands.containsKey(split[0])) {
            log("[ERR:"+split[0]+"] Unknown command :(");
            return false;
        }
        try {
            log("["+split[0]+"] " + runCommand(
                split[0],
                split.length > 1 ? raw.substring(split[0].length()+1).split(" ") : new String[0]
            ));
        } catch(Exception e) {
            log("[ERR:"+split[0]+"] " + e);
            return false;
        }
        return true;
    }

    public static void closeWindow() {
        if(jf != null) jf.dispose();
        forceClose();
    }
    public static void forceClose() {
        jf = null;
    }

}
