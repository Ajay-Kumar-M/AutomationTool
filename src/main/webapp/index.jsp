<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*,java.util.Map, java.util.concurrent.Future" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Automation Tool - Test Case Runner & Editor</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            /* Professional Light Color Palette */
            --color-primary: #f0f4f9;
            --color-secondary: #e8ecf1;
            --color-accent: #2c5aa0;
            --color-accent-light: #e8f0fc;
            --color-text-dark: #1a1a1a;
            --color-text-light: #666666;
            --color-border: #d1dce6;
            --color-success: #4caf50;
            --color-warning: #ff9800;
            --color-danger: #f44336;
            --color-hover: #f5f8fc;
            --color-white: #ffffff;
            --radius-sm: 4px;
            --radius-md: 8px;
            --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08);
            --shadow-md: 0 2px 8px rgba(0, 0, 0, 0.12);
            --shadow-lg: 0 4px 12px rgba(0, 0, 0, 0.15);
            --font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
        }

        body {
            background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
            font-family: var(--font-family);
            color: var(--color-text-dark);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1000px;
            margin: 0 auto;
            background: var(--color-white);
            border-radius: var(--radius-md);
            box-shadow: var(--shadow-md);
            padding: 40px;
        }

        .header {
            text-align: center;
            margin-bottom: 40px;
            padding-bottom: 20px;
            border-bottom: 2px solid var(--color-border);
        }

        .header h1 {
            font-size: 32px;
            color: var(--color-accent);
            font-weight: 600;
            letter-spacing: -0.5px;
        }

        .header p {
            color: var(--color-text-light);
            margin-top: 8px;
            font-size: 14px;
        }

        .section {
            margin-bottom: 30px;
        }

        .section-title {
            font-size: 14px;
            font-weight: 600;
            color: var(--color-text-dark);
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: var(--color-accent);
        }

        .input-group {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
        }

        .input-field {
            flex: 1;
            padding: 12px 16px;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            font-size: 14px;
            font-family: var(--font-family);
            background: var(--color-white);
            color: var(--color-text-dark);
            transition: all 0.2s ease;
        }

        .input-field:focus {
            outline: none;
            border-color: var(--color-accent);
            background: var(--color-accent-light);
            box-shadow: 0 0 0 3px rgba(44, 90, 160, 0.1);
        }

        .input-field::placeholder {
            color: #999;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: var(--radius-md);
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s ease;
            font-family: var(--font-family);
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .btn-primary {
            background: var(--color-accent);
            color: var(--color-white);
        }

        .btn-primary:hover {
            background: #1e3f7f;
            box-shadow: var(--shadow-md);
        }

        .btn-primary:active {
            transform: scale(0.98);
        }

        .btn-primary:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }

        .btn-secondary {
            background: var(--color-secondary);
            color: var(--color-accent);
            border: 1px solid var(--color-border);
        }

        .btn-secondary:hover {
            background: var(--color-hover);
        }

        .btn-success {
            background: var(--color-success);
            color: var(--color-white);
        }

        .btn-success:hover {
            background: #45a049;
        }

        .btn-danger {
            background: var(--color-danger);
            color: var(--color-white);
        }

        .btn-danger:hover {
            background: #da190b;
        }

        .btn-warning {
            background: var(--color-warning);
            color: var(--color-white);
        }

        .btn-warning:hover {
            background: #e68900;
        }

        /* Tree Container for main runner */
        .tree-container {
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            background: var(--color-white);
            max-height: 400px;
            overflow-y: auto;
            padding: 16px;
            margin-bottom: 20px;
        }

        .tree-item {
            margin: 4px 0;
        }

        .tree-node {
            display: flex;
            align-items: center;
            padding: 8px;
            cursor: pointer;
            user-select: none;
            border-radius: var(--radius-sm);
            transition: background 0.15s ease;
        }

        .tree-node:hover {
            background: var(--color-hover);
        }

        .tree-node input[type="checkbox"] {
            margin-right: 8px;
            width: 16px;
            height: 16px;
            cursor: pointer;
            accent-color: var(--color-accent);
        }

        .tree-toggle {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 20px;
            height: 20px;
            margin-right: 4px;
            cursor: pointer;
            color: var(--color-text-light);
            font-size: 12px;
            font-weight: bold;
        }

        .tree-toggle.collapsed::before {
            content: "▶";
        }

        .tree-toggle.expanded::before {
            content: "▼";
        }

        .tree-toggle.empty::before {
            content: "";
        }

        .tree-children {
            margin-left: 24px;
            border-left: 2px solid var(--color-border);
            padding-left: 0;
        }

        .tree-children.hidden {
            display: none;
        }

        .tree-label {
            font-size: 14px;
            color: var(--color-text-dark);
            flex: 1;
        }

        .tree-label.folder {
            color: var(--color-accent);
            font-weight: 500;
        }

        .tree-label.file {
            color: var(--color-text-light);
        }

        .tree-icon {
            margin-right: 6px;
            font-size: 12px;
        }

        /* Main sections visibility */
        .main-section {
            display: none;
        }

        .main-section.active {
            display: block;
        }

        /* Editor Table Styles */
        .editor-table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
        }

        .editor-table thead {
            background: var(--color-accent-light);
            border-bottom: 2px solid var(--color-accent);
        }

        .editor-table th {
            padding: 16px;
            text-align: left;
            font-weight: 600;
            color: var(--color-accent);
            border-right: 1px solid var(--color-border);
        }

        .editor-table th:last-child {
            border-right: none;
        }

        .editor-table td {
            padding: 12px 16px;
            border-right: 1px solid var(--color-border);
            vertical-align: top;
        }

        .editor-table td:last-child {
            border-right: none;
        }

        .editor-table tbody tr {
            border-bottom: 1px solid var(--color-border);
            transition: background 0.2s ease;
        }

        .editor-table tbody tr:hover {
            background: var(--color-hover);
        }

        .editor-table tbody tr:last-child {
            border-bottom: none;
        }

        /* Actions Column */
        .actions-column {
            max-height: 500px;
            overflow-y: auto;
            min-width: 180px;
        }

        .action-item {
            background: var(--color-secondary);
            padding: 10px 12px;
            margin: 6px 0;
            border-radius: var(--radius-sm);
            cursor: move;
            border: 2px solid transparent;
            transition: all 0.2s ease;
            user-select: none;
        }

        .action-item:hover {
            background: var(--color-accent-light);
            border-color: var(--color-accent);
            box-shadow: var(--shadow-sm);
        }

        .action-item.dragging {
            opacity: 0.6;
            transform: scale(0.95);
        }

        /* Operations Column */
        .operations-column {
            min-width: 200px;
        }

        .operations-list {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .operation-item {
            background: var(--color-accent-light);
            padding: 12px;
            border-radius: var(--radius-sm);
            border: 2px solid var(--color-accent);
            cursor: move;
            transition: all 0.2s ease;
            user-select: none;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .operation-item:hover {
            box-shadow: var(--shadow-md);
            transform: translateY(-2px);
        }

        .operation-item.drag-over {
            background: #d4e8f8;
            border-color: #1e3f7f;
        }

        .operation-item-content {
            flex: 1;
        }

        .operation-wrapper {
          display: flex;           /* Arrange children in a row */
          align-items: center;     /* Vertically center them if needed */
          gap: 0px;               /* Optional spacing between divs */
        }

        .operation-item-action {
            font-weight: 600;
            color: var(--color-accent);
            padding: 2px;
            margin: 0 10px;
        }

        .operation-item-index {
            display: inline-block;
            background: var(--color-accent);
            color: var(--color-white);
            padding: 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
        }

        .operation-item-buttons {
            display: flex;
            gap: 6px;
        }

        .operation-item-buttons button {
            padding: 6px 12px;
            font-size: 12px;
        }

        /* Userdata Column */
        .userdata-column {
            min-width: 250px;
            max-height: 500px;
            overflow-y: auto;
        }

        .userdata-form {
            background: var(--color-primary);
            padding: 16px;
            border-radius: var(--radius-md);
            border: 1px solid var(--color-border);
        }

        .userdata-form h4 {
            margin-bottom: 12px;
            color: var(--color-accent);
            font-size: 13px;
        }

        .form-group {
            margin-bottom: 12px;
        }

        .form-group label {
            display: block;
            font-size: 12px;
            font-weight: 500;
            margin-bottom: 4px;
            color: var(--color-text-dark);
        }

        .form-group input[type="text"],
        .form-group input[type="number"],
        .form-group textarea,
        .form-group select {
            width: 100%;
            padding: 8px 10px;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            font-size: 12px;
            font-family: var(--font-family);
        }

        .form-group textarea {
            resize: vertical;
            min-height: 60px;
        }

        .form-group input:focus,
        .form-group textarea:focus,
        .form-group select:focus {
            outline: none;
            border-color: var(--color-accent);
            box-shadow: 0 0 0 2px rgba(44, 90, 160, 0.1);
        }

        .form-buttons {
            display: flex;
            gap: 8px;
            margin-top: 12px;
        }

        .form-buttons button {
            flex: 1;
            padding: 8px 12px;
            font-size: 12px;
        }

        .empty-userdata {
            text-align: center;
            padding: 20px;
            color: var(--color-text-light);
            font-size: 12px;
        }

        /* Drop zone styling */
        .drop-zone {
            border: 2px dashed var(--color-border);
            border-radius: var(--radius-md);
            padding: 16px;
            text-align: center;
            color: var(--color-text-light);
            font-size: 12px;
            min-height: 100px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
        }

        .drop-zone.drag-over {
            border-color: var(--color-accent);
            background: var(--color-accent-light);
            color: var(--color-accent);
            font-weight: 500;
        }

        /* Action buttons */
        .action-buttons {
            display: flex;
            gap: 12px;
            justify-content: center;
            margin-top: 30px;
            flex-wrap: wrap;
        }

        .selected-count {
            text-align: center;
            padding: 12px;
            background: var(--color-accent-light);
            color: var(--color-accent);
            border-radius: var(--radius-md);
            font-size: 13px;
            font-weight: 500;
            margin-bottom: 16px;
        }

        .status-message {
            padding: 12px 16px;
            border-radius: var(--radius-md);
            margin-bottom: 16px;
            font-size: 13px;
            display: none;
        }

        .status-message.show {
            display: block;
        }

        .status-message.success {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #c8e6c9;
        }

        .status-message.error {
            background: #ffebee;
            color: #c62828;
            border: 1px solid #ffcdd2;
        }

        .status-message.loading {
            background: var(--color-accent-light);
            color: var(--color-accent);
            border: 1px solid #b3d9f2;
        }

        .empty-state {
            text-align: center;
            padding: 40px 20px;
            color: var(--color-text-light);
        }

        .empty-state-icon {
            font-size: 48px;
            margin-bottom: 16px;
            opacity: 0.5;
        }

        .footer {
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid var(--color-border);
            text-align: center;
            color: var(--color-text-light);
            font-size: 12px;
        }

        .footer-buttons {
            display: flex;
            gap: 12px;
            justify-content: center;
            margin-top: 16px;
        }

        .footer-buttons button {
            padding: 10px 20px;
        }

        /* Modal Styles */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            animation: fadeIn 0.2s ease;
        }

        .modal.show {
            display: block;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .modal-content {
            background-color: var(--color-white);
            margin: 50px auto;
            padding: 24px;
            border-radius: var(--radius-md);
            width: 90%;
            max-width: 500px;
            box-shadow: var(--shadow-lg);
            animation: slideIn 0.3s ease;
        }

        @keyframes slideIn {
            from { transform: translateY(-50px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }

        .modal-header {
            margin-bottom: 20px;
            border-bottom: 1px solid var(--color-border);
            padding-bottom: 12px;
        }

        .modal-header h2 {
            color: var(--color-accent);
            font-size: 20px;
        }

        .modal-body {
            margin-bottom: 20px;
        }

        .modal-footer {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
        }

        .modal-footer button {
            padding: 10px 20px;
        }

        .tab-buttons {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            border-bottom: 2px solid var(--color-border);
        }

        .tab-button {
            padding: 12px 20px;
            background: none;
            border: none;
            border-bottom: 3px solid transparent;
            cursor: pointer;
            font-weight: 500;
            color: var(--color-text-light);
            transition: all 0.2s ease;
        }

        .tab-button.active {
            color: var(--color-accent);
            border-bottom-color: var(--color-accent);
        }

        @media (max-width: 1200px) {
            .editor-table {
                font-size: 12px;
            }

            .editor-table th,
            .editor-table td {
                padding: 10px;
            }

            .actions-column,
            .operations-column,
            .userdata-column {
                min-width: 150px;
            }
        }

        @media (max-width: 768px) {
            .container {
                padding: 20px;
            }

            .input-group {
                flex-direction: column;
            }

            .header h1 {
                font-size: 24px;
            }

            .editor-table {
                font-size: 11px;
            }

            .editor-table th,
            .editor-table td {
                padding: 8px;
            }

        }

        .task-header {
                      display: flex;              /* Make children align in a row */
                      justify-content: space-between; /* Push first item left, last item right */
                      align-items: center;        /* Vertically center them */
                      margin-bottom: 10px;        /* Optional spacing */
        }

        /* Loading Div */
        .loading-div {
            display: none;
            text-align: center;
            padding: 20px;
            background: var(--color-accent-light);
            border: 1px solid var(--color-accent);
            border-radius: var(--radius-md);
            margin-top: 20px;
            color: var(--color-accent);
            font-weight: 500;
        }

        /* Elements Table */
        .elements-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: var(--color-white);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
            display: none; /* Hidden until data is fetched */
        }

        .elements-table thead {
            background: var(--color-accent-light);
            border-bottom: 2px solid var(--color-accent);
        }

        .elements-table th {
            padding: 12px 16px;
            text-align: left;
            font-weight: 600;
            color: var(--color-accent);
            border-right: 1px solid var(--color-border);
        }

        .elements-table th:last-child {
            border-right: none;
        }

        .elements-table td {
            padding: 10px 16px;
            border-right: 1px solid var(--color-border);
            border-bottom: 1px solid var(--color-border);
            color: var(--color-text-dark);
            font-size: 13px;
            word-break: break-all; /* Handle long selectors */
        }

        .elements-table td:last-child {
            border-right: none;
        }

        .elements-table tbody tr:hover {
            background: var(--color-hover);
        }

        .elements-table tbody tr:last-child td {
            border-bottom: none;
        }

        /* Line Separator */
        .line-separator {
            border: none;
            border-top: 1px solid var(--color-border);
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🚀 Automation Tool</h1>
            <p>Enterprise Test Case Runner & Editor</p>
        </div>

        <!-- Tab Navigation -->
        <div class="tab-buttons">
            <button class="tab-button active" onclick="switchTab('runner')">📁 Test Runner</button>
            <button class="tab-button" onclick="switchTab('create')">➕ Create Testcase</button>
            <button class="tab-button" onclick="switchTab('edit')">✏️ Edit Testcase</button>
            <button class="tab-button" onclick="switchTab('running')">⏩ Running Testcases</button>
        </div>

        <!-- Status Messages -->
        <div id="statusMessage" class="status-message"></div>

        <!-- ===== TAB 1: TEST RUNNER ===== -->
        <div id="runner" class="main-section active">
            <!-- Input Section -->
            <div class="section">
                <div class="section-title">Test Case Configuration</div>
                <div class="input-group">
                    <input 
                        type="text" 
                        id="folderName" 
                        class="input-field" 
                        placeholder="Enter Testcase Folder Name (e.g., /test/cases)"
                        autocomplete="off"
                    />
                    <button class="btn btn-primary" id="fetchBtn" onclick="fetchTestCases()">
                        📁 Fetch
                    </button>
                </div>
            </div>

            <!-- Tree Section -->
            <div class="section">
                <div class="section-title">Available Test Cases</div>
                <div id="treeContainer" class="tree-container">
                    <div class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <div>Enter a folder path and click Fetch to load test cases</div>
                    </div>
                </div>
            </div>

            <!-- Selection Count -->
            <div id="selectionCount" class="selected-count" style="display: none;">
                <span id="selectedCountText">0 items selected</span>
            </div>
            <p class="empty-userdata">* When selecting folders, the testcases present in its sub-tree are selected automatically.</p>

            <!-- Action Buttons -->
            <div class="action-buttons">
                <button class="btn btn-secondary" id="clearBtn" onclick="clearSelection(true)" style="display: none;">
                    Clear Selection
                </button>
                <button class="btn btn-primary" id="runBtn" onclick="runTestCases()" style="display: none;">
                    ▶ Run Test Cases
                </button>
            </div>
        </div>

        <!-- ===== TAB 2: CREATE TESTCASE ===== -->
        <div id="create" class="main-section">
            <div class="section">
                <div class="section-title">Create New Test Case</div>
                <div class="input-group">
                    <input 
                        type="text" 
                        id="createTestcaseId" 
                        class="input-field" 
                        placeholder="Enter Test Case ID (e.g., TC001)"
                        autocomplete="off"
                    />
                </div>
            </div>

            <!-- Editor Table -->
            <table class="editor-table" id="editorTableCreate">
                <thead>
                    <tr>
                        <th style="width: 20%;">Actions</th>
                        <th style="width: 40%;">Operations</th>
                        <th style="width: 40%;">Userdata</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="actions-column">
                            <div id="availableActionsCreate" class="actions-column"></div>
                        </td>
                        <td class="operations-column">
                            <div id="operationsListCreate" class="operations-list drop-zone" ondrop="handleDrop(event, 'create')" ondragover="handleDragOver(event)" ondragleave="handleDragLeave(event)">
                                Drop actions here to build your test case
                            </div>
                        </td>
                        <td class="userdata-column">
                            <div id="userdataFormCreate" class="empty-userdata">
                                Click an operation to edit its details
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            <p class="empty-userdata">* Populate Epic, Feature, Story, Description only in the first Action of the Testcase.</p>
            <!-- Create Footer Buttons -->
            <div class="footer-buttons">
                <button class="btn btn-success" onclick="createTestcase()">
                    ✓ Create
                </button>
            </div>

            <hr class="line-separator">

            <div class="section">
                <div class="section-title">Fetch Page Elements</div>
                <div class="input-group">
                    <input
                        type="text"
                        id="createUrlInput"
                        class="input-field"
                        placeholder="Enter webpage URL (e.g., https://example.com)"
                        autocomplete="off"
                    />
                    <button class="btn btn-primary" id="fetchElementsCreateBtn" onclick="fetchPageElements('create')">
                        📡 Fetch Elements
                    </button>
                </div>
            </div>

            <div id="loadingCreate" class="loading-div">
                Loading elements... Please wait.
            </div>

            <table id="elementsTableCreate" class="elements-table" style="table-layout: fixed">
                  <colgroup>
                      <col style="width: 15%;">
                      <col style="width: 15%;">
                      <col style="width: 35%;">
                      <col style="width: 35%;">
                  </colgroup>
                <thead>
                    <tr>
                        <th>Tag Name</th>
                        <th>Element Text</th>
                        <th>CSS Selector</th>
                        <th>XPath Selector</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>

        <!-- ===== TAB 3: EDIT TESTCASE ===== -->
        <div id="edit" class="main-section">
            <div class="section">
                <div class="section-title">Edit Existing Test Case</div>
                <div class="input-group">
                    <input 
                        type="text" 
                        id="editFilePath" 
                        class="input-field" 
                        placeholder="Enter JSON file path (e.g., /test/cases/TC001.json)"
                        autocomplete="off"
                    />
                    <button class="btn btn-primary" id="loadBtn" onclick="loadTestcaseFile()">
                        📂 Load
                    </button>
                </div>
            </div>

            <!-- Editor Table -->
            <table class="editor-table" id="editorTableEdit">
                <thead>
                    <tr>
                        <th style="width: 20%;">Actions</th>
                        <th style="width: 40%;">Operations</th>
                        <th style="width: 40%;">Userdata</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="actions-column">
                            <div id="availableActionsEdit" class="actions-column"></div>
                        </td>
                        <td class="operations-column">
                            <div id="operationsListEdit" class="operations-list drop-zone" ondrop="handleDrop(event, 'edit')" ondragover="handleDragOver(event)" ondragleave="handleDragLeave(event)">
                                Drop actions here
                            </div>
                        </td>
                        <td class="userdata-column">
                            <div id="userdataFormEdit" class="empty-userdata">
                                Click an operation to edit its details
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            <p class="empty-userdata">* Populate Epic, Feature, Story, Description only in the first Action of the Testcase.</p>
            <!-- Edit Footer Buttons -->
            <div class="footer-buttons">
                <button class="btn btn-danger" onclick="clearEditorTab('edit')" style="display: none;" id="editCancelBtn">
                    ✗ Cancel
                </button>
                <button class="btn btn-success" onclick="saveTestcaseFile()" style="display: none;" id="editSaveBtn">
                    ✓ Save
                </button>
            </div>
            <hr class="line-separator">

            <div class="section">
                <div class="section-title">Fetch Page Elements</div>
                <div class="input-group">
                    <input
                        type="text"
                        id="editUrlInput"
                        class="input-field"
                        placeholder="Enter webpage URL (e.g., https://example.com)"
                        autocomplete="off"
                    />
                    <button class="btn btn-primary" id="fetchElementsEditBtn" onclick="fetchPageElements('edit')">
                        📡 Fetch Elements
                    </button>
                </div>
            </div>

            <div id="loadingEdit" class="loading-div">
                Loading elements... Please wait.
            </div>

            <table id="elementsTableEdit" class="elements-table" style="table-layout: fixed">
                <colgroup>
                    <col style="width: 15%;">
                    <col style="width: 15%;">
                    <col style="width: 35%;">
                    <col style="width: 35%;">
                </colgroup>
                <thead>
                    <tr>
                        <th>Tag Name</th>
                        <th>Element Text</th>
                        <th>CSS Selector</th>
                        <th>XPath Selector</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>

        <!-- ===== TAB 4: RUNNING TESTCASES ===== -->
        <div id="running" class="main-section">
            <div class="section">
                <div class="task-header">
                  <div class="section-title">Running Tasks</div>
                  <button class="btn btn-primary refresh-tasks-btn" onclick="refreshTasks()">🔄 Refresh Tasks</button>
                </div>
                <table id="tasksTable" class="editor-table" border="1" style="margin-top: 10px;">
                    <thead>
                        <tr>
                            <th style="width: 60%">Task ID</th>
                            <th style="width: 40%">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr><td colspan="2" style="text-align: center; color: #666;">Click Refresh to load tasks</td></tr>
                    </tbody>
                </table>
            </div>
            <p class="empty-userdata">* More than 5 tasks will be queued.</p>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>Automation Tool v2.0 | Create, Edit, and Run test cases</p>
        </div>
    </div>

    <!-- File Save Dialog Modal -->
    <div id="saveModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Save Test Case</h2>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>File Name:</label>
                    <input type="text" id="saveFileName" class="input-field" placeholder="e.g., TC001.json" />
                </div>
                <div class="form-group">
                    <label>Save Location:</label>
                    <input type="text" id="saveLocation" class="input-field" placeholder="e.g., /test/cases" />
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('saveModal')">Cancel</button>
                <button class="btn btn-primary" onclick="confirmSaveLocation()">Save</button>
            </div>
        </div>
    </div>

    <script>
        // ===== GLOBAL VARIABLES =====
        //const PREDEFINED_ACTIONS = ['gotoUrl', 'click', 'type', 'wait', 'assertText', 'scroll', 'hover'];
        const PREDEFINED_ACTIONS = {
          gotoUrl: 'gotoUrl',
          click: 'click',
          type: 'type',
          wait: 'wait',
          scroll: 'scroll',
          hover: 'hover',
          clear : 'clear',
          frame: 'frame',
          isVisible: 'assertVisibility',
          isVisibleTimeout: 'assertVisibility',
          isHidden: 'assertVisibility',
          isAttached: 'assertVisibility',
          isDetached: 'assertVisibility',
          isInViewport: 'assertVisibility',
          hasText: 'assertText',
          hasTextPattern: 'assertText',
          containsText: 'assertText',
          containsTextPattern: 'assertText',
          hasTextMultipleElements: 'assertText',
          notContainsText: 'assertText',
          hasValue: 'assertElement',
          isEditable: 'assertElement',
          isChecked: 'assertElement',
          isNotChecked: 'assertElement',
          hasValues: 'assertElement',
          isEnabled: 'assertElement',
          isDisabled: 'assertElement',
          isFocused: 'assertElement',
          hasId: 'assertElement',
          hasCount: 'assertCount',
          exists: 'assertCount',
          notExists: 'assertCount',
          isEmpty: 'assertCount',
          hasTitle: 'assertPage',
          hasTitlePattern: 'assertPage',
          hasURL: 'assertPage',
          hasURLPattern: 'assertPage',
          hasClass: 'assertClass',
          containsClass: 'assertClass',
          notHasClass: 'assertClass',
          hasAttribute: 'assertAttribute',
          hasAttributeValue: 'assertAttribute',
          hasAttributePattern: 'assertAttribute',
          notHasAttribute: 'assertAttribute',
          hasCSSProperty: 'assertCSSProperty',
          hasCSSPropertyPattern: 'assertCSSProperty',
        };

        let currentMode = null; // 'create' or 'edit'
        let currentSelectedAction = null; // Track which action is being edited
        let editingFilePath = null; // Track the file being edited
        let operationsData = {}; // Store operation details: { actionId: { actionType, locator, arguments, etc } }
        let selectedItems = new Set(); // For runner tab
        let treeData = null; // For runner tab

        // ===== TAB SWITCHING =====
        function switchTab(tab) {
            // Hide all sections
            document.querySelectorAll('.main-section').forEach(s => s.classList.remove('active'));
            document.querySelectorAll('.tab-button').forEach(b => b.classList.remove('active'));
            
            // Show selected section
            document.getElementById(tab).classList.add('active');
            event.target.classList.add('active');
            
            // Initialize editors if needed
            if (tab === 'create') {
                initializeEditor('create');
            } else if (tab === 'edit') {
                initializeEditor('edit');
            }
        }

        // ===== EDITOR INITIALIZATION =====
        function initializeEditor(mode) {
            const actionsContainer = document.getElementById('availableActions' + (mode === 'create' ? 'Create' : 'Edit'));
            
            // Clear previous actions
            while (actionsContainer.firstChild) {
                actionsContainer.removeChild(actionsContainer.firstChild);
            }
            
            // Add predefined actions
            Object.entries(PREDEFINED_ACTIONS).forEach(([key, value]) => {
                //console.log(key, value);
                const actionItem = document.createElement('div');
                actionItem.className = 'action-item';
                actionItem.draggable = true;
                actionItem.textContent = key;
                actionItem.ondragstart = (e) => {
                    e.dataTransfer.effectAllowed = 'copy';
                    e.dataTransfer.setData('action', key);
                    e.dataTransfer.setData('methodName', value);
                    actionItem.classList.add('dragging');
                };
                actionItem.ondragend = () => {
                    actionItem.classList.remove('dragging');
                };
                actionsContainer.appendChild(actionItem);
            });

            // Clear operations list
            const operationsList = document.getElementById('operationsList' + (mode === 'create' ? 'Create' : 'Edit'));
            while (operationsList.firstChild) {
                operationsList.removeChild(operationsList.firstChild);
            }
            operationsList.appendChild(document.createTextNode('Drop actions here to build your test case'));
            
            // Clear userdata form
            const userdataForm = document.getElementById('userdataForm' + (mode === 'create' ? 'Create' : 'Edit'));
            while (userdataForm.firstChild) {
                userdataForm.removeChild(userdataForm.firstChild);
            }
            const placeholder = document.createElement('div');
            placeholder.className = 'empty-userdata';
            placeholder.textContent = 'Click an operation to edit its details';
            userdataForm.appendChild(placeholder);
            
            operationsData = {};
            currentSelectedAction = null;
            currentMode = mode;
        }

        // ===== DRAG & DROP HANDLERS =====
        function handleDragOver(e) {
            e.preventDefault();
            e.dataTransfer.dropEffect = 'copy';
            e.currentTarget.classList.add('drag-over');
        }

        function handleDragLeave(e) {
            e.currentTarget.classList.remove('drag-over');
        }

        function handleDrop(e, mode) {
            e.preventDefault();
            e.currentTarget.classList.remove('drag-over');
            const action = e.dataTransfer.getData('action');
            const methodName = e.dataTransfer.getData('methodName');
            if (action) {
                addOperation(action, methodName, mode);
            }
        }

        // ===== ADD OPERATION =====
        function addOperation(action, methodName, mode) {
            console.log("add op action : "+action+" - mode : "+mode);
            const operationsList = document.getElementById('operationsList' + (mode === 'create' ? 'Create' : 'Edit'));

            // Clear placeholder if first item
            if (operationsList.textContent.includes('Drop actions here')) {
                while (operationsList.firstChild) {
                    operationsList.removeChild(operationsList.firstChild);
                }
            }

            const actionId = 'action_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            const operationIndex = Array.from(operationsList.children).length + 1;
            operationsData[actionId] = {
                actionType: action,
                locator: '',
                arguments: [],
                testcaseId: mode === 'create' ? document.getElementById('createTestcaseId').value : '',
                methodName: methodName,
                additionalData: {},
                frameLocator: '',
                epic: '',
                feature: '',
                story: '',
                description: ''
            };

            // Create operation div item
            const operationItem = document.createElement('div');
            operationItem.className = 'operation-item';
            operationItem.id = 'op_' + actionId;
            operationItem.draggable = true;
            operationItem.ondragstart = (e) => {
                e.dataTransfer.effectAllowed = 'move';
                e.dataTransfer.setData('operationId', actionId);
            };
            const operationContent = document.createElement('div');
            operationContent.className = 'operation-item-content';
            const wrapper = document.createElement('div');
            wrapper.className = 'operation-wrapper';
            const indexDiv = document.createElement('div');
            indexDiv.className = 'operation-item-index';
            indexDiv.textContent = operationIndex;  // safely insert text
            const actionDiv = document.createElement('div');
            actionDiv.className = 'operation-item-action';
            actionDiv.textContent = action;  // safely insert text
            wrapper.appendChild(indexDiv);
            wrapper.appendChild(actionDiv);
            operationContent.appendChild(wrapper);

            operationContent.style.cursor = 'pointer';
            operationContent.onclick = () => editOperation(actionId, mode);
            const operationButtons = document.createElement('div');
            operationButtons.className = 'operation-item-buttons';
            const btn = document.createElement('button');
            btn.className = 'btn btn-secondary';
            btn.style.padding = '4px 8px';
            btn.textContent = '🗑';
            btn.addEventListener('click', () => {
                deleteOperation(actionId, mode);
            });
            operationButtons.appendChild(btn);
            operationItem.appendChild(operationContent);
            operationItem.appendChild(operationButtons);
            operationsList.appendChild(operationItem);

            reindexOperations(mode);
        }

        // ===== EDIT OPERATION =====
        function editOperation(actionId, mode) {
            console.log("editOperation action id : "+actionId+" mode : "+mode);
            currentSelectedAction = actionId;
            const data = operationsData[actionId];
            const userdataForm = document.getElementById('userdataForm' + (mode === 'create' ? 'Create' : 'Edit'));
            while (userdataForm.firstChild) {
                userdataForm.removeChild(userdataForm.firstChild);
            }
            const formWrapper = document.createElement('div');
            formWrapper.className = 'userdata-form';

            const heading = document.createElement('h4');
            heading.textContent = 'Edit Action: ' + data.actionType;
            formWrapper.appendChild(heading);

            function createInputGroup(labelText, inputType, inputId, value, placeholder, readOnly = false, isTextarea = false) {
                const group = document.createElement('div');
                group.className = 'form-group';
                const label = document.createElement('label');
                label.textContent = labelText;
                group.appendChild(label);
                let input;
                if (isTextarea) {
                    input = document.createElement('textarea');
                } else {
                    input = document.createElement('input');
                    input.type = inputType;
                }
                if (inputId) input.id = inputId;
                if (value !== undefined) input.value = value;
                if (placeholder) input.placeholder = placeholder;
                if (readOnly) input.readOnly = true;
                if (readOnly) input.style.background = '#f5f5f5';
                group.appendChild(input);
                return group;
            }

            formWrapper.appendChild(createInputGroup('Action Type:', 'text', null, data.actionType, null, true));
            formWrapper.appendChild(createInputGroup('Locator:', 'text', 'editLocator', data.locator || '', 'e.g., #username, button[type=submit]'));
            formWrapper.appendChild(createInputGroup('Arguments (comma-separated):', 'text', 'editArguments', Array.isArray(data.arguments) ? data.arguments.join(', ') : '', 'e.g., admin, secret123'));
            formWrapper.appendChild(createInputGroup('Test Case ID:', 'text', 'editTestcaseId', data.testcaseId || ''));
            formWrapper.appendChild(createInputGroup('Additional Data (JSON):', 'text', 'editAdditionalData', JSON.stringify(data.additionalData || {}, null, 2), null, false, true));
            formWrapper.appendChild(createInputGroup('Frame Locator:', 'text', 'editFrameLocator', data.frameLocator || '', 'e.g., #username, button[type=submit]'));
            formWrapper.appendChild(createInputGroup('Epic:', 'text', 'editEpic', data.epic || ''));
            formWrapper.appendChild(createInputGroup('Feature:', 'text', 'editFeature', data.feature || ''));
            formWrapper.appendChild(createInputGroup('Story:', 'text', 'editStory', data.story || ''));
            formWrapper.appendChild(createInputGroup('Description:', 'text', 'editDescription', data.description || '', null, false, true));

            const buttonsDiv = document.createElement('div');
            buttonsDiv.className = 'form-buttons';
            const saveBtn = document.createElement('button');
            saveBtn.className = 'btn btn-success';
            saveBtn.textContent = '✓ Save';
            saveBtn.addEventListener('click', () => saveOperation(actionId, mode));
            buttonsDiv.appendChild(saveBtn);
            const cancelBtn = document.createElement('button');
            cancelBtn.className = 'btn btn-secondary';
            cancelBtn.textContent = '✗ Cancel';
            cancelBtn.addEventListener('click', () => cancelEditOperation(mode));
            buttonsDiv.appendChild(cancelBtn);
            formWrapper.appendChild(buttonsDiv);

            userdataForm.appendChild(formWrapper);

        }

        // ===== SAVE OPERATION =====
        function saveOperation(actionId, mode) {
            operationsData[actionId].locator = document.getElementById('editLocator').value;
            operationsData[actionId].arguments = document.getElementById('editArguments').value
                .split(',')
                .map(arg => arg.trim())
                .filter(arg => arg);
            operationsData[actionId].testcaseId = document.getElementById('editTestcaseId').value;
            operationsData[actionId].frameLocator = document.getElementById('editFrameLocator').value;
            operationsData[actionId].epic = document.getElementById('editEpic').value;
            operationsData[actionId].feature = document.getElementById('editFeature').value;
            operationsData[actionId].story = document.getElementById('editStory').value;
            operationsData[actionId].description = document.getElementById('editDescription').value;
            
            try {
                operationsData[actionId].additionalData = JSON.parse(document.getElementById('editAdditionalData').value);
            } catch (e) {
                operationsData[actionId].additionalData = {};
            }
            
            cancelEditOperation(mode);
            showStatus('Operation saved', 'success');
        }

        // ===== CANCEL EDIT OPERATION =====
        function cancelEditOperation(mode) {
            const userdataForm = document.getElementById('userdataForm' + (mode === 'create' ? 'Create' : 'Edit'));
            while (userdataForm.firstChild) {
                userdataForm.removeChild(userdataForm.firstChild);
            }
            const placeholder = document.createElement('div');
            placeholder.className = 'empty-userdata';
            placeholder.textContent = 'Click an operation to edit its details';
            userdataForm.appendChild(placeholder);
            currentSelectedAction = null;
        }

        // ===== DELETE OPERATION =====
        function deleteOperation(actionId, mode) {
            const operationItem = document.getElementById('op_' + actionId);
            if (operationItem) {
                operationItem.remove();
            }
            delete operationsData[actionId];
            reindexOperations(mode);
            cancelEditOperation(mode);
            showStatus('Operation deleted', 'success');
        }

        // ===== REINDEX OPERATIONS =====
        function reindexOperations(mode) {
            const operationsList = document.getElementById('operationsList' + (mode === 'create' ? 'Create' : 'Edit'));
            Array.from(operationsList.children).forEach((child, index) => {
                const indexBadge = child.querySelector('.operation-item-index');
                if (indexBadge) {
                    indexBadge.textContent = index + 1;
                }
            });
        }

        // ===== CREATE TESTCASE =====
        function createTestcase() {
            const testcaseId = document.getElementById('createTestcaseId').value.trim();
            
            if (!testcaseId) {
                showStatus('Please enter a Test Case ID', 'error');
                return;
            }
            
            if (Object.keys(operationsData).length === 0) {
                showStatus('Please add at least one operation', 'error');
                return;
            }
            
            // Open save dialog
            openModal('saveModal');
        }

        // ===== OPEN/CLOSE MODAL =====
        function openModal(modalId) {
            document.getElementById(modalId).classList.add('show');
        }

        function closeModal(modalId) {
            document.getElementById(modalId).classList.remove('show');
        }

        // ===== CONFIRM SAVE LOCATION =====
        function confirmSaveLocation() {
            const fileName = document.getElementById('saveFileName').value.trim();
            const location = document.getElementById('saveLocation').value.trim();
            
            if (!fileName || !location) {
                showStatus('Please enter both file name and location', 'error');
                return;
            }
            
            // Prepare data for saving
            const testcaseId = document.getElementById('createTestcaseId').value;
            const testcaseData = Object.values(operationsData).map(op => ({
                actionType: op.actionType,
                locator: op.locator || null,
                arguments: op.arguments.length > 0 ? op.arguments : undefined,
                testcaseId: testcaseId,
                methodName: op.methodName,
                frameLocator: op.frameLocator,
                ...(op.additionalData && Object.keys(op.additionalData).length > 0 ? { additionalData: op.additionalData } : {}),
                ...(op.epic ? { epic: op.epic } : {}),
                ...(op.feature ? { feature: op.feature } : {}),
                ...(op.story ? { feature: op.story } : {}),
                ...(op.description ? { description: op.description } : {})
            }));
            
            // Send to backend
            saveTestcaseToServer(fileName, location, testcaseData, 'create');
            closeModal('saveModal');
        }

        // ===== SAVE TESTCASE TO SERVER =====
        async function saveTestcaseToServer(fileName, location, testcaseData, operation) {
            showStatus('Saving test case...', 'loading');
            
            try {
                const response = await fetch('TestCaseSaveServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        fileName: fileName,
                        location: location,
                        data: testcaseData,
                        operation: operation,
                        filePath: editingFilePath
                    })
                });
                
                const result = await response.json();
                
                if (result.success) {
                    showStatus('✓ Test case saved successfully!', 'success');
                    
                    // Reset form
                    if (operation === 'create') {
                        document.getElementById('createTestcaseId').value = '';
                        clearEditorTab('create');
                    }
                    
                    // Clear modal
                    document.getElementById('saveFileName').value = '';
                    document.getElementById('saveLocation').value = '';
                } else {
                    showStatus(result.message || 'Failed to save test case', 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showStatus('Error saving test case: ' + error.message, 'error');
            }
        }

        // ===== LOAD TESTCASE FILE =====
        async function loadTestcaseFile() {
            const filePath = document.getElementById('editFilePath').value.trim();
            
            if (!filePath) {
                showStatus('Please enter a file path', 'error');
                return;
            }
            
            showStatus('Loading test case file...', 'loading');
            
            try {
                const response = await fetch('TestCaseLoadServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'filePath=' + encodeURIComponent(filePath)
                });
                
                const result = await response.json();
                
                if (result.success) {
                    editingFilePath = filePath;
                    operationsData = {};
                    // Load operations
                    result.data.forEach(action => {
                        const actionId = 'action_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                        operationsData[actionId] = {
                            actionType: action.actionType,
                            locator: action.locator || '',
                            arguments: Array.isArray(action.arguments) ? action.arguments : [],
                            testcaseId: action.testcaseId || '',
                            additionalData: action.additionalData || {},
                            methodName: action.methodName,
                            frameLocator: action.frameLocator,
                            epic: action.epic || '',
                            feature: action.feature || '',
                            story: action.story || '',
                            description: action.description || ''
                        };
                        editAddOperation(action.actionType, 'edit', actionId);
                    });
                    showStatus('Test case loaded successfully', 'success');
                    
                    // Show save/cancel buttons
                    document.getElementById('editSaveBtn').style.display = 'inline-flex';
                    document.getElementById('editCancelBtn').style.display = 'inline-flex';
                } else {
                    showStatus(result.message || 'Failed to load test case', 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showStatus('Error loading test case: ' + error.message, 'error');
            }
        }

        // ===== EDIT ADD OPERATION =====
        function editAddOperation(action, mode, actionId) {
            console.log("edit function add op action : "+action+" - mode : "+mode);
            const operationsList = document.getElementById('operationsList' + (mode === 'create' ? 'Create' : 'Edit'));

            // Clear placeholder if first item
            if (operationsList.textContent.includes('Drop actions here')) {
                while (operationsList.firstChild) {
                    operationsList.removeChild(operationsList.firstChild);
                }
            }

            //const actionId = 'action_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            const operationIndex = Array.from(operationsList.children).length + 1;

            // Create operation div item
            const operationItem = document.createElement('div');
            operationItem.className = 'operation-item';
            operationItem.id = 'op_' + actionId;
            operationItem.draggable = true;
            operationItem.ondragstart = (e) => {
                e.dataTransfer.effectAllowed = 'move';
                e.dataTransfer.setData('operationId', actionId);
            };
            const operationContent = document.createElement('div');
            operationContent.className = 'operation-item-content';
            const wrapper = document.createElement('div');
            wrapper.className = 'operation-wrapper';
            const indexDiv = document.createElement('div');
            indexDiv.className = 'operation-item-index';
            indexDiv.textContent = operationIndex;
            const actionDiv = document.createElement('div');
            actionDiv.className = 'operation-item-action';
            actionDiv.textContent = action;
            wrapper.appendChild(indexDiv);
            wrapper.appendChild(actionDiv);
            operationContent.appendChild(wrapper);

            operationContent.style.cursor = 'pointer';
            operationContent.onclick = () => editOperation(actionId, mode);
            const operationButtons = document.createElement('div');
            operationButtons.className = 'operation-item-buttons';
            const btn = document.createElement('button');
            btn.className = 'btn btn-secondary';
            btn.style.padding = '4px 8px';
            btn.textContent = '🗑';
            btn.addEventListener('click', () => {
                deleteOperation(actionId, mode);
            });
            operationButtons.appendChild(btn);
            operationItem.appendChild(operationContent);
            operationItem.appendChild(operationButtons);
            operationsList.appendChild(operationItem);

            reindexOperations(mode);
        }

        // ===== SAVE TESTCASE FILE (EDIT) =====
        function saveTestcaseFile() {
            if (Object.keys(operationsData).length === 0) {
                showStatus('Please add at least one operation', 'error');
                return;
            }
            const normalizeString = value => value ?? "";
            const normalizeArray = value => Array.isArray(value) ? value : [];
            const normalizeObject = value => value && typeof value === "object" && !Array.isArray(value) ? value : {};
            const addIfDefined = (key, value) => value !== undefined && value !== null ? { [key]: value } : {};
            const addIfNonEmptyArray = (key, value) => Array.isArray(value) && value.length > 0 ? { [key]: value } : {};
            const addIfNonEmptyObject = (key, value) => value && Object.keys(value).length > 0 ? { [key]: value } : {};

            const testcaseData = Object.values(operationsData).map(op => ({
                actionType: op.actionType,
                locator: normalizeString(op.locator),
                testcaseId: normalizeString(op.testcaseId),
                arguments: normalizeArray(op.arguments),
                methodName: op.methodName,
                frameLocator: op.frameLocator,
                ...addIfNonEmptyObject('additionalData', op.additionalData),
                ...addIfDefined('epic', op.epic),
                ...addIfDefined('feature', op.feature),
                ...addIfDefined('story', op.story),
                ...addIfDefined('description', op.description),
            }));
            
            saveTestcaseToServer('', '', testcaseData, 'edit');
            clearEditorTab('edit');
        }

        // ===== CLEAR EDITOR TAB =====
        function clearEditorTab(mode) {
            initializeEditor(mode);
            editingFilePath = null;
            document.getElementById('editSaveBtn').style.display = 'none';
            document.getElementById('editCancelBtn').style.display = 'none';
            
            if (mode === 'create') {
                document.getElementById('createTestcaseId').value = '';
            } else {
                document.getElementById('editFilePath').value = '';
            }
            
            // showStatus((mode === 'create' ? 'Create' : 'Edit') + ' cancelled', 'success');
        }

        // ===== FETCH PAGE ELEMENTS =====
        async function fetchPageElements(mode) {
            const urlInput = document.getElementById(mode === 'create' ? 'createUrlInput' : 'editUrlInput');
            const url = urlInput.value.trim();

            if (!url) {
                showStatus('Please enter a valid URL', 'error');
                return;
            }

            const loadingDiv = document.getElementById(mode === 'create' ? 'loadingCreate' : 'loadingEdit');
            const elementsTable = document.getElementById(mode === 'create' ? 'elementsTableCreate' : 'elementsTableEdit');
            const tbody = elementsTable.querySelector('tbody');

            // Show loading
            loadingDiv.style.display = 'block';
            elementsTable.style.display = 'none';
            while (tbody.firstChild) {
                tbody.removeChild(tbody.firstChild);
            }
            showStatus('Fetching page elements...', 'loading');

            try {
                const response = await fetch('WebpageLocatorGenServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'url=' + encodeURIComponent(url)
                });

                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }

                const result = await response.json();
                console.log(JSON.stringify(result));
                console.log(Array.isArray(result));
                console.log(result.length);
                console.log(typeof result);
                console.log(Array.isArray(result));


                if (result.length > 0) {
                    // Render table rows
                    result.forEach(item => {
                        console.log(item);
                        const row = document.createElement('tr');
                        const cellTag = document.createElement('td');
                        cellTag.textContent = item.tagName || 'N/A';
                        row.appendChild(cellTag);
                        const cellText = document.createElement('td');
                        cellText.textContent = item.text || 'N/A';
                        row.appendChild(cellText);
                        const cellCss = document.createElement('td');
                        cellCss.textContent = item.css || 'N/A';
                        row.appendChild(cellCss);
                        const cellXpath = document.createElement('td');
                        cellXpath.textContent = item.xpath || 'N/A';
                        row.appendChild(cellXpath);
                        tbody.appendChild(row);
                    });

                    elementsTable.style.display = 'table';
                    showStatus('✓ Elements fetched successfully! (' + result.length + ' items)', 'success');
                } else {
                    showStatus('No elements found or empty response', 'warning');
                }
            } catch (error) {
                console.error('Error fetching elements:', error);
                showStatus('Error fetching elements: ' + error.message, 'error');
            } finally {
                // Hide loading
                loadingDiv.style.display = 'none';
            }
        }

        // ===== RUNNER TAB FUNCTIONS =====
        async function fetchTestCases() {
            const folderName = document.getElementById('folderName').value.trim();
            
            if (!folderName) {
                showStatus('Please enter a folder name', 'error');
                return;
            }

            showStatus('Fetching test cases...', 'loading');
            document.getElementById('fetchBtn').disabled = true;

            try {
                const response = await fetch('FileListServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'folderPath=' + encodeURIComponent(folderName)
                });

                const result = await response.json();

                if (result.success) {
                    treeData = result.data;
                    renderTree(treeData);
                    showStatus('Test cases loaded successfully: ' + result.fileCount + ' files found', 'success');
                    selectedItems.clear();
                    updateSelectionUI();
                } else {
                    showStatus(result.message || 'Failed to fetch test cases', 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showStatus('Error connecting to server: ' + error.message, 'error');
            } finally {
                document.getElementById('fetchBtn').disabled = false;
            }
        }

        function renderTree(data) {
            const container = document.getElementById('treeContainer');
            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }
            
            if (!data || data.length === 0) {
                while (container.firstChild) {
                    container.removeChild(container.firstChild);
                }
                const emptyState = document.createElement('div');
                emptyState.className = 'empty-state';
                const iconDiv = document.createElement('div');
                iconDiv.className = 'empty-state-icon';
                iconDiv.textContent = '❌';
                emptyState.appendChild(iconDiv);
                const messageDiv = document.createElement('div');
                messageDiv.textContent = 'No test cases found in the specified folder';
                emptyState.appendChild(messageDiv);
                container.appendChild(emptyState);
                return;
            }

            const ul = document.createElement('div');
            ul.className = 'tree-item';
            
            data.forEach((item, index) => {
                ul.appendChild(createTreeNode(item, index));
            });
            
            container.appendChild(ul);
        }

        function createTreeNode(item, index) {
            const nodeWrapper = document.createElement('div');
            nodeWrapper.className = 'tree-item';
            const nodeId = 'node-' + Date.now() + '-' + index;

            const nodeHeader = document.createElement('div');
            nodeHeader.className = 'tree-node';

            const toggleBtn = document.createElement('div');
            toggleBtn.className = 'tree-toggle' + (item.children && item.children.length > 0 ? ' collapsed' : ' empty');
            toggleBtn.style.visibility = (item.children && item.children.length > 0) ? 'visible' : 'hidden';
            
            if (item.children && item.children.length > 0) {
                toggleBtn.onclick = (e) => {
                    e.stopPropagation();
                    toggleFolder(nodeId);
                };
            }
            nodeHeader.appendChild(toggleBtn);

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = nodeId;
            checkbox.dataset.path = item.path;
            checkbox.dataset.isFolder = item.folder;
            checkbox.onchange = () => updateSelection(checkbox);
            nodeHeader.appendChild(checkbox);

            const label = document.createElement('label');
            label.htmlFor = nodeId;
            label.className = 'tree-label ' + (item.folder ? 'folder' : 'file');
            label.style.cursor = 'pointer';
            label.onclick = (e) => {
                e.stopPropagation();
                checkbox.click();
            };
            
            const icon = document.createElement('span');
            icon.className = 'tree-icon';
            icon.textContent = item.folder ? '📂' : '📄';
            label.appendChild(icon);
            label.appendChild(document.createTextNode(item.name));
            
            nodeHeader.appendChild(label);
            nodeWrapper.appendChild(nodeHeader);

            if (item.children && item.children.length > 0) {
                const childrenContainer = document.createElement('div');
                childrenContainer.className = 'tree-children hidden';
                childrenContainer.id = nodeId + '-children';
                
                item.children.forEach((child, childIndex) => {
                    childrenContainer.appendChild(createTreeNode(child, childIndex));
                });
                
                nodeWrapper.appendChild(childrenContainer);
            }

            return nodeWrapper;
        }

        function toggleFolder(nodeId) {
            const childrenContainer = document.getElementById(nodeId + '-children');
            const toggleBtn = document.getElementById(nodeId).parentElement.querySelector('.tree-toggle');
            
            if (childrenContainer) {
                childrenContainer.classList.toggle('hidden');
                toggleBtn.classList.toggle('collapsed');
                toggleBtn.classList.toggle('expanded');
            }
        }

        function updateSelection(checkbox) {
            const path = checkbox.dataset.path;
            
            if (checkbox.checked) {
                selectedItems.add(path);
            } else {
                selectedItems.delete(path);
            }
            
            updateSelectionUI();
        }

        function updateSelectionUI() {
            const count = selectedItems.size;
            const countDisplay = document.getElementById('selectionCount');
            const countText = document.getElementById('selectedCountText');
            const runBtn = document.getElementById('runBtn');
            const clearBtn = document.getElementById('clearBtn');

            if (count > 0) {
                countDisplay.style.display = 'block';
                countText.textContent = count + ' item' + (count !== 1 ? 's' : '') + ' selected';
                runBtn.style.display = 'inline-flex';
                clearBtn.style.display = 'inline-flex';
            } else {
                countDisplay.style.display = 'none';
                runBtn.style.display = 'none';
                clearBtn.style.display = 'none';
            }
        }

        function clearSelection(showStatusFlag = false) {
            document.querySelectorAll('.tree-node input[type="checkbox"]').forEach(checkbox => {
                checkbox.checked = false;
            });
            selectedItems.clear();
            updateSelectionUI();
            if(showStatusFlag){
                showStatus('Selection cleared', 'success');
            }
        }

        async function runTestCases() {
            if (selectedItems.size === 0) {
                showStatus('No test cases selected', 'error');
                return;
            }

            const selectedArray = Array.from(selectedItems);
            showStatus('Running ' + selectedArray.length + ' test case(s)...', 'loading');
            document.getElementById('runBtn').disabled = true;

            try {
                const response = await fetch('TestCaseExecutorServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        selectedFiles: selectedArray
                    })
                });

                const result = await response.json();

                if (result.success) {
                    showStatus('✓ Test cases Execution Started successfully! ID: ' + result.executionId, 'success');
                    clearSelection(false);
                } else {
                    showStatus(result.message || 'Failed to execute test cases', 'error');
                }
            } catch (error) {
                console.error('Error:', error);
                showStatus('Error executing test cases: ' + error.message, 'error');
            } finally {
                document.getElementById('runBtn').disabled = false;
            }
        }

        async function refreshTasks() {
            try {
                const response = await fetch('TaskStatusServlet');
                const taskStatuses = await response.json();
                const tbody = document.querySelector('#tasksTable tbody');
                while (tbody.firstChild) {
                    tbody.removeChild(tbody.firstChild);
                }

                if (Object.keys(taskStatuses).length > 0) {
                    for (let taskId in taskStatuses) {
                        if (taskStatuses.hasOwnProperty(taskId)) {
                            var isRunning = taskStatuses[taskId];
                            const row = document.createElement('tr');
                            const cellId = document.createElement('td');
                            cellId.textContent = taskId;
                            row.appendChild(cellId);
                            const cellStatus = document.createElement('td');
                            cellStatus.textContent = isRunning ? '🟢 Running' : '⚪ Completed';
                            cellStatus.style.color = isRunning ? '#4caf50' : '#666';
                            cellStatus.style.fontWeight = isRunning ? 'bold' : 'normal';
                            row.appendChild(cellStatus);

                            tbody.appendChild(row);
                        }
                    }
                } else {
                    const row = document.createElement('tr');
                    const cell = document.createElement('td');
                    cell.colSpan = 2; // colspan="2"
                    cell.style.textAlign = 'center';
                    cell.style.color = '#666';
                    cell.textContent = 'No running tasks';
                    row.appendChild(cell);
                    tbody.appendChild(row);
                }

            } catch (error) {
                console.error('Refresh failed:', error);
                // Get tbody
                const tbody = document.querySelector('#tasksTable tbody');
                const row = document.createElement('tr');
                const cell = document.createElement('td');
                cell.colSpan = 2;
                cell.style.color = '#f44336';
                cell.textContent = 'Failed to load tasks';
                row.appendChild(cell);
                tbody.appendChild(row);
            }
        }

        // Auto-refresh every 10 seconds
        setInterval(refreshTasks, 10000);

        // ===== UTILITY FUNCTIONS =====
        function showStatus(message, type) {
            const statusDiv = document.getElementById('statusMessage');
            statusDiv.textContent = message;
            statusDiv.className = 'status-message show ' + type;
            
            if (type !== 'loading') {
                setTimeout(() => {
                    statusDiv.classList.remove('show');
                }, 8000);
            }
        }

        // Initialize on page load
        document.getElementById('folderName').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                fetchTestCases();
            }
        });

        document.getElementById('createTestcaseId').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                switchTab('create');
            }
        });

        // Close modal on outside click
        window.addEventListener('click', function(e) {
            if (e.target.classList.contains('modal')) {
                e.target.classList.remove('show');
            }
        });
    </script>
</body>
</html>
