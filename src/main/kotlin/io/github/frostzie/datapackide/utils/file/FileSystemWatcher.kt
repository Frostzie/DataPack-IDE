package io.github.frostzie.datapackide.utils.file

import io.github.frostzie.datapackide.events.DirectorySelected
import io.github.frostzie.datapackide.events.EventBus
import io.github.frostzie.datapackide.utils.LoggerProvider
import java.nio.file.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class FileSystemWatcher(private val watchPath: Path) {
    private val logger = LoggerProvider.getLogger("FileSystemWatcher")
    private var watchService: WatchService? = null
    private var watchThread: Thread? = null
    private var debounceExecutor: ScheduledExecutorService? = null
    private var scheduledUpdate: ScheduledFuture<*>? = null
    @Volatile
    private var lastEventTime = 0L
    private val debounceDelayMs = 500L
    @Volatile
    private var isWindowFocused = true
    @Volatile
    private var pendingUpdate = false

    // Executor dedicated to offloading directory-checks and registration so the watch thread is never blocked.
    private var registerExecutor: ExecutorService? = null
    // Lock to ensure registerRecursive's registration operations are thread-safe (reentrant on the same thread).
    private val registerLock = Any()

    fun start() {
        stop()

        try {
            debounceExecutor = Executors.newSingleThreadScheduledExecutor()
            registerExecutor = Executors.newSingleThreadExecutor()
            watchService = FileSystems.getDefault().newWatchService()
            // Initial registration can remain synchronous; it will acquire the same lock.
            registerRecursive(watchPath)

            watchThread = thread(name = "FileSystemWatcher") {
                logger.debug("Started watching directory: {}", watchPath)
                pollEvents()
            }
        } catch (e: Exception) {
            logger.error("Failed to start file system watcher", e)
        }
    }

    fun stop() {
        watchThread?.interrupt()
        watchThread?.join(5000) // Wait up to 5 seconds for thread to terminate
        watchThread = null
        scheduledUpdate?.cancel(false)
        debounceExecutor?.shutdown()
        try {
            if (debounceExecutor?.awaitTermination(5, TimeUnit.SECONDS) == false) {
                debounceExecutor?.shutdownNow()
            }
        } catch (ie: InterruptedException) {
            debounceExecutor?.shutdownNow()
            Thread.currentThread().interrupt()
        }

        // Shutdown the register executor cleanly
        registerExecutor?.shutdown()
        try {
            registerExecutor?.awaitTermination(5, TimeUnit.SECONDS)?.let {
                if (!it) {
                    registerExecutor?.shutdownNow()
                }
            }
        } catch (ie: InterruptedException) {
            registerExecutor?.shutdownNow()
            Thread.currentThread().interrupt()
        }

        watchService?.close()
        watchService = null
        logger.info("Stopped file system watcher")
    }

    fun setWindowFocused(focused: Boolean) {
        isWindowFocused = focused
        if (focused && pendingUpdate) {
            try {
                triggerUpdate()
            } finally {
                pendingUpdate = false
            }
        }
    }

    private fun registerRecursive(path: Path) {
        // Ensure only one thread performs registration operations at a time.
        synchronized(registerLock) {
            if (!Files.isDirectory(path)) return
            val service = watchService ?: return

            try {
                path.register(
                    service,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )

                Files.walk(path, 1).use { stream ->
                    stream.filter { Files.isDirectory(it) && it != path }
                        .forEach { registerRecursive(it) }
                }
            } catch (e: Exception) {
                logger.warn("Failed to register watch for: $path", e)
            }
        }
    }

    private fun pollEvents() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                val key = watchService?.poll(100, TimeUnit.MILLISECONDS) ?: continue

                for (event in key.pollEvents()) {
                    val kind = event.kind()

                    if (kind == StandardWatchEventKinds.OVERFLOW) continue

                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val context = ev.context()
                    val dir = key.watchable() as Path
                    val child = dir.resolve(context)

                    logger.debug("File system event: {} - {}", kind, child)

                    // Re-register if a new directory was created.
                    // Do the potentially blocking Files.isDirectory(...) and registration off the watch thread.
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            val pathToCheck = child
                            registerExecutor?.submit {
                                try {
                                    if (Files.isDirectory(pathToCheck)) {
                                        // registerRecursive is already synchronized internally
                                        registerRecursive(pathToCheck)
                                    }
                                } catch (e: Exception) {
                                    logger.warn("Failed to check/register created path: $pathToCheck", e)
                                }
                            }
                        } catch (ree: RejectedExecutionException) {
                            logger.warn("Register executor rejected task for path: $child", ree)
                        }
                    }

                    if (!isWindowFocused) {
                        scheduleUpdate()
                    }
                }

                if (!key.reset()) {
                    logger.warn("Watch key no longer valid, continuing to watch other directories")
                    continue
                }
            } catch (e: InterruptedException) {
                break
            } catch (e: Exception) {
                logger.error("Error polling file system events", e)
            }
        }
    }

    private fun scheduleUpdate() {
        scheduledUpdate?.cancel(false)
        scheduledUpdate = debounceExecutor?.schedule({
            if (isWindowFocused) {
                triggerUpdate()
            } else {
                pendingUpdate = true
            }
        }, debounceDelayMs, TimeUnit.MILLISECONDS)
    }

    private fun triggerUpdate() {
        logger.info("Triggering directory refresh due to file system changes")
        EventBus.post(DirectorySelected(watchPath))
    }
}