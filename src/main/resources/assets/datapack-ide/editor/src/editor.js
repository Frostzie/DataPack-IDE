import { EditorView, Decoration, tooltips, hoverTooltip } from "@codemirror/view"
import { autocompletion } from "@codemirror/autocomplete"
import { StateField } from "@codemirror/state"
import * as core from '@spyglassmc/core'
import { FileNode } from '@spyglassmc/core'
import { BrowserExternals } from '@spyglassmc/core/lib/browser.js'
import * as je from '@spyglassmc/java-edition'
import * as mcdoc from '@spyglassmc/mcdoc'

async function getSpyglassExtensions(initialContentPerma) {
    console.log('Initializing Spyglass...')

    // use the incoming initial content (from the main editor)
    const initialContent = typeof initialContentPerma === 'string' ? initialContentPerma : 'execute as @a run say hello world'

    const currentUri = 'file:///root/example.mcfunction'
    const currentLanguage = 'mcfunction'
    let version = 0

    const getContent = (state) => state.doc.toString()

    const patchedExternals = { ...BrowserExternals }
    try {
        const hasGlobalFetch = typeof globalThis !== 'undefined' && typeof globalThis.fetch === 'function'
        const boundGlobalFetch = hasGlobalFetch ? globalThis.fetch.bind(globalThis) : null

        if (boundGlobalFetch) {
            // Patch top-level fetch if used.
            patchedExternals.fetch = (...args) => boundGlobalFetch(...args)

            // Patch nested web.fetch (Spyglass internals may call BrowserExternals.web.fetch).
            if (patchedExternals.web && typeof patchedExternals.web === 'object') {
                patchedExternals.web = { ...patchedExternals.web, fetch: (...args) => boundGlobalFetch(...args) }
            }
        }
    } catch (e) {
        console.warn('spyglass failed to create patched externals, using original', e)
    }

    const service = new core.Service({
        project: {
            cacheRoot: 'file:///.cache/',
            defaultConfig: core.ConfigService.merge(core.VanillaConfig, {env: { dependencies: [] } }),
            externals: patchedExternals,
            initializers: [mcdoc.initialize, je.initialize],
            projectRoots: ['file:///root/'],
        },
    })

    await service.project.ready()

    await service.project.onDidOpen(currentUri, currentLanguage, version, initialContent)

    const onChange = EditorView.updateListener.of((update) => {
	    if (!update.docChanged) return
        const content = getContent(update.state)
        service.project.onDidChange(currentUri, [{ text: content }], ++version)
            .catch((e) => console.error('[onChange]', e))
    })

    async function spyglassCompletions(ctx) {
        try {
            const docAndNodes = await service.project.ensureClientManagedChecked(currentUri)
            if (!docAndNodes) return null

            const items = service.complete(docAndNodes.node, docAndNodes.doc, ctx.pos)
            if (!items || !items.length) return null

            return {
                from: items[0].range.start,
                to: items[0].range.end,
                options: items.map(v => ({
                    label: v.label,
                    detail: v.detail,
                    info: v.documentation
                })),
            }
        } catch (e) {
            console.error('[spyglassCompletions]', e)
            return null
        }
    }

    const getDiagnosticMark = (err) => {
        return Decoration.mark({
            attributes: { 'data-diagnostic-message': err.message ?? String(err) },
            class: `spyglassmc-diagnostic spyglassmc-diagnostic-${err.severity ?? 0}`,
        })
    }

    const diagnosticField = StateField.define({
        create() {
            return Decoration.none
        },
        update(decorations, tr) {
            const docAndNode = service.project.getClientManaged(currentUri)
            if (!docAndNode) return decorations

            const { node } = docAndNode
            let underlines = Decoration.none
            for (const e of FileNode.getErrors(node)) {
                underlines = underlines.update({
                    add: [
                        getDiagnosticMark(e).range(
                            e.range.start,
                            e.range.end === e.range.start ? e.range.start + 1 : e.range.end,
                        )
                    ]
                })
            }
            return underlines
        },
        provide: (f) => EditorView.decorations.from(f)
    })

    const colorTokenField = StateField.define({
        create() {
            return Decoration.none
        },
        update(underlines, tr) {
            const docAndNode = service.project.getClientManaged(currentUri)
            if (!docAndNode) {
                return underlines
            }
            const { node, doc } = docAndNode
            const tokens = service.colorize(node, doc)
            let out = Decoration.none
            for (const t of tokens) {
                if (t.range.start === t.range.end) continue
                out = out.update({
                    add: [getColorTokenMark(t).range(t.range.start, t.range.end)]
                })
            }
            return out
        },
        provide: f => EditorView.decorations.from(f)
    })

    const getColorTokenMark = t => {
        return Decoration.mark({
            class: `spyglassmc-color-token-${t.type} ${t.modifiers
                ?.map(m => `spyglassmc-color-token-modifier-${m}`)
            .join('') ?? ""}`
        })
    }

    const diagnosticTheme = EditorView.baseTheme(window.editorThemes?.diagnostic || {})
    const colorTokenTheme = EditorView.baseTheme(window.editorThemes?.colorToken || {})

    window.spyglassService = service

    return [
        autocompletion({ override: [spyglassCompletions] }),
        tooltips(),
        hoverTooltip((view, pos) => {
            let foundTooltip = null
            view.state.field(diagnosticField).between(pos, pos, (from, to, deco) => {
                const msg = deco.spec.attributes['data-diagnostic-message']
                if (msg) {
                    foundTooltip = {
                        pos: from,
                        end: to,
                        create() {
                            const dom = document.createElement('div')
                            dom.className = 'cm-tooltip-diagnostic'
                            dom.textContent = msg
                            return { dom }
                        },
                    }
                    return false
                }
            })
            return foundTooltip
        }),
        onChange,
        diagnosticField,
        diagnosticTheme,
        colorTokenField,
        colorTokenTheme,
    ]
}

// This is an IIFE (Immediately Invoked Function Expression).
// Runs as soon as the script is loaded.
(async () => {
    // Wait for the main editor to be ready.
    await new Promise(resolve => setTimeout(resolve, 100));

    const editor = window.datapackEditor;

    // Only proceed if the main editor created the shared object.
    if (editor && editor.view && editor.spyglassCompartment) {
        try {
            const initialContent = editor.view.state.doc.toString();
            const spyglassExtensions = await getSpyglassExtensions(initialContent);

            // Replace the basic autocompletion with the full Spyglass extension pack.
            editor.view.dispatch({
                effects: editor.spyglassCompartment.reconfigure(spyglassExtensions)
            });
            console.log("Spyglass extensions loaded successfully.");
        } catch (e) {
            console.error("Failed to load Spyglass extensions:", e);
        }
    } else {
        console.warn("Datapack editor instance not found. Spyglass extensions not loaded.");
    }
})();