package suso.shaderreload.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.ShaderParseException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlProgramManager.class)
public class LinkerMixin {
    @Inject(at = @At(value = "INVOKE", target = "org/apache/logging/log4j/Logger.warn (Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), method = "linkProgram", cancellable = true)
    private static void linkError(GlShader shader, CallbackInfo ci) throws ShaderParseException {
        throw new ShaderParseException(GlStateManager.glGetProgramInfoLog(shader.getProgramRef(), 32768));
    }
}
