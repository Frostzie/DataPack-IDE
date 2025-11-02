import { EditorView, keymap, highlightActiveLine, highlightActiveLineGutter, lineNumbers, dropCursor, drawSelection } from "@codemirror/view"
import { foldGutter, foldKeymap } from "@codemirror/language"
import { defaultKeymap, history, historyKeymap, indentWithTab } from "@codemirror/commands"
import { searchKeymap, highlightSelectionMatches } from "@codemirror/search"
import { autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap } from "@codemirror/autocomplete"
import { EditorState, Compartment } from "@codemirror/state"
import { lintGutter } from "@codemirror/lint"
import { barf } from 'thememirror'; // TEMP

const initialContent = ''

function initializeEditor() {
    const container = document.getElementById('editor-container')

    const spyglassCompartment = new Compartment()

    const extensions = [
        barf, // TEMP
        drawSelection(),
        lineNumbers(),
        foldGutter(),
        lintGutter(),
        closeBrackets(),
        history(),
        highlightActiveLine(),
        highlightActiveLineGutter(),
        highlightSelectionMatches(),
        dropCursor(),
        keymap.of([
            ...closeBracketsKeymap,
            ...defaultKeymap,
            ...searchKeymap,
            ...historyKeymap,
            ...foldKeymap,
            ...completionKeymap,
            indentWithTab,
        ]),
        spyglassCompartment.of([autocompletion()])
    ];

    let view = new EditorView({
        parent: container,
        state: EditorState.create({
            doc: initialContent,
            extensions: extensions
        }),
    })

    // Provide safe defaults for editor themes so the optional spyglass module can use them.
    window.editorThemes = window.editorThemes || { diagnostic: {}, colorToken: {} }

    // Expose the editor instance and compartment to the window so that editor.js can find it
    window.datapackEditor = { view, spyglassCompartment }

    // Function to load content with fresh state
    window.loadFileContent = function(content) {
        if (!view) {
            console.error('Editor view not available');
            return false;
        }
        if (typeof content !== 'string') {
            console.error('Invalid content: must be a string');
            return false;
        }

        const newState = EditorState.create({
            doc: content,
            extensions: extensions
        });

        view.setState(newState);
        console.log('File content loaded with fresh state:', content.length, 'characters');
        return true;
    };

    console.log('Editor initialized successfully')

    // Notify Kotlin that editor is ready
    if (window.kotlinBridge) {
        try {
            window.kotlinBridge.editorReady()
        } catch (error) {
            console.error('Failed to notify Kotlin of editor ready:', error)
        }
    }
}

try {
    initializeEditor()
} catch (error) {
    console.error('Failed to initialize editor:', error)
    document.body.innerHTML = `<h1>Error</h1><pre>${error.message}</pre>`
}