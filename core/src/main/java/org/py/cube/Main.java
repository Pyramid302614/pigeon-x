// Programmed with Natural Intelligence

package org.py.cube;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import org.py.cmm5.Polyware;
import org.py.cube.dev.ControlModels;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Main extends ApplicationAdapter implements InputProcessor {

    private Camera camera;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private Model model;
    private Environment environment;

    private boolean rightClick = false;
    private boolean middleButton = false;
    private Point mousePosition;
    private Point previousMousePosition;
    private boolean leftClick = false;

    private float zoomingSensitivity;
    private float zoomingEasingInteger; // Zooming - user control
    private float targetScale = 1;
    private float scale = 1;

    private float FOV;
    private float previousFOV = 60;
    private float targetFOV;
    private final Vector3 up = new Vector3(0,1,0);
    private final Vector3 targetUp = new Vector3(0,1,0);

    private final Vector3 subject = new Vector3(0,0,0);
    private final Vector3 targetSubject = new Vector3(0,0,0);

    private boolean userManipulate = false;
    private boolean userRotate = true;

    // pbe = Pre-birds-eye
    private boolean birdsEye = false;
    private float dragging_sensitivity;
    private float pbe_targetScale;
    private float pbe_targetXRot;
    private float pbe_targetYRot;
    private float pbe_targetFOV;

    private float xRot = 0;
    private float yRot = 0;
    private float targetXRot = 0;
    private float targetYRot = 0;
    private float rotationEasingInteger = 10f;

    private float ps_zoomingEasingInteger;
    private float ps_rotationEasingInteger;

    private boolean botDiscovered = true;
    private boolean previousBotDiscovered = true;

    private int botState = 0;

    private boolean sim = false; // Simulation mode

    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    @Override
    public void create() {

        Settings.defaults(); // Sets defaults
        Settings.cache(); // Adds any settings in settings.txt to the cache
        Settings.deCache(); // Pushes the whole cache to the file

        zoomingSensitivity = Settings.getFloat("zoom_sensitivity");
        zoomingEasingInteger = Settings.getBoolean("zoom_smoothly") ? 7f : 0f;
        dragging_sensitivity = Settings.getFloat("dragging_sensitivity");

        // Configuration
        Gdx.graphics.setTitle(Settings.getString("program_name"));
        Gdx.graphics.setVSync(Settings.getBoolean("vsync"));
        String windowRes = Settings.getString("window_resolution");
        Gdx.graphics.setWindowedMode(Integer.parseInt(windowRes.split("x")[0]),Integer.parseInt(windowRes.split("x")[1]));

        Textures.create();
        Fonts.create();
        Graphics2D.create();

        new Termalink("botDiscovered",() -> Boolean.toString(botDiscovered),v -> botDiscovered = v.equals("true"));
        new Termalink("sim",() -> Boolean.toString(sim),v -> sim = v.equals("true"));
        new Termalink("dev",() -> Boolean.toString(Dev.dev),v -> Dev.dev = v.equals("true"));

        UI.clearPrompt();
        Log.forceClose();

        ControlModels.testBot(v -> botState = v,() -> botState);

        splashOn();
        scheduler.schedule(() -> {
            if(botDiscovered) splashOff();
        },2,TimeUnit.SECONDS);

        targetFOV = Settings.getFloat("default_fov");
        FOV = targetFOV;

        Gdx.input.setInputProcessor(this);

        modelBatch = new ModelBatch();
        setUpCamera(FOV);

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(Color.RED));
        model = modelBuilder.createBox(8f,8f,8f,material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instance = new ModelInstance(model);
        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(0.6f,0.6f,0.6f,1f));
        environment.add(new DirectionalLight().set(0.7f,0.7f,0.7f,-1f,-0.7f,-0.5f));

    }

    @Override
    public void render() {

        // CMM5 support
        if(Dev.cmm5) {
            if(Time.now() % 3 == 0) org.py.cmm5.Main.process();
            Polyware.process();
        }


        ScreenUtils.clear(255f,255f,255f,1f,true);

        if(!previousBotDiscovered && botDiscovered) { // Bot just got discovered
            splashOff();
        } else if(previousBotDiscovered && !botDiscovered) { // Bot just got lost
            splashOn();
        }
        previousBotDiscovered = botDiscovered;

        UI.mousePosition = mousePosition;
        UI.mouseDown = leftClick;
        UI.tick(); // Ticked before everything because of getRenderOffset(), but rendered on top of everything

        // FOV
        FOV += (targetFOV - FOV) / 5f;
        FOV = Math.round(FOV*100f)/100f;
        if(previousFOV != FOV) setUpCamera(FOV);
        previousFOV = FOV;

        // Subject value process
        subject.add(
            (targetSubject.x - subject.x) / 10f,
            (targetSubject.y - subject.y) / 10f,
            (targetSubject.z - subject.z) / 10f
        );

        // Scale value process
        if(Math.round(scale*100) != Math.round(targetScale*100)) {
            scale += (targetScale - scale) / zoomingEasingInteger; // TC
        } else {
            scale = targetScale;
        }

        // Rotation value process
        if(Math.round(yRot*100) != Math.round(targetYRot*100)) { // TC
            if(targetYRot - yRot > 180) targetYRot -= 360;
            if(yRot - targetYRot > 180) targetYRot += 360;
            yRot += (targetYRot - yRot) / rotationEasingInteger;
        } else {
            yRot = targetYRot % 360; // -360 - 360
        }
        if(Math.round(xRot*100) != Math.round(targetXRot*100)) { // TC
            if(targetXRot - xRot > 180) targetXRot -= 360;
            if(xRot - targetXRot > 180) targetXRot += 360;
            xRot += (targetXRot - xRot) / rotationEasingInteger;
        } else {
            xRot = targetXRot % 360; // -360 - 360
        }

        // Up value process (fml)
        if(Math.round(targetUp.x*100) != Math.round(up.x*100)) up.x += (targetUp.x - up.x) / 10f; else up.x = targetUp.x;
        if(Math.round(targetUp.y*100) != Math.round(up.y*100)) up.y += (targetUp.y - up.y) / 10f; else up.y = targetUp.y;
        if(Math.round(targetUp.z*100) != Math.round(up.z*100)) up.z += (targetUp.z - up.z) / 10f; else up.z = targetUp.z;

        // Camera Update
        camera.position.set(
            subject.x+(((float)Math.sin(-yRot*Math.PI/180f)*((float)Math.cos(xRot*Math.PI/180f)))*25f)*(1/scale),
            subject.y+((float)Math.sin(xRot*Math.PI/180f)*25f)*(1/scale),
            subject.z+(((float)Math.cos(-yRot*Math.PI/180f)*((float)Math.cos(xRot*Math.PI/180f)))*25f)*(1/scale)
        );
        camera.direction.set(
            -(float)Math.sin(-yRot*Math.PI/180f)*(float)Math.cos(xRot*Math.PI/180f),
            -(float)Math.sin(xRot*Math.PI/180f),
            -(float)Math.cos(-yRot*Math.PI/180f)*(float)Math.cos(xRot*Math.PI/180f)
        );
        camera.up.set(
            up.x,
            up.y,
            up.z
        );
        camera.update();

        modelBatch.begin(camera);
        modelBatch.render(instance,environment);
        modelBatch.end();

        UI.render(); // Rendered on top of everything, but ticked before because of getRenderOffset()

    }

    @Override
    public void dispose() {

        Textures.dispose();
        Fonts.dispose();
        Graphics2D.dispose();

        modelBatch.dispose();
        model.dispose();

    }

    //  EXTRA METHODS
    // =================================================================================================================

    private void setUpCamera(float fov) {

        camera = new PerspectiveCamera(fov,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.far = 100000f;

    }

    private void splashOn() {
        ps_zoomingEasingInteger = zoomingEasingInteger;
        ps_rotationEasingInteger = rotationEasingInteger;
        zoomingEasingInteger = 20f;
        rotationEasingInteger = 20f;
        targetScale = 0.006f;
        scale = targetScale;
        targetXRot = 90f;
        targetYRot = 179f;
        UI.splashOn();
    }
    private void splashOff() {
        scheduler.schedule(() -> {
            zoomingEasingInteger = ps_zoomingEasingInteger;
            rotationEasingInteger = ps_rotationEasingInteger;
        },3,TimeUnit.SECONDS);
        targetScale = 1f;
        targetXRot = 45f;
        targetYRot = 0f;
        UI.splashOff();
    }

    //  EVENT LISTENERS - INPUT
    // =================================================================================================================

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        if(button == 0) {
            leftClick = true;
        }
        if(button == 1) {
            rightClick = true;
        }
        if(button == 2) {
            middleButton = true;
        }

        return false;

    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {

        if(UI.splash) return false;

        // Non-UI controls
        if(middleButton && userManipulate) {

            Gdx.input.setCursorCatched(true);
            float X = (x - previousMousePosition.x) * dragging_sensitivity;
            float Y = (y - previousMousePosition.y) * dragging_sensitivity;

            targetSubject.add(
                -X * (1/scale),
                0f,
                -Y * (1/scale)
            );

            // No easing
            subject.set(
                targetSubject.x,
                targetSubject.y,
                targetSubject.z
            );

        }
        if(rightClick && userRotate) {

            Gdx.input.setCursorCatched(true);
            float X = ( (float) x - previousMousePosition.x ) / Gdx.graphics.getWidth() * 360f; // Mouse move distance X
            float Y = ( (float) y - previousMousePosition.y ) / Gdx.graphics.getHeight() * 360f; // Mouse move distance Y

            // These values get warped to 360 after they ease to target
            xRot += Y;
            yRot += X;
            if(xRot > 90f) xRot = 90f;
            if(xRot < -90f) xRot = -90f;

            // No easing (AKA instant easing)
            targetXRot = xRot;
            targetYRot = yRot;

        }
        mousePosition = new Point(x,y);
        previousMousePosition = new Point(x,y);
        return false;

    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {

        if(button == 0) {
            leftClick = false;
        }
        if(button == 1) {
            rightClick = false;
            if(!UI.splash) Gdx.input.setCursorCatched(false);
        }
        if(button == 2) {
            middleButton = false;
            if(!UI.splash) Gdx.input.setCursorCatched(false);
        }
        return false;

    }

    @Override
    public boolean mouseMoved(int x, int y) { // Doesn't call when it's dragged

        mousePosition = new Point(x,y);
        previousMousePosition = new Point(x,y); // Relies on this being called before touchDragged() because then it isn't null
        return false;

    }

    @Override
    public boolean scrolled(float x, float y) {

        if(UI.splash) return false;

        // Non-UI controls
        targetScale *= (float) (1/Math.pow(2,y*zoomingSensitivity)); // 1/x to flip controls
        targetScale = Math.min(12f,Math.max(0.02f,targetScale));

        return false;

    }

    private boolean ctrlDown = false;
    private boolean shiftDown = false;
    private boolean altDown = false;

    @Override
    public boolean keyDown(int code) {

        if(code == 129) ctrlDown = true;
        if(code == 59) shiftDown = true;
        if(code == 57) altDown = true;

        if(code == 66) { // Enter
            if(UI.promptExists()) {
                UI.currentPrompt.onPlus.run();
                UI.clearPrompt();
            }
        }
        if(code == 111) { // Esc
            UI.clearPrompt();
        }
        if(ctrlDown && shiftDown && altDown && code == 40) { // L
            Dev.restart = true; // Restart after exit
            UI.clearPrompt();
            Log.closeWindow();
            Dev.dev = false;
            org.py.cmm5.Polyware.exit();
            Gdx.app.exit();
        }

        if(!UI.splash && code == 46) { // R

            birdsEye = !birdsEye;

            if(birdsEye) {

                pbe_targetScale = targetScale;
                pbe_targetXRot = targetXRot;
                pbe_targetYRot = targetYRot;
                pbe_targetFOV = targetFOV;

                userRotate = false;
                userManipulate = true;
                targetXRot = 90f; // Looking down
                targetYRot = 0f; // Looking forward
                targetSubject.set(Vector3.Zero);
                targetScale = 0.06f;
                targetFOV = 30;

                targetUp.set(0,0,-0.5f); // Reaches it at the end of easing (It rounds it)

            } else {

                targetScale = pbe_targetScale;
                targetXRot = pbe_targetXRot;
                targetYRot = pbe_targetYRot;
                targetFOV = pbe_targetFOV;

                userRotate = true;
                userManipulate = false;
                targetSubject.set(Vector3.Zero);

                targetUp.set(0,0.5f,0); // Reaches it at the end of easing (It rounds it)

            }
        }

        if(ctrlDown && shiftDown) {

            if(code == 47) { // S

                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(Settings.file());
                    } catch(IOException e) {
                        Error.make("Cannot find settings file :(");
                    }
                } else {
                    Error.make("Desktop not supported. Are you on Windows?");
                }

            } else if(code == 32) { // D

                if(Dev.dev) {
                    UI.prompt(Textures.warn,"Disable developer mode?","YES","BACK",
                        () -> {
                            Dev.dev = false;
                            Gdx.graphics.setTitle(Settings.getString("program_name"));
                            Log.closeWindow();
                        },
                        () -> {}
                    );
                } else {
                    UI.prompt(Textures.warn,"Enter developer mode?","YES","BACK",
                        () -> {
                            Dev.dev = true;
                            Gdx.graphics.setTitle(Settings.getString("program_name") + " - Developer Mode");
                        },
                        () -> {}
                    );
                }

            }

        }

        if(Dev.dev) {
            switch(code) {
                case 8: // 1
                    Log.window();
                    break;
            }

        }

        return false;

    }

    @Override
    public boolean keyUp(int code) {

        if(code == 129) ctrlDown = false;
        if(code == 59) shiftDown = false;
        if(code == 57) altDown = false;

        return false;

    }


    @Override public boolean keyTyped(char ch) { return false; }
    @Override public boolean touchCancelled(int x, int y, int pointer, int button) { return false; }

    @Override
    public void resize(int width, int height) {
        Graphics2D.resize(width,height);
    }


}





//                                                                                                        ******
//                                                                                                         *****
//                                                                                                          *****
//                                                                                                           *****
//                                                                                                            ******
//                                                                                                             ******
//                                                                                                               *****
//                                                                                                                *****
//                                                                                                                 *****
//                                                                                                                  ******          ******
//    @@@@@@@@@@@@@@@@  @@@@@   @@@@@@@@@@@@@@@@   @@@@@@@@@@@@@@@@        @@@@@@@@@@        @@@@@@@@@@@@            ******        ******
//    @@@@@@@@@@@@@@@@  @@@@@   @@@@@@@@@@@@@@@@   @@@@@@@@@@@@@@@@      @@@@@@@@@@@@@@      @@@@@@@@@@@@@@@           *****      *****
//    @@@@       @@@@@  @@@@@   @@@@@              @@@@@               @@@@@@      @@@@@@    @@@@@    @@@@@@@           *****    *****
//    @@@@      @@@@@   @@@@@   @@@@@              @@@@@               @@@@@        @@@@@@   @@@@@       @@@@@           ************
//    @@@@     @@@@@    @@@@@   @@@@@              @@@@@@@@@@@@@@     @@@@@          @@@@@   @@@@@        @@@@@           **********
//    @@@@ @@@@@@@@     @@@@@   @@@@@              @@@@@@@@@@@@@@     @@@@@          @@@@@   @@@@@        @@@@@            *******
//    @@@@@@@@@@@       @@@@@   @@@@@              @@@@@              @@@@@          @@@@@   @@@@@        @@@@@             ******
//    @@@@@@@@          @@@@@   @@@@@       @@@@   @@@@@              @@@@@          @@@@@   @@@@@        @@@@@           *********
//    @@@@              @@@@@    @@@@@      @@@@    @@@@@              @@@@@        @@@@@@   @@@@@        @@@@@          ************
//    @@@@              @@@@@    @@@@@@@@   @@@@     @@@@@@@           @@@@@@      @@@@@@    @@@@@        @@@@@         ******  ******
//    @@@@              @@@@@      @@@@@@@@@@@@@      @@@@@@@@@@@@@      @@@@@@@@@@@@@@      @@@@@        @@@@@        *****     ******
//    @@@@              @@@@@        @@@@@@@@@@@         @@@@@@@@@@        @@@@@@@@@@        @@@@@        @@@@@      ******        *****
//                                                                                                                  ******          *****
//    Made with blood, sweat, tears, and LibGDX                                                                    ******            ******
//                                                                                                                *****               ******
//                                                                                                               *****                 ******
//                                                                                                             ******                    *****
//                                                                                                            ******                      *****
//                                                                                                           ******
//                                                                                                          *****
