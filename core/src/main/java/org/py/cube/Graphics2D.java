package org.py.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

// https://libgdx.com/wiki/graphics/opengl-utils/rendering-shapes
// roundrect - https://gamedev.stackexchange.com/questions/73103/how-do-i-render-a-rounded-rectangle-in-libgdx

public class Graphics2D {

    private static SpriteBatch spriteBatch;
    private static ShapeRenderer shapeRenderer;
    private static OrthographicCamera camera;
    private static FitViewport viewport;

    public static final float WIDTH = Gdx.graphics.getWidth();
    public static final float HEIGHT = Gdx.graphics.getHeight();

    public static void create() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WIDTH,HEIGHT,camera);
    }
    public static void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }



    public static Color fillColor = Color.RED;
    public static Color strokeColor = Color.BLACK;

    public static BitmapFont font = new BitmapFont(); // Uses default

    public static void fill(Color fill) {
        fillColor = fill;
        strokeColor = null;
    }
    public static void stroke(Color stroke) {
        fillColor = null;
        strokeColor = stroke;
    }
    public static void colors(Color fill, Color stroke) {
        fillColor = fill.cpy();
        strokeColor = stroke.cpy();
    }
    public static void setFont(BitmapFont font) {
        Graphics2D.font = font;
    }
    public static Vector2 stringBounds(String string) {
        GlyphLayout glyphLayout = new GlyphLayout();
        glyphLayout.setText(font,string);
        return new Vector2(
            glyphLayout.width,
            glyphLayout.height
        );
    }


    public static void rect(float x, float y, float w, float h) {
        ShapeRender(() -> shapeRenderer.rect(x,HEIGHT-y-h,w,h));
    }
    public static void texture(Texture texture, float x, float y, float width, float height) {
        texture(texture,x,y,width,height,1f);
    }
    public static void texture(Texture texture, float x, float y, float width, float height, float transparency) {
        SpriteRender(() -> {
            spriteBatch.setColor(1f,1f,1f,transparency);
            spriteBatch.draw(texture,x,HEIGHT-y-height,width,height);
            spriteBatch.setColor(1f,1f,1f,1f); // Reset
        });
    }
    public static void text(String text, float x, float y) {
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        SpriteRender(() -> {
            spriteBatch.setColor(fillColor);
            font.draw(spriteBatch,text,x,HEIGHT-y+font.getAscent());
            spriteBatch.setColor(1f,1f,1f,1f); // Reset
        });
    }

    public static void roundrect(float x, float y_, float width, float height, float radius){

        final float y = HEIGHT-y_-height;

        ShapeRender(() -> {

            // Central rectangle
            shapeRenderer.rect(x + radius, y + radius, width - 2*radius, height - 2*radius);

            // Four side rectangles, in clockwise order
            shapeRenderer.rect(x + radius, y, width - 2*radius, radius);
            shapeRenderer.rect(x + width - radius, y + radius, radius, height - 2*radius);
            shapeRenderer.rect(x + radius, y + height - radius, width - 2*radius, radius);
            shapeRenderer.rect(x, y + radius, radius, height - 2*radius);

            // Four arches, clockwise too
            shapeRenderer.arc(x + radius, y + radius, radius, 180f, 90f);
            shapeRenderer.arc(x + width - radius, y + radius, radius, 270f, 90f);
            shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0f, 90f);
            shapeRenderer.arc(x + radius, y + height - radius, radius, 90f, 90f);

        });

    }

    private static void ShapeRender(Runnable action) {

        if(fillColor != null) {
            if(fillColor.a != 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(fillColor);
            action.run();
            shapeRenderer.end();
        }
        if(strokeColor != null) {
            if(strokeColor.a != 1) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(strokeColor);
            action.run();
            shapeRenderer.end();
        }

    }
    private static void SpriteRender(Runnable action) {
        spriteBatch.begin();
        action.run();
        spriteBatch.end();
    }

    public static void resize(int w, int h) {
        viewport.update(w,h);
    }

}
