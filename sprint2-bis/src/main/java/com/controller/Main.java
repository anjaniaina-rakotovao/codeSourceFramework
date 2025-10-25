package com.controller;

import java.util.Set;

import org.reflections.Reflections;

public class Main {
    public static void main(String[] args) {
        Reflections reflections = new Reflections("com.controller");

        Set<Class<?>> classesAnnotees = reflections.getTypesAnnotatedWith(AnnotationController.class);

        for (Class<?> clazz : classesAnnotees) {
            System.out.println(clazz.getName());
        }
    }
}
