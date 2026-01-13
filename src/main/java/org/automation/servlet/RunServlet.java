package org.automation.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/run")
@WebInitParam(name = "AppName",value = "AutomationTool",description = "name of the application")
public class RunServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String folderName = request.getParameter("folderName");
        System.out.println("folder name-"+folderName);
        PrintWriter out = response.getWriter();
        out.println("Automation triggered in the folder - "+folderName);

    }
}

/*
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String folderName = request.getParameter("folderName");
        System.out.println("folder name-"+folderName);
        PrintWriter out = response.getWriter();
        out.println("Automation triggered in the folder - "+folderName);
    }

 */

/*
    req.setAttribute("name",value);
    RequestDispatcher rd = request.getRequestDispatcher("servlet");
        rd.forward(request,response);

 */
/*
    //url rewriting
    res.sendRedirect("servlet2?variable="+value);

 */
/*
    //HttpSession
            HttpSession session = request.getSession();
        session.setAttribute("Name",value);
        String sessionValue = session.getAttribute("Name").toString();
        session.removeAttribute("Name");
 */
/*
    //cookies
        Cookie cookie = new Cookie("Name",value);
        response.addCookie(cookie);

        Cookie[] cookies = request.getCookies();
        for(Cookie cookie1 : cookies){
            if(cookie1.getName().equals("Name")){
                String value = cookie1.getValue();
                break;
            }
        }
 */
/*
    //servlet config
        ServletConfig servletConfig = getServletConfig();
        String paramvalue = servletConfig.getInitParameter("AppName");
        System.out.println("Param value-"+paramvalue);

    //servlet context
    ServletContext servletContext = getServletContext();
        String paramvalue = servletContext.getInitParameter("AppName");
        System.out.println("Param value-"+paramvalue);
 */
/*
display.jsp
        request.setAttribute("label",folderName);
        List<DriverConfig> dcs = Arrays.asList(
                new DriverConfig("chrome1","wait","selenium",false,"url","container"),
                new DriverConfig("chrome1","wait","selenium",false,"url","container")

        );
        request.setAttribute("dcs",dcs);
        RequestDispatcher rd = request.getRequestDispatcher("display.jsp");
        rd.forward(request,response);
 */