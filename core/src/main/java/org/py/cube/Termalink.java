package org.py.cube;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Termalink {

    private static HashMap<String,Termalink> links = new HashMap<>();

    public static Termalink get(String name) {

        return links.get(name);

    }
    public static boolean exists(String name) {

        return links.containsKey(name);

    }


    private final Supplier<String> get;
    private final Consumer<String> set;

    public Termalink(String name, Supplier<String> get, Consumer<String> set) {

        this.get = get;
        this.set = set;

        links.put(name,this);

    }

    public String get() {

        return get.get();

    }
    public static String[] list() {
        return links.keySet().toArray(new String[0]);
    }
    public void set(String v) {

        set.accept(v);

    }

}
