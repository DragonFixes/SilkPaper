package com.mohistmc.silkard.paper.paper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jspecify.annotations.Nullable;

/**
 * Evaluates Purpur's configurable JavaScript expressions, which Purpur runs on a script engine kept on
 * {@code Entity}. A JavaScript engine is only available when one is installed on the server's class path.
 */
public final class PurpurScripts {

    private static @Nullable ScriptEngine engine;
    private static boolean engineLookedUp;

    private PurpurScripts() {
    }

    /**
     * Evaluates {@code expression} with {@code difficulty} bound, as Purpur's skeleton bow accuracy does.
     *
     * @throws ScriptException if the expression fails, or no JavaScript engine is installed
     */
    public static synchronized Object evalWithDifficulty(final String expression, final int difficulty) throws ScriptException {
        if (!engineLookedUp) {
            engineLookedUp = true;
            engine = new ScriptEngineManager().getEngineByName("js");
        }
        if (engine == null) {
            throw new ScriptException("No JavaScript engine is installed to evaluate: " + expression);
        }
        return engine.eval("let difficulty = " + difficulty + "; " + expression);
    }
}
