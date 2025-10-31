package methods;

import java.io.File;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import annotation.AnnotationController;
import annotation.AnnotationUrl;

public class ScannerPackage {
    
    public static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (resource.getProtocol().equals("file")) {
                // Classes dans un dossier
                File folder = new File(resource.toURI());
                for (File file : folder.listFiles()) {
                    if (file.getName().endsWith(".class")) {
                        String className = packageName + "." + file.getName().replace(".class", "");
                        classes.add(Class.forName(className));
                    }
                }

            } else if (resource.getProtocol().equals("jar")) {
                // Classes dans un jar
                JarURLConnection conn = (JarURLConnection) resource.openConnection();
                JarFile jarFile = conn.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                        String className = name.replace('/', '.').replace(".class", "");
                        classes.add(Class.forName(className));
                    }
                }
            }
        }
        return classes;
    }

    public static List<Class<?>> getAnnotatedClasses(String packageName) throws Exception {
        List<Class<?>> annotated = new ArrayList<>();
        for (Class<?> clazz : getClasses(packageName)) {
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                annotated.add(clazz);
            }
        }
        return annotated;
    }

    public static Map<String, Method> getUrlMethodMap(String packageName) throws Exception {
        Map<String, Method> urlMap = new HashMap<>();
        List<Class<?>> controllers = getAnnotatedClasses(packageName);

        for (Class<?> clazz : controllers) {
            AnnotationController controllerAnnotation = clazz.getAnnotation(AnnotationController.class);
            String baseUrl = controllerAnnotation.annotationName();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AnnotationUrl.class)) {
                    AnnotationUrl urlAnnotation = method.getAnnotation(AnnotationUrl.class);
                    String fullUrl = baseUrl + urlAnnotation.url();
                    urlMap.put(fullUrl, method);
                }
            }
        }

        return urlMap;
    }
}
