package suso.shaderreload.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Keyboard.class)
public interface KeyboardInvoker {
    @Invoker
    void invokeDebugLog(String key, Object ... args);

    @Invoker
    void invokeDebugError(String key, Object ... args);
}
