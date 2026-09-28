package org.py.cmm5;

import java.util.ArrayList;

public class Usage {

    public Usage() {

        // Handled by ControlsManager Controller Selection Interface, disregard this
        Controller masterController = new Controller();
        Controller controller2 = new Controller();
        Controller controller3 = new Controller();

        // ========================= MOST IMPORTANT PART! SET THE CONTROLLER(S)! =========================

            // Allows you to set controllers as one
            Controls.setController(masterController);
            // Allows you to set controllers as several
            ArrayList<Controller> controllers = new ArrayList<>();
            controllers.add(masterController);
            controllers.add(controller2);
            Controls.setControllers(controllers);
            // Allows you to add controllers later
            Controls.addController(controller3);

        // ========================= LOGGERS =========================

        // Joystick output is quite messy, so suppressJoystickOutput disables any joystick output
        Controls.inputLogger(true,true); // On for all controllers
        Controls.inputLogger(true,true,1); // On for just controller 1
        Controls.inputLogger(false,false,0); // When turning off, other values don't matter, because it won't be on!

        Controls.controlsLogger(true); // On
        Controls.controlsLogger(false); // Off

        Controls.errorLogger(true); // On
        Controls.errorLogger(false); // Off

        // =========================  CONTROLS =========================

        // You can add controls and any values you don't include will default to their default values
        Controls.addControl("jump",Controls.BinaryComponents.A); // Defaults to "ACTIVE"
        Controls.addControl("releaseJump",Controls.BinaryComponents.A,"INACTIVE");
        Controls.addControl("shoot",Controls.ThresholdComponents.RT,"GREATER_THAN:0",new int[] { 1, 3 }); // Only for controllers 1 and 3
        Controls.addControl("shoot",Controls.ThresholdComponents.RT); // Defaults to "GREATER_THAN:0"
        Controls.addControl("moveForward",Controls.JoystickComponents.A,"north:0.5", new int[] { 2 }); // Only for controller 2

        // You can link controls and both of them will have to be true for the control to resolve
        Controls.addControl("cheatcode1",Controls.BinaryComponents.SA,new int[] { 1 })
                .linkControl(Controls.ThresholdComponents.AX,"GREATER_THAN:0.5");

        // ========================= FUNCTIONS =========================

        // Must have identical name to the control made
        Controls.bindFunctionToControl("jump",true,false,msa -> {
            // When I coded this, I knew what MSA meant. Now I don't. MSA is pointless, and only exists so I can have the Consumer object.
            System.out.println("Jumped!");
        });

        // You can do these inside loops
        if(Controls.conditionResolve("jump")) {
            System.out.println("Currently holding jump!");
        }

    }

}
