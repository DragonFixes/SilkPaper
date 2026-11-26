package com.mohistmc.silkard.injected.network;

import com.mohistmc.silkard.util.ContextStateException;

/**
 * @author Mgazul
 * @date 2026/10/30
 */
public interface ContextDiscardedPayload {

    default byte[] data() {
        throw new ContextStateException("Not implemented");
    }

    default void silkard$data(byte[] data) {
        throw new ContextStateException("Not implemented");
    }
}
