package suso.shaderreload.mixin;

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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import suso.shaderreload.ShaderReload;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "processF3", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V",
            ordinal = 0,
            shift = At.Shift.AFTER),
            slice = @Slice(
                    from = @At(value = "CONSTANT",
                    args = "stringValue=debug.reload_resourcepacks.help")))
    private void onProcessF3$addHelp(int key, CallbackInfoReturnable<Boolean> cir) {
        client.inGameHud.getChatHud().addMessage(Text.translatable("debug.reload_shaders.help"));
    }

    @Inject(method = "processF3", at = @At("RETURN"), cancellable = true)
    void onProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == ShaderReload.GLFW_KEY) {
            ShaderReload.reloadShaders();
            cir.setReturnValue(true);
        }
    }

    // processF3 isn't called when a screen is open, but shader reloading is still supposed to work in that case
    @Inject(method = "onKey", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"),
            cancellable = true)
    void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) || key != ShaderReload.GLFW_KEY) return;
        if (action != 0) {
            ShaderReload.reloadShaders();
        }
        ci.cancel();
    }
}
