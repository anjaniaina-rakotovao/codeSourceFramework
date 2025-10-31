package controller;
import annotation.AnnotationController;
import annotation.AnnotationUrl;

@AnnotationController(annotationName =  "/products")
public class ProductController {
    @AnnotationUrl(url = "/list")
    public void listProducts() {
        // Logic to list products
    }

    @AnnotationUrl(url = "/favorites")
    public void favoritesProducts() {
        // Logic to list favorite products
    }
}
