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

        .container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 60px 40px;
            max-width: 600px;
            width: 100%;
        }

        header {
            text-align: center;
            margin-bottom: 40px;
        }

        h1 {
            color: #333;
            font-size: 2.5em;
            margin-bottom: 10px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .subtitle {
            color: #666;
            font-size: 1.1em;
            margin-bottom: 30px;
        }

        .info-section {
            background: #f8f9fa;
            border-left: 4px solid #667eea;
            padding: 20px;
            margin: 20px 0;
            border-radius: 6px;
        }

        .info-section h2 {
            color: #333;
            font-size: 1.3em;
            margin-bottom: 10px;
        }

        .info-section p {
            color: #666;
            line-height: 1.6;
            margin-bottom: 8px;
        }

        .status-badge {
            display: inline-block;
            background: #10b981;
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 0.9em;
            margin-top: 10px;
        }

        .server-info {
            background: #f0f4ff;
            border: 1px solid #667eea;
            padding: 15px;
            border-radius: 6px;
            margin: 20px 0;
            font-family: 'Courier New', monospace;
            font-size: 0.95em;
        }

        .server-info p {
            margin: 8px 0;
            color: #333;
        }

        .server-info strong {
            color: #667eea;
        }

        .button-group {
            display: flex;
            gap: 15px;
            margin-top: 30px;
            flex-wrap: wrap;
            justify-content: center;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 6px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s ease;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
        }

        .btn-secondary {
            background: #e5e7eb;
            color: #333;
            border: 2px solid #d1d5db;
        }

        .btn-secondary:hover {
            background: #d1d5db;
            border-color: #9ca3af;
        }

        .features {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
            margin-top: 30px;
        }

        .feature-card {
            background: #f9fafb;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
            border: 1px solid #e5e7eb;
        }

        .feature-icon {
            font-size: 2em;
            margin-bottom: 10px;
        }

        .feature-card h3 {
            color: #333;
            font-size: 1em;
            margin-bottom: 5px;
        }

        .feature-card p {
            color: #666;
            font-size: 0.9em;
        }

        footer {
            text-align: center;
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
            color: #999;
            font-size: 0.9em;
        }

        @media (max-width: 600px) {
            .container {
                padding: 40px 20px;
            }

            h1 {
                font-size: 2em;
            }

            .features {
                grid-template-columns: 1fr;
            }

            .button-group {
                flex-direction: column;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }
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
            <form action="add.jsp" method="get">
                Enter folder name: <input typ="text" name="folderName"></br>
                <input type="Submit">
            </form>
            </br>
            <form action="run" method="post">
                 Enter folder name: <input typ="text" name="folderName"></br>
                 Enter folder ID  : <input typ="text" name="folderID"></br>
                 <input type="Submit">
            </form>
            </br>
        </div>

        <div class="info-section">
            <h2>Welcome to AutomationTool</h2>
            <p>
                AutomationTool is a comprehensive test automation framework built with Java, 
                Selenium, and Playwright. It provides enterprise-grade solutions for web UI 
                automation, cross-browser testing, and mobile application testing.
            </p>
        </div>

        <div class="server-info">
            <p><strong>Server Time:</strong> <%= new java.util.Date() %></p>
            <p><strong>Server Name:</strong> <%= request.getServerName() %></p>
            <p><strong>Server Port:</strong> <%= request.getServerPort() %></p>
            <p><strong>Application Context:</strong> <%= request.getContextPath() %></p>
            <p><strong>Java Version:</strong> <%= System.getProperty("java.version") %></p>
        </div>

        <div class="features">
            <div class="feature-card">
                <div class="feature-icon">🌐</div>
                <h3>Web Automation</h3>
                <p>Selenium & Playwright</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">📱</div>
                <h3>Mobile Testing</h3>
                <p>Android & iOS</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">🔐</div>
                <h3>Security Testing</h3>
                <p>Encryption & Protocols</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon">📊</div>
                <h3>Test Reports</h3>
                <p>Detailed Analytics</p>
            </div>
        </div>

        <div class="button-group">
            <a href="about.jsp" class="btn btn-primary">📚 Learn More</a>
            <a href="test-runner.jsp" class="btn btn-primary">▶️ Test Runner</a>
            <a href="https://github.com" target="_blank" class="btn btn-secondary">🔗 GitHub</a>
        </div>

        <div class="info-section" style="margin-top: 30px;">
            <h2>Quick Start</h2>
            <p><strong>1.</strong> Create your test scripts</p>
            <p><strong>2.</strong> Configure test environment</p>
            <p><strong>3.</strong> Run tests via Test Runner</p>
            <p><strong>4.</strong> View results and reports</p>
        </div>

        <footer>
            <p>&copy; 2026 AutomationTool. Built with Java, Servlets & JSP.</p>
            <p>Deployed on <%= application.getServerInfo() %></p>
        </footer>
    </div>
</body>
</html>
