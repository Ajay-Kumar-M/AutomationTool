<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
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
           background: red;
           min-height: 100vh;
           display: flex;
           justify-content: center;
           align-items: center;
           padding: 20px;
       }

    </style>
</head>
<body bgcolor="red">
    <div class="container">
        <header>
            <h1>🤖 AutomationTool Error</h1>
            <p class="subtitle">Enterprise Test Automation Platform</p>
            <span class="status-badge">✓ Running</span>
        </header>

        <div class="info-section">
            <h2>Exception occurred</h2>
            <%=
               exception.getMessage()
            %>
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
