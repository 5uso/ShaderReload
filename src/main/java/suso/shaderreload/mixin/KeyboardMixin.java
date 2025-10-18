package suso.shaderreload.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import suso.shaderreload.ShaderReload;

@Mixin(Keyboard.class) @Environment(EnvType.CLIENT)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "processF3", at = @At("RETURN"), cancellable = true)
    void onProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == ShaderReload.GLFW_KEY) {
            cir.setReturnValue(true);
        } else if (key == GLFW.GLFW_KEY_Q) {
            client.inGameHud.getChatHud().addMessage(Text.translatable("debug.reload_shaders.help"));
        }
    }

    @Inject(method = "onKey", at = @At(value = "HEAD"))
    void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) || key != ShaderReload.GLFW_KEY) return;
        if (action != 0) {
            ShaderReload.reloadShaders();
        }
    }

    @Inject(method = "onChar", at = @At(value = "HEAD"), cancellable = true)
    void onOnChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) && InputUtil.isKeyPressed(window, ShaderReload.GLFW_KEY)) {
            ci.cancel();
        }
    }
}
