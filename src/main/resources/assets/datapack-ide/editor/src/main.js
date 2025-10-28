import { EditorView, keymap, highlightActiveLine, highlightActiveLineGutter, lineNumbers, dropCursor, drawSelection } from "@codemirror/view"
import { foldGutter, foldKeymap } from "@codemirror/language"
import { defaultKeymap, history, historyKeymap, indentWithTab } from "@codemirror/commands"
import { searchKeymap, highlightSelectionMatches } from "@codemirror/search"
import { autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap } from "@codemirror/autocomplete"
import { EditorState, Compartment } from "@codemirror/state"
import { lintGutter } from "@codemirror/lint"

const initialContent = 'execute as @a run say'

function initializeEditor() {
    const container = document.getElementById('editor-container')

    const spyglassCompartment = new Compartment()

    const extensions = [
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

    const view = new EditorView({
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
}

try {
    initializeEditor()
} catch (error) {
    console.error('Failed to initialize editor:', error)
    document.body.innerHTML = `<h1>Error</h1><pre>${error.message}</pre>`
}
