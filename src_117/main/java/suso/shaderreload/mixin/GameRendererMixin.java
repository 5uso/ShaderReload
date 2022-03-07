package suso.shaderreload.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import suso.shaderreload.client.ShaderReloadClient;

import java.io.IOException;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow private int forcedShaderIndex;
    @Shadow private boolean shadersEnabled;
    @Shadow @Final public static int SHADER_COUNT;

    @Inject(
            method = "loadShader(Lnet/minecraft/util/Identifier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    public void error(Identifier id, CallbackInfo ci, IOException var3) {
        if(ShaderReloadClient.reloading) {
            this.forcedShaderIndex = SHADER_COUNT;
            this.shadersEnabled = false;
            throw new RuntimeException(var3);
        }
    }
}
