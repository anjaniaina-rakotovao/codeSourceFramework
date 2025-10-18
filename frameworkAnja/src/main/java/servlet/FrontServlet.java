package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        service(request, response);
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        String resourcePath = requestURI.substring(contextPath.length());
        
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
        
        showFrameworkPage(request, response, resourcePath);
    }
    
    private void showFrameworkPage(HttpServletRequest request, HttpServletResponse response, 
                                 String requestedPath) 
            throws IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
       out.println("<!DOCTYPE html>");
        out.println("<html lang='fr'>");
        out.println("<head>");
        out.println("     <meta charset='UTF-8'>");
        out.println("     <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("     <title>Bienvenue dans le Framework d'Anja!</title>");
        out.println("     <style>");
        out.println("         body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background: linear-gradient(135deg, #FFD700 0%, #FF8C00 100%); color: #333; min-height: 100vh; display: flex; align-items: center; justify-content: center; }");
        out.println("         .container { max-width: 700px; width: 90%; background: #ffffff; padding: 40px; border-radius: 20px; box-shadow: 0 15px 40px rgba(0,0,0,0.2); border: 2px solid #FF8C00; text-align: center; }");
        out.println("         h1 { color: #FF4500; font-size: 3em; margin-bottom: 10px; font-weight: 700; text-shadow: 1px 1px 1px rgba(0,0,0,0.1); }");
        out.println("         .welcome-message { color: #555; font-size: 1.2em; margin-bottom: 30px; }");
        out.println("         .message { background: #fffaf0; padding: 30px; border-radius: 15px; border: 1px solid #FFD700; margin: 25px 0; box-shadow: 0 5px 15px rgba(0,0,0,0.1); }");
        out.println("         h3 { color: #FF4500; margin-bottom: 15px; font-weight: 600; font-size: 1.5em; }");
        out.println("         p { line-height: 1.6; margin-bottom: 15px; color: #666; font-weight: 500; }");
        out.println("         .path-label { font-size: 1.1em; color: #333; margin-top: 20px; }");
        out.println("         .path { font-family: 'Courier New', monospace; background: #FF8C00; color: white; padding: 15px 25px; border-radius: 10px; display: inline-block; margin: 10px 0; font-weight: bold; font-size: 1.1em; word-wrap: break-word; }");
        out.println("     </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("     <div class='container'>");
        out.println("         <h1>🎉 Framework Java d'Anja 🎉</h1>");
        out.println("         <p class='welcome-message'>Coucou, c'est Anja.</p>");
        
        out.println("         <div class='message'>");
        out.println("             <p>Oups! Route pas encore gérée!</p>");
        out.println("             <p class='path-label'>Voici l'URL que vous avez demandée :</p>");
        out.println("             <div class='path'><strong>" + requestedPath + "</strong></div>");
        out.println("         </div>");
        out.println("     </div>");
        out.println("</body>");
        out.println("</html>");
    }
}
