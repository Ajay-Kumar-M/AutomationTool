// ===== GLOBAL VARIABLES =====
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
    console.log("addOperation action : "+action+" - mode : "+mode);
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

function recordTestcase() {
    const testcaseId = document.getElementById('createTestcaseId').value.trim();

    if (!testcaseId) {
        showStatus('Please enter a Test Case ID', 'error');
        return;
    }

    // Open save dialog
    openModal('recordModal');
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

function confirmRecordDetails() {
    const fileName = document.getElementById('recordFileName').value.trim();
    const location = document.getElementById('recordSaveLocation').value.trim();
    const testcaseId = document.getElementById('createTestcaseId').value.trim();
    const epic = document.getElementById('epic').value.trim();
    const feature = document.getElementById('feature').value.trim();
    const story = document.getElementById('story').value.trim();
    const description = document.getElementById('description').value.trim();
    const webpageUrl = document.getElementById('webpageUrl').value.trim();
    if (!fileName || !location || !testcaseId || !epic || !feature || !story || !description || !webpageUrl) {
        showStatus('Please fill all the details 111111111', 'error');
        return;
    }
    // Send to backend
    recordTestcaseServer(fileName, location, testcaseId, epic, feature, story, description, webpageUrl);
    closeModal('recordModal');
}

async function recordTestcaseServer(fileName, location, testcaseId, epic, feature, story, description, webpageUrl) {
    showStatus('Record test case...', 'loading');
    showLoading();
    try {
        const response = await fetch('TestCaseRecordServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                webpageUrl: webpageUrl,
                fileName: fileName,
                location: location,
                testcaseId: testcaseId,
                epic: epic,
                feature: feature,
                story: story,
                description: description
            })
        });

        const result = await response.json();

        if (result.success) {
            showStatus('✓ Test case recorded at '+location+' successfully!', 'success');
            // Reset form
            clearEditorTab('create');
            // Clear modal
            document.getElementById('recordFileName').value = '';
            document.getElementById('recordSaveLocation').value = '';
            document.getElementById('createTestcaseId').value = '';
            document.getElementById('epic').value = '';
            document.getElementById('feature').value = '';
            document.getElementById('story').value = '';
            document.getElementById('description').value = '';
        } else {
        console.log(result.message);
            showStatus(result.message || 'Failed to record test case', 'error');
        }
        hideLoading();
    } catch (error) {
        hideLoading();
        console.error('Error:', error);
        showStatus('Error recording test case: ' + error.message, 'error');
    }
}

function showLoading() {
  const overlay = document.getElementById("loadingOverlay");
  overlay.style.display = "flex";
  overlay.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden";
}

function hideLoading() {
  const overlay = document.getElementById("loadingOverlay");
  overlay.style.display = "none";
  overlay.setAttribute("aria-hidden", "true");
  document.body.style.overflow = "";
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
//    console.log("editAddOperation action : "+action+" - mode : "+mode);
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
//        console.log(JSON.stringify(result));

        if (result.length > 0) {
            // Render table rows
            result.forEach(item => {
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