package com.mohistmc.silkard.paper.paper;

import com.google.common.collect.MapMaker;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.APICommandMeta;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.lang.reflect.Field;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Paper's Brigadier command API state for {@link CommandNode}s.
 *
 * <p>Paper adds fields and the {@code removeCommand}/{@code clearAll} methods to Brigadier's
 * {@code CommandNode} by patching the library. Brigadier is a game library rather than part of
 * the Minecraft jar, so here the extra state lives in a side table keyed by node identity
 * ({@code CommandNode} overrides {@code equals}/{@code hashCode} structurally, so an identity
 * map is required), and child removal goes through the node's own maps.
 */
public final class PaperCommandNodes {

    private static final class Extra {
        @Nullable CommandNode<CommandSourceStack> unwrappedCached;
        @Nullable CommandNode<CommandSourceStack> wrappedCached;
        @Nullable APICommandMeta apiCommandMeta;
    }

    private static final Map<CommandNode<?>, Extra> EXTRA = new MapMaker().weakKeys().concurrencyLevel(2).makeMap();

    private static final Field CHILDREN = field("children");
    private static final Field LITERALS = field("literals");
    private static final Field ARGUMENTS = field("arguments");

    private PaperCommandNodes() {
    }

    private static Field field(final String name) {
        try {
            final Field field = CommandNode.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Extra extra(final CommandNode<?> node) {
        return EXTRA.computeIfAbsent(node, ignored -> new Extra());
    }

    private static @Nullable Extra extraIfPresent(final CommandNode<?> node) {
        return EXTRA.get(node);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> map(final Field field, final CommandNode<?> node) {
        try {
            return (Map<String, ?>) field.get(node);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Removes the child named {@code name} from {@code node}. */
    public static void removeCommand(final CommandNode<?> node, final String name) {
        map(CHILDREN, node).remove(name);
        map(LITERALS, node).remove(name);
        map(ARGUMENTS, node).remove(name);
    }

    /** Removes every child of {@code node}. */
    public static void clearAll(final CommandNode<?> node) {
        map(CHILDREN, node).clear();
        map(LITERALS, node).clear();
        map(ARGUMENTS, node).clear();
    }

    public static @Nullable CommandNode<CommandSourceStack> unwrappedCached(final CommandNode<?> node) {
        final Extra extra = extraIfPresent(node);
        return extra == null ? null : extra.unwrappedCached;
    }

    public static void unwrappedCached(final CommandNode<?> node, final @Nullable CommandNode<CommandSourceStack> value) {
        extra(node).unwrappedCached = value;
    }

    public static @Nullable CommandNode<CommandSourceStack> wrappedCached(final CommandNode<?> node) {
        final Extra extra = extraIfPresent(node);
        return extra == null ? null : extra.wrappedCached;
    }

    public static void wrappedCached(final CommandNode<?> node, final @Nullable CommandNode<CommandSourceStack> value) {
        extra(node).wrappedCached = value;
    }

    public static @Nullable APICommandMeta apiCommandMeta(final CommandNode<?> node) {
        final Extra extra = extraIfPresent(node);
        return extra == null ? null : extra.apiCommandMeta;
    }

    public static void apiCommandMeta(final CommandNode<?> node, final @Nullable APICommandMeta value) {
        extra(node).apiCommandMeta = value;
    }
}
