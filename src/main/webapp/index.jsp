<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*,java.util.Map, java.util.concurrent.Future" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Automation Tool - Test Case Runner & Editor</title>
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
                    <button class="btn btn-success" onclick="recordTestcase()">
                       <span style="font-size: 10px;">&#128308;</span> Record Testcase
                    </button>
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

    <!-- Record Testcase Dialog Modal -->
    <div id="recordModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Record Test Case</h2>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label>Webpage URL:</label>
                    <input type="text" id="webpageUrl" class="input-field" placeholder="e.g., https://google.com" />
                </div>
                <div class="form-group">
                    <label>File Name:</label>
                    <input type="text" id="recordFileName" class="input-field" placeholder="e.g., TC001.json" />
                </div>
                <div class="form-group">
                    <label>Record Location:</label>
                    <input type="text" id="recordSaveLocation" class="input-field" placeholder="e.g., /test/cases" />
                </div>
                <div class="form-group">
                    <label>Epic:</label>
                    <input type="text" id="epic" class="input-field" placeholder="e.g., Product check" />
                </div>
                <div class="form-group">
                    <label>Feature:</label>
                    <input type="text" id="feature" class="input-field" placeholder="e.g., Login" />
                </div>
                <div class="form-group">
                    <label>Story:</label>
                    <input type="text" id="story" class="input-field" placeholder="e.g., Validate Login" />
                </div>
                <div class="form-group">
                    <label>Description:</label>
                    <input type="text" id="description" class="input-field" placeholder="e.g., Validate Login with invalid credentials" />
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('recordModal')">Cancel</button>
                <button class="btn btn-primary" onclick="confirmRecordDetails()">Record</button>
            </div>
        </div>
    </div>

    <div id="loadingOverlay" aria-hidden="true">
      <div class="loader-card">
        <div class="spinner"></div>
        <div class="loading-text">Loading…</div>
      </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/index.js"></script>
</body>
</html>
