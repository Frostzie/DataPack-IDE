console.log('DataPack IDE Editor starting...');

let view;
let currentUri = 'file:///root/untitled.txt';

let EditorState, basicSetup, indentWithTab, undo, redo, keymap, EditorView;
let editorExtensions;

const initialContent = '';
const DEBUG = true;

function log(...args) {
    if (DEBUG) console.log(...args);
}

function safeJavaCall(method, ...args) {
    if (window.javaConnector && typeof window.javaConnector[method] === 'function') {
        window.javaConnector[method](...args);
    } else {
        log(`Java connector or method ${method} not available, using fallback.`);
        // Temporary fallback simulation for testing without Java connector
        if (method === 'editorReady') {
            log('Simulated Java connector: Editor is ready.');
        } else if (method === 'onContentChanged') {
            log('Simulated Java connector: Content changed. Content length:', args[0]?.length || 0);
        }
    }
}

const getContent = (state) => state ? state.sliceDoc(0) : (view ? view.state.sliceDoc(0) : '');

async function initializeCodeMirror() {
    try {
        log('Loading CodeMirror modules...');

        // Dynamically import and assign to wider-scope variables
        const basicSetupModule = await import('https://cdn.jsdelivr.net/npm/@codemirror/basic-setup@0.20.0/+esm');
        EditorState = basicSetupModule.EditorState;
        EditorView = basicSetupModule.EditorView;
        basicSetup = basicSetupModule.basicSetup;

        const commandsModule = await import('https://cdn.jsdelivr.net/npm/@codemirror/commands@0.20.0/+esm');
        indentWithTab = commandsModule.indentWithTab;
        undo = commandsModule.undo;
        redo = commandsModule.redo;

        const viewModule = await import('https://cdn.jsdelivr.net/npm/@codemirror/view@0.20.6/+esm');
        keymap = viewModule.keymap;

        log('CodeMirror modules loaded successfully');

        const loadingMsg = document.getElementById('loading-message');
        const container = document.getElementById('editor-container');

        if (loadingMsg) {
            loadingMsg.remove();
        }
        container.classList.remove('hidden');

        const onChange = EditorView.updateListener.of((update) => {
            if (!update.docChanged) return;
            const content = getContent(update.state);
            safeJavaCall('contentChanged', content);
        });

        const onCursorChange = EditorView.updateListener.of((update) => {
            if (!update.selectionSet) return;
            const pos = update.state.selection.main.head;
            const line = update.state.doc.lineAt(pos);
            safeJavaCall('cursorPositionChanged', line.number, (pos - line.from) + 1);
        });

        const sizeTheme = EditorView.theme({
            '&': { height: '100vh' },
            '.cm-gutter,.cm-content': { minHeight: '100vh' },
            '.cm-scroller': { overflow: 'auto' },
        });

        editorExtensions = [
            basicSetup,
            keymap.of([indentWithTab]),
            onCursorChange,
            onChange,
            sizeTheme,
        ];

        view = new EditorView({
            parent: container,
            state: EditorState.create({
                doc: initialContent,
                extensions: editorExtensions,
            }),
        });

        log('CodeMirror view created successfully');

        window.editorSetContent = function(content, newUri) {
            if (!view) return;
            log('CodeMirror: editorSetContent called with', content?.length || 0, 'characters for URI:', newUri);

            const newState = EditorState.create({
                doc: content || '',
                extensions: editorExtensions
            });
            view.setState(newState);

            if (newUri) {
                currentUri = newUri;
            }
        };

        window.editorGetContent = function() {
            return view ? view.state.doc.toString() : '';
        };

        window.editorInsertText = function(text) {
            if (!view) return;
            const pos = view.state.selection.main.head;
            view.dispatch({
                changes: { from: pos, insert: text }
            });
        };

        window.editorGetSelectedText = function() {
            if (!view) return '';
            const selection = view.state.selection.main;
            return view.state.doc.sliceString(selection.from, selection.to);
        };

        window.editorCut = function() {
            document.execCommand('cut');
        };

        window.editorCopy = function() {
            document.execCommand('copy');
        };

        window.editorPaste = function() {
            document.execCommand('paste');
        };

        window.editorUndo = function() {
            if (view) undo(view);
        };

        window.editorRedo = function() {
            if (view) redo(view);
        };

        window.editorSelectAll = function() {
            if (!view) return;
            view.dispatch({
                selection: { anchor: 0, head: view.state.doc.length }
            });
        };

        window.editorFind = function(searchText) {
            // TODO: Implement find functionality. CodeMirror 6 has extensions for this.
            log("Find not implemented yet. Searched for:", searchText);
            return false;
        };

        window.editorSetEditable = function(editable) {
            if (!view) return;
            view.contentDOM.contentEditable = editable;
        };

        window.editorFocus = function() {
            if (view) view.focus();
        };

        window.disposeEditor = function() {
            if (view) {
                view.destroy();
                view = null;
                log('Editor view disposed');
            }
        };

        safeJavaCall('editorReady');
        log('CodeMirror initialization complete');

    } catch (error) {
        console.error('Failed to load CodeMirror:', error);
        throw error;
    }
}

if (document.readyState === 'loading') {
    const listener = async () => {
        document.removeEventListener('DOMContentLoaded', listener);
        await startEditor();
    };
    document.addEventListener('DOMContentLoaded', listener);
} else {
    startEditor();
}

async function startEditor() {
    log('Starting editor initialization...');

    try {
        await initializeCodeMirror();
    } catch (error) {
        console.error('Failed to initialize CodeMirror editor:', error);
        const loadingMessage = document.getElementById('loading-message');
        if (loadingMessage) {
            loadingMessage.textContent = 'Error: Failed to load the editor. Please check the console for details.';
            loadingMessage.style.color = 'red';
        }
    }
}