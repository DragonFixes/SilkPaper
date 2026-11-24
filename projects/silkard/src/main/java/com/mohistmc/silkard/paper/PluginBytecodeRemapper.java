package com.mohistmc.silkard.paper;

import java.nio.charset.StandardCharsets;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Rewrites plugin bytecode compiled against Paper's server internals where SilkPaper's differ
 * only in a member's declared type, which the JVM treats as a different member.
 *
 * <p>Paper declares {@code ServerLevel.serverLevelData} as {@code PaperLevelOverrides}; SilkPaper
 * keeps vanilla's {@code ServerLevelData} field (which holds the same {@code PaperLevelOverrides}
 * object for every dimension of the main save). A read of Paper's field becomes a read of
 * vanilla's followed by a cast, which leaves the stack exactly as the plugin expects.</p>
 */
public final class PluginBytecodeRemapper {

    private static final String SERVER_LEVEL = "net/minecraft/server/level/ServerLevel";
    private static final String LEVEL_OVERRIDES = "io/papermc/paper/world/saveddata/PaperLevelOverrides";
    private static final String PAPER_LEVEL_DATA_DESC = "L" + LEVEL_OVERRIDES + ";";
    private static final String VANILLA_LEVEL_DATA_DESC = "Lnet/minecraft/world/level/storage/ServerLevelData;";
    private static final byte[] MARKER = LEVEL_OVERRIDES.getBytes(StandardCharsets.UTF_8);

    private PluginBytecodeRemapper() {
    }

    public static byte[] remap(byte[] bytecode) {
        if (!contains(bytecode, MARKER)) {
            return bytecode;
        }
        ClassReader reader = new ClassReader(bytecode);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDescriptor) {
                        if (opcode == Opcodes.GETFIELD && SERVER_LEVEL.equals(owner) && "serverLevelData".equals(fieldName)
                            && PAPER_LEVEL_DATA_DESC.equals(fieldDescriptor)) {
                            super.visitFieldInsn(opcode, owner, fieldName, VANILLA_LEVEL_DATA_DESC);
                            super.visitTypeInsn(Opcodes.CHECKCAST, LEVEL_OVERRIDES);
                            return;
                        }
                        super.visitFieldInsn(opcode, owner, fieldName, fieldDescriptor);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static boolean contains(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0, last = haystack.length - needle.length; i <= last; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
