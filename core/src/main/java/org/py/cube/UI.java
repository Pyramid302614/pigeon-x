package org.py.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.awt.*;

public class UI {

    public static Prompt currentPrompt = null;

    public static boolean splash = false;
    public static long splashChangeTimestamp = 0;

    public static Point mousePosition;
    public static boolean mouseDown;

    public static void prompt(

        Texture texture,
        String text, // Text/question
        String plus, // Text for the button meaning "YES", "OK" and so on
        String minus, // Text for the button meaning the opposite of the plus, like "NO" or "CANCEL"
        Runnable onPlus,
        Runnable onMinus

    ) {

        if(currentPrompt == null) currentPrompt = new Prompt(
            texture,text,plus,minus,onPlus,onMinus,
            () -> currentPrompt = null // Self de-reference
        );

    }

    public static boolean promptExists() {

        return currentPrompt != null;

    }

    public static void clearPrompt() {
        if(currentPrompt != null) currentPrompt.remove();
    }

    public static void tick() {

        if(currentPrompt != null) currentPrompt.tick();

    }
    public static void render() {

        long now = Time.now();

        if(splash || (now - splashChangeTimestamp) < 10_000) {

            float splashDuration = 250f; // MS
            float alpha = splash ? 1 : ((splashDuration-(now-splashChangeTimestamp))/splashDuration);
            alpha = Math.max(0,Math.min(1,alpha));

            float vw = Graphics2D.WIDTH;
            float vh = Graphics2D.HEIGHT;

            Texture logo = Gdx.graphics.getWidth() > 1200f ? Textures.logo2048 : Textures.logo1024;

            Graphics2D.fill(new Color(255,255,255,alpha));
            Graphics2D.rect(0,0,vw,vh);

            float logoHeight = vh*0.6f;
            float logoWidth = logoHeight / logo.getHeight() * logo.getWidth();

            Graphics2D.texture(logo,(vw-logoWidth)/2f,((vh-logoHeight)/2f),logoWidth,logoHeight,alpha);

        }

        if(currentPrompt != null) currentPrompt.render();

    }

    public static void splashOn() {
        splash = true;
        splashChangeTimestamp = Time.now();
    }
    public static void splashOff() {
        splash = false;
        splashChangeTimestamp = Time.now();
    }

}
