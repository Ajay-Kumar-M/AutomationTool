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
            <p class="subtitle">Error occurred</p>
        </header>

        <div class="info-section">
            <h2>Exception occurred</h2>
            <%=
               exception.getMessage()
            %>
            </br>
            <%=
               exception.printStackTrace()
            %>
        </div>
    </div>
</body>
</html>
