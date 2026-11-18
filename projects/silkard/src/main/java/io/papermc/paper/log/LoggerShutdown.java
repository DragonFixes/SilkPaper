package io.papermc.paper.log;

import org.apache.logging.log4j.LogManager;
import org.jline.utils.ShutdownHooks;

public final class LoggerShutdown {
    private LoggerShutdown() {
    }

    public static void shutdownLogging() {
        io.papermc.paper.log.CustomLogManager.forceReset(); // Reset loggers after shutdown
        LogManager.shutdown(); // Flushes the async appender

        // Call JLine's shutdown hooks now that all pending logs have been flushed
        runJLineShutdownHooks();

        try {
            // This may end up calling AbstractTerminal#close twice which doesn't have any guards for that, consider patching TCA if it becomes an issue
            net.minecrell.terminalconsole.TerminalConsoleAppender.close(); // Use TerminalConsoleAppender
        } catch (final Exception ignored) {
        }
    }

    // JLine keeps runTasks private (Paper patches the library to open it), so it is called reflectively.
    private static void runJLineShutdownHooks() {
        try {
            final java.lang.reflect.Method runTasks = ShutdownHooks.class.getDeclaredMethod("runTasks");
            runTasks.setAccessible(true);
            runTasks.invoke(null);
        } catch (final ReflectiveOperationException | RuntimeException ignored) {
        }
    }
}
