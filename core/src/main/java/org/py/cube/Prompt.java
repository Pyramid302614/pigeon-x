package org.py.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import org.py.cube.Fonts;
import org.py.cube.Graphics2D;
import org.py.cube.Time;
import org.py.cube.UI;

public class Prompt {

    public Texture texture;
    public String text;
    public String plus;
    public String minus;
    public Runnable onPlus;
    public Runnable onMinus;

    // Off = Offset
    private float[] plusSizeOff = { 0f, 0f }; // T C
    private float[] minusSizeOff = { 0f, 0f }; // T C

    private float[] plusPos = {0f,0f};
    private float[] minusPos = {0f,0f};
    private float[] plusDims = {0f,0f};
    private float[] minusDims = {0f,0f};

    public final long createdTimestamp;

    private boolean removeMe = false;
    private long removeMeTimestamp;
    private Runnable remove;

    private boolean previousMouseDown = false;

    public Prompt(

        Texture texture,
        String text,
        String plus,
        String minus,
        Runnable onPlus,
        Runnable onMinus,

        Runnable deletion

    ) {

        this.texture = texture;
        this.text = text;
        this.plus = plus;
        this.minus = minus;
        this.onPlus = onPlus;
        this.onMinus = onMinus;

        this.remove = deletion;

        createdTimestamp = Time.now();

    }

    public void tick() {

        if(removeMe && Time.now() - removeMeTimestamp > 200f) remove.run();

        float mx = (float) (UI.mousePosition != null ? UI.mousePosition.x : 0) / Gdx.graphics.getWidth() * Graphics2D.WIDTH;
        float my = (float) (UI.mousePosition != null ? UI.mousePosition.y : 0) / Gdx.graphics.getHeight() * Graphics2D.HEIGHT;

        boolean p_x = mx > plusPos[0] && mx < plusPos[0] + plusDims[0];
        boolean p_y = my > plusPos[1] && my < plusPos[1] + plusDims[1];
        boolean m_x = mx > minusPos[0] && mx < minusPos[0] + minusDims[0];
        boolean m_y = my > minusPos[1] && my < minusPos[1] + minusDims[1];

        if(p_x && p_y) {
            if(UI.mouseDown) {
                plusSizeOff[1] = Graphics2D.HEIGHT * -0.015f;
            } else if(previousMouseDown) {
                onPlus.run();
                remove();
            } else {
                plusSizeOff[1] = Graphics2D.HEIGHT * 0.015f;
            }
        } else plusSizeOff[1] = 0;
        if(m_x && m_y) {
            if(UI.mouseDown) {
                minusSizeOff[1] = Graphics2D.HEIGHT * -0.015f;
            } else if(previousMouseDown) {
                onMinus.run();
                remove();
            } else {
                minusSizeOff[1] = Graphics2D.HEIGHT * 0.015f;
            }
        } else minusSizeOff[1] = 0;

        plusSizeOff[0] += (plusSizeOff[1] - plusSizeOff[0]) / 5f;
        minusSizeOff[0] += (minusSizeOff[1] - minusSizeOff[0]) / 5f;

        previousMouseDown = UI.mouseDown;

    }
    public void render() {

        // Background

        long now = Time.now();

        float alpha = 255; // Idk if it's 255 or 1 :shrug:
        if(removeMe) {
            alpha = Math.max(0f,1f-(now-removeMeTimestamp)/200f);
        } else {
            alpha = Math.min(1f,(now-createdTimestamp)/200f);
        }
        Graphics2D.fill(new Color(255f,255f,255f,alpha));
        Graphics2D.rect(0,0,Graphics2D.WIDTH,Graphics2D.HEIGHT);

        // Prompt

        if(removeMe) return; // Don't render prompt contents once marked for removal

        boolean highRes = Gdx.graphics.getWidth() > 1000f;

        float padding = Graphics2D.WIDTH * 0.03f;

        float textureHeight = Graphics2D.HEIGHT * 0.4f;
        float textureWidth = textureHeight / texture.getHeight() * texture.getWidth();

        float textSize = Graphics2D.HEIGHT * 0.08f;
        Graphics2D.setFont(highRes ? Fonts.font48blackHR : Fonts.font48black);
        Vector2 textBounds = Graphics2D.stringBounds(text);

        float buttonHeight = Graphics2D.HEIGHT * 0.1f;
        float buttonCornerRadius = buttonHeight * 0.333f;
        Graphics2D.setFont(highRes ? Fonts.font24whiteHR : Fonts.font24white);
        Vector2 plusBounds = Graphics2D.stringBounds(plus);
        Vector2 minusBounds = Graphics2D.stringBounds(minus);
        float buttonsEntireWidth = padding + plusBounds.x + padding*3 + minusBounds.x + padding;
        float buttonsBase = (Graphics2D.WIDTH - buttonsEntireWidth) / 2f; // X

        float totalHeight = textureHeight + padding + textSize + padding + buttonHeight;
        float base = (Graphics2D.HEIGHT-(totalHeight))/2f; // Y

        Graphics2D.texture(texture,(Graphics2D.WIDTH-textureWidth)/2f,base,textureWidth,textureHeight);

        Graphics2D.setFont(Fonts.font48black);
        Graphics2D.text(text,(Graphics2D.WIDTH-textBounds.x)/2f,base+textureHeight+padding+Graphics2D.font.getAscent());

        Graphics2D.fill(Color.BLACK);
        plusPos = new float[] { buttonsBase-plusSizeOff[0],base+textureHeight+padding+textBounds.y+padding-plusSizeOff[0] };
        plusDims = new float[] { plusBounds.x+padding*2+plusSizeOff[0]*2,plusBounds.y+padding*2+plusSizeOff[0]*2 };
        Graphics2D.roundrect(plusPos[0],plusPos[1],plusDims[0],plusDims[1],buttonCornerRadius);
        minusPos = new float[] { buttonsBase+plusBounds.x+padding*3-minusSizeOff[0],base+textureHeight+padding+textBounds.y+padding-minusSizeOff[0] };
        minusDims = new float[] { minusBounds.x+padding*2+minusSizeOff[0]*2,minusBounds.y+padding*2+minusSizeOff[0]*2 };
        Graphics2D.roundrect(minusPos[0],minusPos[1],minusDims[0],minusDims[1],buttonCornerRadius);

        Graphics2D.setFont(Fonts.font24white);
        Graphics2D.text(plus,buttonsBase+padding,base+textureHeight+padding+textBounds.y+padding+padding+Graphics2D.font.getAscent());
        Graphics2D.text(minus,buttonsBase+padding+plusBounds.x+padding*3,base+textureHeight+padding+textBounds.y+padding+padding+Graphics2D.font.getAscent());

    }


    public void remove() {

        removeMe = true;
        removeMeTimestamp = Time.now();

    }

}
