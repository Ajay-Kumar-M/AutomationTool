<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="error.jsp" %>
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
            <h1>🤖 AutomationTool</h1>
            <p class="subtitle">Enterprise Test Automation Platform</p>
            <span class="status-badge">✓ Running</span>
        </header>

        <div class="info-section">
            <h2>Welcome to AutomationTool</h2>
            <form action="run" method="get">
                Enter folder name: <input typ="text" name="folderName"></br>
                <input type="Submit">
            </form>
            </br>
            <form action="run" method="post">
                 Enter folder name: <input typ="text" name="folderName"></br>
                 <input type="Submit">
            </form>
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
    </div>
</body>
</html>
