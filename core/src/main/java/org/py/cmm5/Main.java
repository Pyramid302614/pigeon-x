//
//        __|              |             |        \  |
//       (      _ \    \    _|   _| _ \  | (_-<  |\/ |   _` |    \    _` |   _` |   -_)   _|
//      \___| \___/ _| _| \__| _| \___/ _| ___/ _|  _| \__,_| _| _| \__,_| \__, | \___| _|
//                                                                         ____/
//      Testing Environment - CM-M4

package org.py.cmm5;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.py.cube.Log;
import org.py.cube.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static final String configPath = System.getenv("APPDATA") + "\\.cube\\cmm5-config.txt";

    public static HashMap<String,String> config = txtSon.getData(configPath);

    private static boolean wpiMode = config.get("wpi-mode").equals("true");

    private static final boolean startToSelect = config.get("selection--mode").equals("start-to-select"); // Autoselects if joystick Start button (Special Button B) is pressed
    private static final boolean nameToSelect = config.get("selection--mode").equals("name-to-select"); // Autoselects joystick if it contains "Controller" in the name (Recommended only if one controller is being used)
    private static boolean polyware = config.get("selection--use-polyware").equals("true");

    // (!) Start-to-select and Name-to-select is not compatible with WPIMode.


    public static ArrayList<Integer> selectedJIDs = new ArrayList<>(); // GLFW Mode
    public static ArrayList<XboxControllerAdapter> xboxControllerAdapters = new ArrayList<>(); // WPI Mode

    public static void init() {

        Log.log("====== CONTROLS MANAGER MODEL 5 ======");
        Log.log("[Modified client for : pigeonx]");
        Log.log("Starting...");

        ArrayList<Controller> controllers = new ArrayList<>();

        if(!GLFW.glfwInit()) {
            Log.log("Failed to initialize GLFW. Falling back to WPI mode.");
            wpiMode = true;
            polyware = false;
        }

        if(polyware) {

            String mode = config.get("selection--mode");
            if(mode == null && Controls.errorLoggerOn) Log.log("[ControlsManager:ControllerSelection] Mode not defined in config. Please define: \"selection--mode\"");
            else if(mode != null) Polyware.controllerSelection(mode);

        } else {

            boolean manual = !(nameToSelect || startToSelect);

            Log.log("============ ControlsManager Model " + Controls.model + " || Controller Selection ============");

            if(!wpiMode) {

                Log.log("\nGLFW initialized.");

                Log.log("\nJID | NAME             Detected Controllers\n--------------------------------------------");
                short detected = 0;
                for(int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_16; jid++) {
                    if(GLFW.glfwJoystickPresent(jid)) {
                        GLFWGamepadState s = GLFWGamepadState.create();
                        GLFW.glfwGetGamepadState(jid,s);
                        Log.log(" " + jid + "  |  " + GLFW.glfwGetJoystickName(jid) + (s.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == 1? " (A is pressed)":""));
                        detected++;
                    }
                }

                if(detected == 0) Log.log("<No controllers detected>");

                if(nameToSelect) {
                    Log.log("\nEntering name-to-select mode.");
                    for(int i = 0; i < 15; i++) {
                        if(GLFW.glfwJoystickPresent(i)) {
                            String name = GLFW.glfwGetJoystickName(i);
                            if(name != null) if(name.toLowerCase().contains("controller")||name.toLowerCase().contains("logitech")) {
                                Log.log("| Selected controller with JID " + i);
                                selectedJIDs.add(i);
                            }
                        }
                    }
                    Log.log("Finished selection.");
                }
                else if(startToSelect) {
                    boolean done = false;
                    Log.log("\nEntering start-to-select mode. Press the START button on your controller to select it. The master controller determines when selection is over.");
                    while(!done) {
                        for(int i = 0; i < 15; i++) {
                            GLFWGamepadState s = GLFWGamepadState.create();
                            GLFW.glfwGetGamepadState(i,s);
                            if(!selectedJIDs.contains(i) && s.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START) == 1) {
                                selectedJIDs.add(i);
                                if(selectedJIDs.size() == 1) Log.log("| Master Controller selected. (JID:" + i + ")\n| | [HOLD BACK ON MASTER CONTROLLER TO FINISH SELECTION, HOLD A TO SELECT ADDITIONAL CONTROLLERS MANUALLY]");
                                else Log.log("| Controller " + selectedJIDs.size() + " selected. (JID:" + i + ")");
                            } else if(s.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK) == 1 && !selectedJIDs.isEmpty() && selectedJIDs.get(0) == i) {
                                done = true;
                                Log.log("Finished selection.");
                            } else if(s.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A) == 1 && !selectedJIDs.isEmpty() && selectedJIDs.get(0) == i) {
                                done = true;
                                manual = true;
                            }
                        }
                        sleep(200);
                    }
                }

                if(manual) {
                    Log.log("\nEntering manual mode. [ENTER \"DONE\" TO FINISH SELECTION]");
                    Scanner sc = new Scanner(System.in);
                    while(true) {
                        System.out.print("| [As controller " + (selectedJIDs.size()+1) + "] Enter joystick by JID: ");
                        String input = sc.nextLine();
                        if(Objects.equals(input,"done")) break;
                        try { if(!selectedJIDs.contains(Integer.parseInt(input))) {
                            selectedJIDs.add(Integer.parseInt(input));
                            if(selectedJIDs.size() == 1) Log.log("| | Master Controller selected. (JID:" + input + ")");
                            else Log.log("| | Controller " + selectedJIDs.size() + " selected. (JID:" + input + ")");
                        } else {
                            Log.log("| | (!) That JID is already selected as Controller " + selectedJIDs.indexOf(Integer.parseInt(input)));
                        } } catch(Exception ignored) {}
                    }
                    sc.close();
                    Log.log("Finished Selection.");
                }

                Log.log();

                for(int i = 0; i < selectedJIDs.size(); i++) controllers.add(new Controller());

            } else {
                if(startToSelect) Log.log("(!) Start-to-select is not compatible with WPIMode.");
                if(nameToSelect) Log.log("(!) Name-to-select is not compatible with WPIMode.");
                Log.log("WPI Mode - Add controllers by their port number. Type \"done\" to finish.");
                Scanner sc = new Scanner(System.in);
                while(true) {
                    System.out.print("| [As controller " + controllers.size() + "] Enter port number: ");
                    String input = sc.nextLine();
                    if(input.equals("done")) break;
                    try {
                        int in = Integer.parseInt(input);
                        Controller controller = new Controller();
                        boolean add = true;
                        for(XboxControllerAdapter adapter : xboxControllerAdapters) {
                            if(adapter.xboxController.port == in) {
                                Log.log("| (!) That port is already added.");
                                add = false;
                            }
                        }
                        // These two are using the same object, so they will sync, which is kinda beautiful
                        if(add) {
                            xboxControllerAdapters.add(new XboxControllerAdapter(new XboxController(in),controller));
                            controllers.add(controller);
                        }
                    } catch(Exception ignored) {}
                }
                sc.close();

            }

            Controls.setControllers(controllers);
        }
    }

    public static void process() {

        if(!wpiMode) for(int i = 0; i < Controls.controllers.size(); i++) {
            Controller controller = Controls.controllers.get(i);
            GLFWGamepadState state = GLFWGamepadState.create();
            if(selectedJIDs.size() > i && GLFW.glfwGetGamepadState(selectedJIDs.get(i),state)) {
                controller.getComponent(Controls.BinaryComponents.A).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_A);
                controller.getComponent(Controls.BinaryComponents.B).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_B);
                controller.getComponent(Controls.BinaryComponents.X).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_X);
                controller.getComponent(Controls.BinaryComponents.Y).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_Y);
                controller.getComponent(Controls.BinaryComponents.LB).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER);
                controller.getComponent(Controls.BinaryComponents.RB).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER);
                controller.getComponent(Controls.BinaryComponents.DL).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT);
                controller.getComponent(Controls.BinaryComponents.DR).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);
                controller.getComponent(Controls.BinaryComponents.DU).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP);
                controller.getComponent(Controls.BinaryComponents.DD).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN);
                controller.getComponent(Controls.BinaryComponents.JA).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB);
                controller.getComponent(Controls.BinaryComponents.JB).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB);
                controller.getComponent(Controls.BinaryComponents.SA).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_BACK);
                controller.getComponent(Controls.BinaryComponents.SB).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_START);
                controller.getComponent(Controls.BinaryComponents.BB).value = state.buttons(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE);
                controller.getComponent(Controls.ThresholdComponents.LT).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER);
                controller.getComponent(Controls.ThresholdComponents.RT).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER);
                controller.getComponent(Controls.ThresholdComponents.AX).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
                controller.getComponent(Controls.ThresholdComponents.BY).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
                controller.getComponent(Controls.ThresholdComponents.BX).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
                controller.getComponent(Controls.ThresholdComponents.AY).value = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
                controller.getComponent(Controls.JoystickComponents.A).value.x = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
                controller.getComponent(Controls.JoystickComponents.A).value.y = state.axes(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
                controller.getComponent(Controls.JoystickComponents.B).value.x = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
                controller.getComponent(Controls.JoystickComponents.B).value.y = state.axes(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
            }
        }
        else for(XboxControllerAdapter adapter : xboxControllerAdapters) {
            adapter.sync();
        }

        Controls.processAll();

    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception ignored) {}
    }

}
