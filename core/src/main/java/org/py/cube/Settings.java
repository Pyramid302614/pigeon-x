package org.py.cube;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Settings {

    // Windows compatible only
    private static final String path = System.getenv("APPDATA") + "\\.cube";
    private static final String fileName = "settings.txt";

    private static final HashMap<String,String> cache = new HashMap<>();

    public static void defaults() {

        cache.clear();
        cache.put("zoom_sensitivity","0.2f");
        cache.put("zoom_smoothly","true");
        cache.put("log_prefix","CUBE");
        cache.put("program_name","Cube");
        cache.put("vsync","true");
        cache.put("window_resolution","925x480");
        cache.put("default_fov","60f");
        cache.put("dragging_sensitivity","0.02f");

    }

    public static File file() {

        return new File(path + "/" + fileName);

    }

    // Saves file to cache
    public static void cache() {

        try {
            File dir = new File(path);
            File file = new File(path + "/" + fileName);
            if(!dir.exists()) {
                if(!dir.mkdir()) {
                    Error.make("Error creating %AppData% directory.");
                } else if(!file.createNewFile()) {
                    Error.make("Error creating settings file.");
                } else {
                    // Writes the defaults to the file
                    defaults();
                    deCache();
                }
            } else {
                Scanner sc = new Scanner(file);
                boolean write = false;
                while(sc.hasNextLine()) {
                    if(!write) if(sc.nextLine().equals("@")) write = true;
                    if(write) {
                        String def = sc.nextLine();
                        String setting = def.split(":")[0];
                        String value = def.substring(setting.length()+1);
                        cache.put(setting,value); // Overrides any values it finds, leaves the defaults
                    }
                }
                if(!write) Error.make("Settings file is missing '@' to show start of compilation, and so all settings have been overridden with the defaults. (Ctrl+Shift+S to edit)");
                sc.close();
            }

        } catch(IOException e) {
            defaults(); // Fall back to defaults, must before go Error.make, because it uses "log_prefix" setting
            Error.make("Error caching settings: " + e.getMessage());
        }

    }

    // Saves cache to file (Requires file to exist to work)
    public static void deCache() {

        File file = new File(path + "/" + fileName);

        if(!file.exists()) return;

        try {

            FileWriter fw = new FileWriter(file,false); // Override-mode
            StringBuilder newContents = new StringBuilder();
            newContents.append(header).append("\n\n@\n");
            cache.forEach((k,v) -> {
                newContents.append(k).append(":").append(v).append("\n");
            });
            fw.write(newContents.toString());
            fw.flush();
            fw.close();

        } catch(IOException e) {
            Error.make("Error de-caching settings: " + e.getMessage());
        }

    }

    public static String getString(String setting) {

        // It's going to fail anyway, might as well give a more detailed error
        if(!cache.containsKey(setting)) throw new RuntimeException("[CUBE-ENGINE] Failed to find setting: \"" + setting + "\"");

        return cache.get(setting);

    }
    public static void set(String setting, String value) {

        cache(); // Pull the changes
        cache.put(setting,value); // Rebase
        deCache(); // Push the changes

    }

    public static boolean getBoolean(String setting) {

        try {
            return getString(setting).equals("true");
        } catch(Exception ignored) {
            throw new RuntimeException("[CUBE-ENGINE] Setting \"" + setting + "\" is not a boolean.");
        }

    }
    public static float getFloat(String setting) {

        try {
            return Float.parseFloat(getString(setting));
        } catch(NumberFormatException ignored) {
            throw new RuntimeException("[CUBE-ENGINE] Setting \"" + setting + "\" is not a float.");
        } catch(Exception e) {
            throw new RuntimeException("[CUBE-ENGINE] Setting \"" + setting + "\" failed to be parsed as float.");
        }

    }

    private static final String header =
        " ________  ___  ________  _______   ________  ________           ___  ___\n" +
        "|\\   __  \\|\\  \\|\\   ____\\|\\  ___ \\ |\\   __  \\|\\   ___  \\        |\\  \\|\\  \\\n" +
        "\\ \\  \\|\\  \\ \\  \\ \\  \\___|\\ \\   __/|\\ \\  \\|\\  \\ \\  \\\\ \\  \\       \\ \\__\\_\\__\\\n" +
        " \\ \\   ____\\ \\  \\ \\  \\  __\\ \\  \\_|/_\\ \\  \\\\\\  \\ \\  \\\\ \\  \\       \\|__|\\_\\_|_\n" +
        "  \\ \\  \\___|\\ \\  \\ \\  \\|\\  \\ \\  \\_|\\ \\ \\  \\\\\\  \\ \\  \\\\ \\  \\        |\\  \\|\\  \\\n" +
        "   \\ \\__\\    \\ \\__\\ \\_______\\ \\_______\\ \\_______\\ \\__\\\\ \\__\\       \\ \\__\\ \\__\\\n" +
        "    \\|__|     \\|__|\\|_______|\\|_______|\\|_______|\\|__| \\|__|        \\|__|\\|__|";

    //     ________  ___  ________  _______   ________  ________           ___  ___
    //    |\   __  \|\  \|\   ____\|\  ___ \ |\   __  \|\   ___  \        |\  \|\  \
    //    \ \  \|\  \ \  \ \  \___|\ \   __/|\ \  \|\  \ \  \\ \  \       \ \__\_\__\
    //     \ \   ____\ \  \ \  \  __\ \  \_|/_\ \  \\\  \ \  \\ \  \       \|__|\_\_|_
    //      \ \  \___|\ \  \ \  \|\  \ \  \_|\ \ \  \\\  \ \  \\ \  \        |\  \|\  \
    //       \ \__\    \ \__\ \_______\ \_______\ \_______\ \__\\ \__\       \ \__\ \__\
    //        \|__|     \|__|\|_______|\|_______|\|_______|\|__| \|__|        \|__|\|__|
    //

}
