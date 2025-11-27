package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import annotation.RequestParam;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import methods.ModelVue;
import methods.ScannerPackage;

@WebServlet(name = "FrontServlet", urlPatterns = { "/" }, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {

    private Map<String, Map<String ,List<Method>>> urlMethodMap;
    private List<String> dynamicPatterns = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try {
            ClassLoader webAppClassLoader = getServletContext().getClassLoader();
            String basePackage = "controller";
            urlMethodMap = ScannerPackage.getUrlMethodMap(basePackage, webAppClassLoader);

            dynamicPatterns = new ArrayList<>(ScannerPackage.dynamicUrlMap.keySet());
            System.out.println("Routes détectées au démarrage");
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());

        try {
            for (Map.Entry<String, Method> e : ScannerPackage.dynamicUrlMap.entrySet()) {
                String key = e.getKey();
                String regex;

                if (key.contains("{")) {
                    regex = "^" + key.replaceAll("\\{[^/]+\\}", "([^/]+)") + "$";
                } else {
                    if (key.startsWith("^") || key.endsWith("$")) {
                        regex = key;
                    } else {
                        regex = "^" + key + "$";
                    }
                }

                if (resourcePath.matches(regex)) {
                    handleMethod(e.getValue(), request, response);
                    return;
                }
            }

            String httpMethod = request.getMethod().toLowerCase();


            Map<String, List<Method>> httpMap = urlMethodMap.get(resourcePath);

            if (httpMap != null) {
                List<Method> methods = httpMap.get(httpMethod);

                if (methods != null) {
                Method methodToCall = null;

                    for (Method m : methods) {
                        Parameter[] params = m.getParameters();
                        boolean match = true;

                        for (Parameter p : params) {
                            if (request.getParameter(p.getName()) == null) {
                            match = false;
                            break;
                            }
                        }
                        if (match || params.length == 0) {
                            methodToCall = m;
                            break;
                        }
                    }

                    if (methodToCall != null) {
                        handleMethod(methodToCall, request, response);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            throw new ServletException("Erreur lors du matching des routes pour: " + resourcePath, ex);
        }


        try {
            java.net.URL resource = getServletContext().getResource(resourcePath);
            if (resource != null) {
                RequestDispatcher defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) {
                    defaultServlet.forward(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de la vérification de la ressource: " + resourcePath, e);
        }

        showFrameworkPage(response, resourcePath);
    }

    public void handleMethod(Method method, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            Parameter[] params = method.getParameters();
            Object[] args = new Object[params.length];

            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();
            String resourcePath = requestURI.substring(contextPath.length());

            for (int i = 0; i < params.length; i++) {
                Parameter p = params[i];
                RequestParam rp = p.getAnnotation(RequestParam.class);
                String paramName = (rp != null) ? rp.value() : p.getName();
                String value = request.getParameter(paramName);

                    if (value == null) {
                        for (Map.Entry<String, Method> entry : ScannerPackage.dynamicUrlMap.entrySet()) {
                        String key = entry.getKey();
                        if (!key.contains("{")) continue; 

                        String regex = "^" + key.replaceAll("\\{[^/]+\\}", "([^/]+)") + "$";
                        if (resourcePath.matches(regex)) {
                        
                            String[] patternParts = key.split("/");
                            String[] urlParts = resourcePath.split("/");
                            for (int j = 0; j < patternParts.length; j++) {
                                if (patternParts[j].equals("{" + paramName + "}")) {
                                    value = urlParts[j];
                                    break;
                                }
                            }
                        }
                    }
                }

                if (value != null) {
                    if (p.getType().equals(Integer.class) || p.getType().equals(int.class)) {
                        args[i] = Integer.parseInt(value);
                    } else if (p.getType().equals(Double.class) || p.getType().equals(double.class)) {
                        args[i] = Double.parseDouble(value);
                    } else {
                        args[i] = value;
                    }
                } else {
                    args[i] = null;
                }
            }

            Object result = method.invoke(instance, args);

            if (result instanceof String) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println((String) result);
                return;
            } else if (result instanceof ModelVue) {
                ModelVue mv = (ModelVue) result;
                String url = "/" + mv.getVue() + ".jsp";
                RequestDispatcher rd = request.getRequestDispatcher(url);
                rd.forward(request, response);
                return;
            } else if (result != null) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();

                Class<?> clazz = result.getClass();
                out.println("<h2>Objet retourné : " + clazz.getSimpleName() + "</h2>");
                out.println("<ul>");

                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object value = f.get(result);
                    out.println("<li><strong>" + f.getName() + "</strong> = " + value + "</li>");
                }

                out.println("</ul>");
                return;
            }

        } catch (Exception e) {
            throw new ServletException("Erreur execution de méthode", e);
        }
    }

    private void showFrameworkPage(HttpServletResponse response, String requestedPath)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='fr'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'/>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'/>");
        out.println("<title>Bienvenue dans le Framework d'Anja!</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 0; padding: 0; " +
                "background: linear-gradient(135deg, #FFD700 0%, #FF8C00 100%); color: #333;" +
                "min-height: 100vh; display: flex; align-items: center; justify-content: center; }");
        out.println(".container { max-width: 700px; width: 90%; background: #ffffff; padding: 40px;" +
                "border-radius: 20px; box-shadow: 0 15px 40px rgba(0,0,0,0.2); border: 2px solid #FF8C00;" +
                "text-align: center; }");
        out.println("h1 { color: #FF4500; font-size: 3em; margin-bottom: 10px; font-weight: 700; }");
        out.println(".message { background: #fffaf0; padding: 30px; border-radius: 15px;" +
                "border: 1px solid #FFD700; margin: 25px 0; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }");
        out.println(".path { font-family: 'Courier New', monospace; background: #FF8C00; color: white;" +
                "padding: 15px 25px; border-radius: 10px; display: inline-block; margin: 10px 0;" +
                "font-weight: bold; font-size: 1.1em; word-wrap: break-word; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>🎉 Framework Java d'Anja 🎉</h1>");
        out.println("<div class='message'>");
        out.println("<p>Oups! Route pas encore gérée!</p>");
        out.println("<p>URL demandée :</p>");
        out.println("<div class='path'><strong>" + requestedPath + "</strong></div>");
        out.println("</div>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
