package org.py.cube;

import com.badlogic.gdx.graphics.Texture;

public class Textures {

    public static Texture logo1024;
    public static Texture logo2048;
    public static Texture warn;

    public static void create() {
        logo1024 = new Texture("pigeonx1024.png");
        logo2048 = new Texture("pigeonx2048.png");
        warn = new Texture("warn.png");
    }
    public static void dispose() {
        logo1024.dispose();
        logo2048.dispose();
        warn.dispose();
    }

}
