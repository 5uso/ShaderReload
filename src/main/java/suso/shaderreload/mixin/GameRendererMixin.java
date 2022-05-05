package suso.shaderreload.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import suso.shaderreload.ShaderReload;

import java.io.IOException;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final public static int SHADER_COUNT;
    @Shadow private int forcedShaderIndex;
    @Shadow private boolean shadersEnabled;

    // Minecraft Development plugin definitely doesn't like @Redirects of NEW :pensive_bread:
    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "loadShaders", at = @At(value = "NEW",
            target = "(Lnet/minecraft/resource/ResourceFactory;Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;)Lnet/minecraft/client/render/Shader;"))
    Shader onLoadShaders$new(ResourceFactory factory, String name, VertexFormat format) throws IOException {
        return ShaderReload.onLoadShaders$new(factory, name, format);
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "loadShader(Lnet/minecraft/util/Identifier;)V", at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/texture/TextureManager;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/gl/ShaderEffect;"))
    ShaderEffect onLoadShader$new(TextureManager textureManager, ResourceManager resourceManager,
                                  Framebuffer framebuffer, Identifier location) throws IOException {
        return ShaderReload.onLoadShader$new(textureManager, resourceManager, framebuffer, location);
    }

    @Inject(method = "loadShader(Lnet/minecraft/util/Identifier;)V", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false), cancellable = true)
    void onLoadShader$error(Identifier id, CallbackInfo ci) {
        forcedShaderIndex = SHADER_COUNT;
        shadersEnabled = false;
        ShaderReload.onLoadShader$end();
        ci.cancel();
    }

    @Inject(method = "loadShader(Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    void onLoadShader$success(Identifier id, CallbackInfo ci) {
        ShaderReload.onLoadShader$end();
    }
}
