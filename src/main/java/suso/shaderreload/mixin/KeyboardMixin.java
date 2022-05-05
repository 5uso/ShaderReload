package suso.shaderreload.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import suso.shaderreload.ShaderReload;

import java.util.Objects;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean switchF3State;

    @Inject(method = "processF3", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V",
            ordinal = 11, shift = At.Shift.AFTER))
    private void onProcessF3$addHelp(int key, CallbackInfoReturnable<Boolean> cir) {
        client.inGameHud.getChatHud().addMessage(new TranslatableText("debug.reload_shaders.help"));
    }

    @Inject(method = "processF3", at = @At("RETURN"), cancellable = true)
    void onProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == ShaderReload.GLFW_KEY) {
            ShaderReload.reloadShaders();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onKey", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
            cancellable = true)
    void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) || key != ShaderReload.GLFW_KEY) return;
        if (action != 0) {
            if (Objects.requireNonNull(client.currentScreen).passEvents) {
                switchF3State = true;
            }
            ShaderReload.reloadShaders();
        }
        ci.cancel();
    }

    @Inject(method = "onChar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
            ordinal = 0), cancellable = true)
    void onOnChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) && InputUtil.isKeyPressed(window, ShaderReload.GLFW_KEY)) {
            ci.cancel();
        }
    }
}
