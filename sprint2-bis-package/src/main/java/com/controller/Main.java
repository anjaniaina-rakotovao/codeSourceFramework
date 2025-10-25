package com.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        // Package à scanner
        String packageName = "com.controller";
        List<Class<?>> classes = getClasses(packageName);

        // Vérifier les annotations
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                System.out.println(clazz.getName());
            }
        }
    }

    public static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource == null) return classes;

        File folder = new File(resource.toURI());
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
}
