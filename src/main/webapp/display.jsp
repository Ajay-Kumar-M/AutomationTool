<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AutomationTool - Home</title>
    <style>
        * {
           margin: 0;
           padding: 0;
           box-sizing: border-box;
       }

       body {
           font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
           background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
           min-height: 100vh;
           display: flex;
           justify-content: center;
           align-items: center;
           padding: 20px;
       }

    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🤖 AutomationTool Error</h1>
            <p class="subtitle">Enterprise Test Automation Platform</p>
            <span class="status-badge">✓ Running</span>
        </header>

        <div class="info-section">
            <h2>EL - Taglib</h2>

            </br>
        </div>

        <%@ page import="java.until.*" %>
        <%!
            String name = "AutomationTool";
        %>
        <%
            String folderName = request.getParameter("folderName");
            System.out.println("folder name-"+folderName);
            out.println("Automation triggered in the folder - "+folderName);
        %>
        Tool name is : <%= name %>
        </br>
        EL example:
        ${label}
        </br>
        <jstl:out value="Hello world"/>
        </br>
        <jstl:out value="${label}"/>
        </br>
        <jstl:import url="http://www.google.com"></jstl:import>
        </br>
        </br>

    </div>
</body>
</html>
