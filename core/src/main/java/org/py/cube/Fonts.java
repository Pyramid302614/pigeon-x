package org.py.cube;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

// https://libgdx.com/wiki/extensions/gdx-freetype
// Core - build.gradle (natives line in dependencies {}) https://github.com/libgdx/libgdx/issues/1821

public class Fonts {

    // HR = high resolution
    public static BitmapFont font48black;
    public static BitmapFont font48blackHR;
    public static BitmapFont font24white;
    public static BitmapFont font24whiteHR;

    public static void create() {
        font48black = loadFont("Coolvetica Rg.otf",48, Color.BLACK);
        font48blackHR = loadFont("Coolvetica Rg.otf",192, Color.BLACK); font48blackHR.getData().setScale(0.25f,0.25f);
        font24white = loadFont("Coolvetica Rg.otf",24, Color.WHITE);
        font24whiteHR = loadFont("Coolvetica Rg.otf",96, Color.WHITE); font24whiteHR.getData().setScale(0.25f,0.25f);
    }
    public static void dispose() {
        font48black.dispose();
        font24white.dispose();
    }

    private static BitmapFont loadFont(String filePath, int size, Color color) {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(new FileHandle(filePath));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.borderStraight = true;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;

    }

}
