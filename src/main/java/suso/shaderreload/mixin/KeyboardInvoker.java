package suso.shaderreload.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Keyboard.class) @Environment(EnvType.CLIENT)
public interface KeyboardInvoker {
    @Invoker("debugLog")
    void debugLog(Text text);
}
