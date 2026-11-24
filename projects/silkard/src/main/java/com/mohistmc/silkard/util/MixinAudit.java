package com.mohistmc.silkard.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * Loads the target class of every SilkPaper mixin, so each mixin is applied and its injections are
 * checked, including mixins on classes a normal startup never loads. Each failing target is logged
 * as an ERROR and the audit moves on, so one run reports all of them. Run it with
 * {@code -Dsilkpaper.mixinAudit=true} ({@code scripts\boot-test -MixinAudit}).
 */
public final class MixinAudit {

    private static final Logger LOGGER = LogManager.getLogger("SilkPaper");
    private static final String MIXIN_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String[] CONFIGS = {
        "silkard.mixins.json",
        "silkard.paper.core.mixins.json",
        "silkard.paper.server.mixins.json",
        "silkard.paper.paper.mixins.json",
        "silkard.purpur.mixins.json"
    };

    private MixinAudit() {
    }

    public static void run() {
        ClassLoader loader = MixinAudit.class.getClassLoader();
        Set<String> targets = new LinkedHashSet<>();
        for (String config : CONFIGS) {
            try (InputStream in = loader.getResourceAsStream(config)) {
                if (in == null) {
                    LOGGER.error("Mixin audit: config {} not found", config);
                    continue;
                }
                JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                String pkg = json.get("package").getAsString();
                for (String key : new String[] {"mixins", "server"}) {
                    JsonArray mixins = json.getAsJsonArray(key);
                    if (mixins == null) continue;
                    for (JsonElement mixin : mixins) {
                        targets.addAll(targetsOf(loader, pkg + "." + mixin.getAsString()));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Mixin audit: could not read {}", config, e);
            }
        }

        int failed = 0;
        for (String target : targets) {
            try {
                Class.forName(target, false, loader);
            } catch (Throwable t) {
                failed++;
                Throwable root = t;
                while (root.getCause() != null) root = root.getCause();
                LOGGER.error("Mixin audit: {} failed: {}", target, root.getMessage());
            }
        }
        LOGGER.info("Mixin audit checked {} target classes, {} failed", targets.size(), failed);
    }

    private static List<String> targetsOf(ClassLoader loader, String mixinClass) throws Exception {
        List<String> result = new ArrayList<>();
        try (InputStream in = loader.getResourceAsStream(mixinClass.replace('.', '/') + ".class")) {
            if (in == null) {
                LOGGER.error("Mixin audit: mixin class {} not found", mixinClass);
                return result;
            }
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            List<AnnotationNode> annotations = new ArrayList<>();
            if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);
            if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
            for (AnnotationNode annotation : annotations) {
                if (!MIXIN_DESC.equals(annotation.desc) || annotation.values == null) continue;
                for (int i = 0; i < annotation.values.size(); i += 2) {
                    String name = (String) annotation.values.get(i);
                    Object value = annotation.values.get(i + 1);
                    if (!(value instanceof List<?> list)) continue;
                    for (Object entry : list) {
                        if ("value".equals(name) && entry instanceof Type type) {
                            result.add(type.getClassName());
                        } else if ("targets".equals(name) && entry instanceof String string) {
                            result.add(string.replace('/', '.'));
                        }
                    }
                }
            }
        }
        return result;
    }
}
