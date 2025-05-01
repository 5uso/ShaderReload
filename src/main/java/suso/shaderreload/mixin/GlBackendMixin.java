package suso.shaderreload.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.CompiledShaderPipeline;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import suso.shaderreload.ShaderReload;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(GlBackend.class)
public class GlBackendMixin {
    @ModifyArg(
            method = {
                    "compileShader(Lnet/minecraft/client/gl/GlBackend$ShaderKey;Ljava/util/function/BiFunction;)Lnet/minecraft/client/gl/CompiledShader;",
                    "compileRenderPipeline"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private String shader_reload$printPipelineShaderErrors(String logMessage, Object... args) {
        ShaderReload.printShaderLogError(logMessage, args);
        return logMessage;
    }

    @ModifyArg(
            method = {
                    "compileShader(Lnet/minecraft/client/gl/GlBackend$ShaderKey;Ljava/util/function/BiFunction;)Lnet/minecraft/client/gl/CompiledShader;",
                    "compileRenderPipeline"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            )
    )
    private String shader_reload$printPipelineShaderErrors(String logMessage, Object arg1, Object arg2) {
        ShaderReload.printShaderLogError(logMessage, arg1, arg2);
        return logMessage;
    }

    @WrapMethod(method = "compileRenderPipeline")
    private CompiledShaderPipeline shader_reload$retryFailedShadersWithDefault(RenderPipeline pipeline, BiFunction<Identifier, ShaderType, String> sourceRetriever, Operation<CompiledShaderPipeline> op) {
        ShaderReload.isReloadingBuiltin = false;
        var compiled = op.call(pipeline, sourceRetriever);
        if(compiled.isValid())
            return compiled;

        ShaderReload.isReloadingBuiltin = true;
        var vanillyOnlyDefinitions = ShaderReload.getVanillaOnlyDefinitions();
        return op.call(
                pipeline,
                (BiFunction<Identifier, ShaderType, String>)(id, type) ->
                        vanillyOnlyDefinitions.shaderSources().get(new ShaderLoader.ShaderSourceKey(id, type))
        );
    }

    @WrapOperation(
            method = "compileShader(Lnet/minecraft/util/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;Lnet/minecraft/client/gl/Defines;Ljava/util/function/BiFunction;)Lnet/minecraft/client/gl/CompiledShader;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
            )
    )
    private <K, V> Object shader_reload$skipCacheWhenReloadingBuiltin(Map<K, V> instance, K key, Function<? super K, ? extends V> mapping_function, Operation<V> op) {
        if(ShaderReload.isReloadingBuiltin)
            return mapping_function.apply(key);
        return op.call(instance, key, mapping_function);
    }
}
