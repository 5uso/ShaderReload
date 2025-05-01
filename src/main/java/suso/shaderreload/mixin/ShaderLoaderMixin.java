package suso.shaderreload.mixin;

import net.minecraft.client.gl.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import suso.shaderreload.ShaderReload;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {
    @ModifyArg(
            method = "loadPostEffect(Lnet/minecraft/util/Identifier;Ljava/util/Set;)Lnet/minecraft/client/gl/PostEffectProcessor;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private String shader_reload$printPostEffectShaderErrors(String format, Object arg1, Object arg2) {
        ShaderReload.printShaderLogError(format + ": {}", arg1, arg2);
        return format;
    }

    @ModifyArg(
            method = "loadPostEffect(Lnet/minecraft/util/Identifier;Lnet/minecraft/resource/Resource;Lcom/google/common/collect/ImmutableMap$Builder;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private static String shader_reload$printPostEffectChainErrors(String format, Object arg1, Object arg2) {
        ShaderReload.printShaderLogError(format + ": {}", arg1, arg2);
        return format;
    }
}
