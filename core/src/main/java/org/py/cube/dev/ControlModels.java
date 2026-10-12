package org.py.cube.dev;

import org.py.cmm5.Controller;
import org.py.cmm5.Controls;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControlModels {

    // bs = Bot state

    public static Consumer<Integer> bsSet;
    public static Supplier<Integer> bsGet;

    public static void testBot(Consumer<Integer> bsSet, Supplier<Integer> bsGet) {

        ControlModels.bsSet = bsSet;
        ControlModels.bsGet = bsGet;

        Controls.addControl("elevator-up",Controls.BinaryComponents.B,"ACTIVE")
            .bindFunction(e -> bsSet.accept(bsGet.get()+1), true,false);
        Controls.addControl("elevator-down",Controls.BinaryComponents.B,"INACTIVE")
            .bindFunction(e -> bsSet.accept(bsGet.get()-1), true,false);
        Controls.addControl("test", Controls.BinaryComponents.A)
            .bindFunction(e -> System.out.println(1),true,false);

        Controls.addControl("move",Controls.JoystickComponents.A," TODOnew int[] {0})
            .bindFunction(e -> {
                Controls.getJoystickAngle(Controls.JoystickComponents.A,0);
            );

    }

}
