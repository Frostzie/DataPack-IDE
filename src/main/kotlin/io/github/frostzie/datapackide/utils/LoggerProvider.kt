package io.github.frostzie.datapackide.utils

import javafx.scene.web.WebView
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private const val MAIN = "datapack-ide"
    private val loggers = mutableMapOf<String, Logger>()

    fun getLogger(name: String): Logger {
        val fullName = "$MAIN:$name"
        return loggers.getOrPut(fullName) {
            LoggerFactory.getLogger(fullName)
        }
    }

    /**
     * Sets up JavaScript console logging to redirect to logger
     */
    fun setupConsoleLogging(webView: WebView) {
        val logger = getLogger("WebView")
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
}