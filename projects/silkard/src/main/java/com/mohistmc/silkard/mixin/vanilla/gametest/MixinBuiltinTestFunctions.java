package com.mohistmc.silkard.mixin.vanilla.gametest;

import com.mohistmc.silkard.gametest.SilkGameTests;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Registers SilkPaper's gameplay test functions while vanilla fills the TEST_FUNCTION registry,
 * the only point before that registry is frozen. Does nothing unless {@code -Dsilkpaper.gametest} is set.
 */
@Mixin(BuiltinTestFunctions.class)
public class MixinBuiltinTestFunctions {

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void silkpaper$registerGameTests(Registry<Consumer<GameTestHelper>> registry, CallbackInfoReturnable<Consumer<GameTestHelper>> cir) {
        SilkGameTests.registerFunctions();
    }
}
