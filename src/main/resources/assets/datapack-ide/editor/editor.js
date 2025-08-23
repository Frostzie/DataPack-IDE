console.log('DataPack IDE Editor starting...');

let view;
let currentLanguage = 'plaintext';
let currentUri = 'file:///root/untitled.txt';

const initialContent = '';
const DEBUG = true;

function log(...args) {
    if (DEBUG) console.log(...args);
}

function safeJavaCall(method, ...args) {
    if (window.javaConnector && typeof window.javaConnector[method] === 'function') {
        window.javaConnector[method](...args);
    } else {
        log(`Java connector method ${method} not available, using temporary fallback.`);
        // Temporary fallback simulation for testing without Java connector
        if (method === 'onEditorReady') {
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

        const { basicSetup, EditorState, EditorView } = await import('https://cdn.jsdelivr.net/npm/@codemirror/basic-setup@0.20.0/+esm');
        const { indentWithTab } = await import('https://cdn.jsdelivr.net/npm/@codemirror/commands@0.20.0/+esm');
        const { keymap } = await import('https://cdn.jsdelivr.net/npm/@codemirror/view@0.20.6/+esm');

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
            safeJavaCall('onContentChanged', content);
        });

        const sizeTheme = EditorView.theme({
            '&': { height: '100vh' },
            '.cm-gutter,.cm-content': { minHeight: '100vh' },
            '.cm-scroller': { overflow: 'auto' },
        });

        view = new EditorView({
            parent: container,
            state: EditorState.create({
                doc: initialContent,
                extensions: [
                    basicSetup,
                    keymap.of([indentWithTab]),
                    onChange,
                    sizeTheme,
                ],
            }),
        });

        log('CodeMirror view created successfully');

        window.editorAPI = {
            setText(content, uri) {
                if (!view) return;
                log('CodeMirror: setText called with', content?.length || 0, 'characters');
                if (uri && uri !== currentUri) {
                    currentUri = uri;
                }
                view.dispatch({
                    changes: {
                        from: 0,
                        to: view.state.doc.length,
                        insert: content || ''
                    }
                });
            },

            getText() {
                return view ? view.state.doc.toString() : '';
            },

            setLanguage(language) {
                if (language !== currentLanguage) {
                    currentLanguage = language;
                    log('CodeMirror: setLanguage', language);
                    if (!window.javaConnector) {
                        log('Simulated: Pretending language is json');
                        currentLanguage = 'json';
                    }
                }
            },

            insertText(text) {
                if (!view) return;
                const pos = view.state.selection.main.head;
                view.dispatch({
                    changes: { from: pos, insert: text }
                });
            },

            getSelectedText() {
                if (!view) return '';
                const selection = view.state.selection.main;
                return view.state.doc.sliceString(selection.from, selection.to);
            },

            selectAll() {
                if (!view) return;
                view.dispatch({
                    selection: { anchor: 0, head: view.state.doc.length }
                });
            },

            focus() {
                if (view) view.focus();
            },

            dispose() {
                if (view) {
                    view.destroy();
                    view = null;
                    log('Editor view disposed');
                }
            }
        };

        safeJavaCall('onEditorReady');
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

window.getEditorAPI = () => window.editorAPI;
window.getView = () => view;