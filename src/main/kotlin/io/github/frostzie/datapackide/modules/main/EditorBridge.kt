package io.github.frostzie.datapackide.modules.main

import io.github.frostzie.datapackide.utils.LoggerProvider
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * Bridge between Kotlin and JavaScript for the CodeMirror editor.
 * Provides bidirectional communication for editor operations.
 */
class EditorBridge(
    private val webView: WebView,
    private val filePath: Path,
    private val initialContent: String,
    private val onContentSaved: () -> Unit = {}
) {
    private val logger = LoggerProvider.getLogger("EditorBridge")
    private var isInitialized = false

    /**
     * Initializes the bridge by injecting the Kotlin handler into JavaScript context.
     * This is called automatically and sets up console logging and the bridge.
     */
    fun initialize() {
        webView.engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED && !isInitialized) {
                try {
                    val window = webView.engine.executeScript("window") as JSObject
                    window.setMember("kotlinBridge", KotlinHandler())

                    // Setup console logging interception
                    setupConsoleLogging()
                    // Disable context menu
                    webView.isContextMenuEnabled = false

                    isInitialized = true
                    logger.debug("Bridge initialized for file: {}, waiting for editor ready callback", filePath.fileName)
                } catch (e: Exception) {
                    logger.error("Failed to initialize bridge for ${filePath.fileName}", e)
                }
            }
        }
    }

    /**
     * This will be called from JavaScript when the editor is ready.
     */
    private fun onEditorReady() {
        try {
            loadContent(initialContent)
            logger.debug("Editor ready callback received, content loaded")
        } catch (e: Exception) {
            logger.error("Error loading content after editor ready", e)
        }
    }

    //TODO: Move this away from here
    /**
     * Sets up JavaScript console logging to redirect to logger
     */
    private fun setupConsoleLogging() {
        webView.engine.executeScript(
            """
            (function() {
                const originalLog = console.log;
                const originalError = console.error;
                const originalWarn = console.warn;
                const originalInfo = console.info;

                function formatArgs(args) {
                    return args.map(arg => {
                        if (arg instanceof Error) {
                            return arg.stack || arg.message;
                        }
                        if (typeof arg === 'object' && arg !== null) {
                            try { return JSON.stringify(arg, null, 2); } catch (e) { return '[Unserializable Object]'; }
                        }
                        return String(arg);
                    }).join(' ');
                }

                console.log = function(...args) { 
                    if (window.kotlinBridge) kotlinBridge.consoleLog(formatArgs(args)); 
                    originalLog.apply(console, args); 
                };
                console.error = function(...args) { 
                    if (window.kotlinBridge) kotlinBridge.consoleError(formatArgs(args)); 
                    originalError.apply(console, args); 
                };
                console.warn = function(...args) { 
                    if (window.kotlinBridge) kotlinBridge.consoleWarn(formatArgs(args)); 
                    originalWarn.apply(console, args); 
                };
                console.info = function(...args) { 
                    if (window.kotlinBridge) kotlinBridge.consoleInfo(formatArgs(args)); 
                    originalInfo.apply(console, args); 
                };
            })();
            """.trimIndent()
        )
        logger.debug("Console logging setup completed")
    }

    /**
     * Loads file content into the CodeMirror editor.
     * @param content The text content to load
     */
    private fun loadContent(content: String) {
        try {
            val escapedContent = content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            val script = """
                if (window.datapackEditor && window.datapackEditor.view) {
                    const view = window.datapackEditor.view;
                    view.dispatch({
                        changes: {
                            from: 0,
                            to: view.state.doc.length,
                            insert: "$escapedContent"
                        }
                    });
                    console.log('Content loaded: ' + view.state.doc.length + ' characters');
                    true;
                } else {
                    console.error('Editor not available');
                    false;
                }
            """.trimIndent()

            val result = webView.engine.executeScript(script) as Boolean
            if (result) {
                logger.debug("Content loaded successfully: {} ({} characters)", filePath.fileName, content.length)
            } else {
                logger.error("Failed to load content - editor not available")
            }
        } catch (e: Exception) {
            logger.error("Failed to load content into editor for ${filePath.fileName}", e)
        }
    }

    /**
     * Retrieves the current content from the CodeMirror editor.
     * @return The current editor content as a string
     */
    private fun getContent(): String {
        return try {
            val content = webView.engine.executeScript(
                "window.datapackEditor && window.datapackEditor.view ? window.datapackEditor.view.state.doc.toString() : ''"
            ) as String
            content
        } catch (e: Exception) {
            logger.error("Failed to get content from editor for ${filePath.fileName}", e)
            ""
        }
    }

    /**
     * Saves the current editor content to the file.
     */
    fun saveFile() {
        try {
            val content = getContent()
            filePath.writeText(content)
            onContentSaved()
            logger.info("File saved: ${filePath.fileName} (${content.length} characters)")
        } catch (e: Exception) {
            logger.error("Failed to save file: ${filePath.fileName}", e)
            // TODO: Show error notification to user
        }
    }

    /**
     * Inner class that exposes Kotlin methods to JavaScript.
     * JavaScript can call these methods via window.kotlinBridge
     */
    inner class KotlinHandler {
        /**
         * Called from JavaScript when the editor is fully initialized and ready
         */
        fun editorReady() {
            Platform.runLater {
                this@EditorBridge.onEditorReady()
            }
        }

        /**
         * Called from JavaScript when a user presses Ctrl+S
         */
        fun saveFile() {
            this@EditorBridge.saveFile()
        }

        /**
         * Console.log from JavaScript
         */
        fun consoleLog(message: String) {
            logger.info("[JS Console] LOG: $message")
        }

        /**
         * Console.error from JavaScript
         */
        fun consoleError(message: String) {
            logger.error("[JS Console] ERROR: $message")
        }

        /**
         * Console.warn from JavaScript
         */
        fun consoleWarn(message: String) {
            logger.warn("[JS Console] WARN: $message")
        }

        /**
         * Console.info from JavaScript
         */
        fun consoleInfo(message: String) {
            logger.info("[JS Console] INFO: $message")
        }
    }
}