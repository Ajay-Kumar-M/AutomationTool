<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Automation Tool - Test Case Runner</title>
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
            --color-hover: #f5f8fc;
            --color-white: #ffffff;
            --radius-sm: 4px;
            --radius-md: 8px;
            --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.08);
            --shadow-md: 0 2px 8px rgba(0, 0, 0, 0.12);
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

        .action-buttons {
            display: flex;
            gap: 12px;
            justify-content: center;
            margin-top: 30px;
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

        @media (max-width: 600px) {
            .container {
                padding: 20px;
            }

            .input-group {
                flex-direction: column;
            }

            .header h1 {
                font-size: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🚀 Automation Tool</h1>
            <p>Test Case Runner</p>
        </div>

        <!-- Status Messages -->
        <div id="statusMessage" class="status-message"></div>

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
        <p class="tree-label">* Incase of selection of parent folder, all the testcases inside the folder are selected automatically !</p>
        <!-- Selection Count -->
        <div id="selectionCount" class="selected-count" style="display: none;">
            <span id="selectedCountText">0 items selected</span>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
            <button class="btn btn-secondary" id="clearBtn" onclick="clearSelection(true)" style="display: none;">
                Clear Selection
            </button>
            <button class="btn btn-primary" id="runBtn" onclick="runTestCases()" style="display: none;">
                ▶ Run Test Cases
            </button>
            <button class="btn btn-primary" id="editBtn" onclick="editTestCase()" style="display: none;">
                🖊 Edit Test Cases
            </button>
            <button class="btn btn-primary" id="createBtn" onclick="createTestCase()" style="display: inline-flex">
                ⊕ Create Test Cases
            </button>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>Automation Tool v1.0</p>
        </div>
    </div>

    <script>
        let treeData = null;
        let selectedItems = new Set();

        /**
         * Fetch test cases from the specified folder
         */
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

        /**
         * Render tree structure from fetched data
         */
        function renderTree(data) {
            const container = document.getElementById('treeContainer');
            container.innerHTML = '';
            
            if (!data || data.length === 0) {
                container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">❌</div><div>No test cases found in the specified folder</div></div>';
                return;
            }

            const ul = document.createElement('div');
            ul.className = 'tree-item';
            
            data.forEach((item, index) => {
                ul.appendChild(createTreeNode(item, index));
            });
            
            container.appendChild(ul);
        }

        /**
         * Create a tree node element recursively
         */
        function createTreeNode(item, index) {
            const nodeWrapper = document.createElement('div');
            nodeWrapper.className = 'tree-item';
            const nodeId = 'node-' + Date.now() + '-' + index;

            // Node header
            const nodeHeader = document.createElement('div');
            nodeHeader.className = 'tree-node';

            // Toggle button (for folders with children)
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

            // Checkbox
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = nodeId;
            checkbox.dataset.path = item.path;
            checkbox.dataset.isFolder = item.isFolder;
            checkbox.onchange = () => updateSelection(checkbox);
            nodeHeader.appendChild(checkbox);

            // Label with icon
            const label = document.createElement('label');
            label.htmlFor = nodeId;
            label.className = 'tree-label ' + (item.isFolder ? 'folder' : 'file');
            label.style.cursor = 'pointer';
            label.onclick = (e) => {
                e.stopPropagation();
                checkbox.click();
            };
            
            const icon = document.createElement('span');
            icon.className = 'tree-icon';
            icon.textContent = item.isFolder ? '📂' : '📄';
            label.appendChild(icon);
            label.appendChild(document.createTextNode(item.name));
            
            nodeHeader.appendChild(label);
            nodeWrapper.appendChild(nodeHeader);

            // Children (if any)
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

        /**
         * Toggle folder expansion
         */
        function toggleFolder(nodeId) {
            const childrenContainer = document.getElementById(nodeId + '-children');
            const toggleBtn = document.getElementById(nodeId).parentElement.querySelector('.tree-toggle');
            
            if (childrenContainer) {
                childrenContainer.classList.toggle('hidden');
                toggleBtn.classList.toggle('collapsed');
                toggleBtn.classList.toggle('expanded');
            }
        }

        /**
         * Update selection tracking
         */
        function updateSelection(checkbox) {
            const path = checkbox.dataset.path;
            
            if (checkbox.checked) {
                selectedItems.add(path);
            } else {
                selectedItems.delete(path);
            }
            
            updateSelectionUI();
        }

        /**
         * Update selection UI elements
         */
        function updateSelectionUI() {
            const count = selectedItems.size;
            const countDisplay = document.getElementById('selectionCount');
            const countText = document.getElementById('selectedCountText');
            const runBtn = document.getElementById('runBtn');
            const clearBtn = document.getElementById('clearBtn');
            const editBtn = document.getElementById('editBtn');
            const createBtn = document.getElementById('createBtn');

            if (count > 0) {
                countDisplay.style.display = 'block';
                countText.textContent = count + ' item' + (count !== 1 ? 's' : '') + ' selected';
                runBtn.style.display = 'inline-flex';
                clearBtn.style.display = 'inline-flex';
                createBtn.style.display = 'none';
                if((count==1)&&((selectedItems.values().next().value).endsWith(".json"))){
                    editBtn.style.display = 'inline-flex';
                } else {
                    editBtn.style.display = 'none';
                }
            } else {
                countDisplay.style.display = 'none';
                runBtn.style.display = 'none';
                clearBtn.style.display = 'none';
                editBtn.style.display = 'none';
                createBtn.style.display = 'inline-flex';
            }
        }

        /**
         * Clear all selections
         */
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

        /**
         * Run selected test cases
         */
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
                    showStatus('✓ Test cases executed successfully! ID: ' + result.executionId, 'success');
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


        /**
         * Edit selected test cases
         */
        async function editTestCase() {

        }

        /**
         * Create selected test cases
         */
        async function createTestCase() {

        }

        /**
         * Show status message
         */
        function showStatus(message, type) {
            const statusDiv = document.getElementById('statusMessage');
            statusDiv.textContent = message;
            statusDiv.className = 'status-message show ' + type;
            
            if (type !== 'loading') {
                setTimeout(() => {
                    statusDiv.classList.remove('show');
                }, 4000);
            }
        }

        // Allow Enter key to fetch
        document.getElementById('folderName').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                fetchTestCases();
            }
        });
    </script>
</body>
</html>
