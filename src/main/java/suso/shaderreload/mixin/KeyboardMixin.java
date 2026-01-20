package suso.shaderreload.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import suso.shaderreload.ShaderReload;

@Mixin(Keyboard.class) @Environment(EnvType.CLIENT)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "HEAD"), cancellable = true)
    void onOnKey(long window, int action, KeyInput input, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_F3)) {
            if (input.key() == ShaderReload.GLFW_KEY) {
                if (action != 0) {
                    ShaderReload.reloadShaders();
                }
                ci.cancel();
            } else if (input.key() == GLFW.GLFW_KEY_Q) {
                if (action == 1) {
                    client.inGameHud.getChatHud().addMessage(Text.translatable("debug.reload_shaders.help"));
                }
            }
        }
    }

    @Inject(method = "onChar", at = @At(value = "HEAD"), cancellable = true)
    void onOnChar(long window, CharInput input, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_F3) && InputUtil.isKeyPressed(this.client.getWindow(), ShaderReload.GLFW_KEY)) {
            ci.cancel();
        }
    }
}
