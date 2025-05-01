package suso.shaderreload.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import suso.shaderreload.ShaderReload;

@Mixin(targets = "net.minecraft.client.gl.ShaderLoader$1")
public class ShaderLoaderGlImportProcessorMixin {
    @ModifyArg(
            method = "loadImport",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private String shader_reload$printGLSLImportErrors(String format, Object arg1, Object arg2) {
        ShaderReload.printShaderLogError(format, arg1, arg2);
        return format;
    }
}
