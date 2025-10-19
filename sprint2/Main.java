import java.lang.reflect.*;

public class Main {
    public static void main(String[] args) {
        Class<ListAnnotation> liste = ListAnnotation.class;
        for (Method methode : liste.getDeclaredMethods()) {
            if(methode.isAnnotationPresent(AnnotationUrl.class)){
                AnnotationUrl annotation = methode.getAnnotation(AnnotationUrl.class);
                System.out.println(annotation.url());
            }
        }
    }
}
