package suso.shaderreload.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import suso.shaderreload.client.ShaderReloadClient;

import java.io.IOException;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    private ShaderEffect entityOutlineShader;
    @Shadow
    private Framebuffer entityOutlinesFramebuffer;


    @Shadow protected abstract void resetTransparencyShader();

    @Inject(
            method = "loadEntityOutlineShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    public void outlineError(CallbackInfo ci, Identifier identifier, IOException var3) {
        if(ShaderReloadClient.reloading) {
            this.entityOutlineShader = null;
            this.entityOutlinesFramebuffer = null;
            throw new RuntimeException(var3);
        }
    }

    @Inject(
            method = "loadTransparencyShader",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;onResourceReloadFailure(Ljava/lang/Throwable;Lnet/minecraft/text/Text;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    public void transparencyError(CallbackInfo ci, Identifier identifier, Exception exception, String string, String string2, WorldRenderer.ShaderException shaderException, Text text2) {
        if (ShaderReloadClient.reloading) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.inGameHud.getChatHud().addMessage(
                    new LiteralText("")
                            .append(new LiteralText("[Debug]:").formatted(Formatting.BOLD, Formatting.YELLOW))
                            .append(new LiteralText(" Disabling fabulous graphics").formatted(Formatting.RED)));

            this.resetTransparencyShader();
            throw new RuntimeException(shaderException);
        }
    }
}
