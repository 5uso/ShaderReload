package suso.shaderreload.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.TextureManager;
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

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private ShaderEffect entityOutlineShader;
    @Shadow private Framebuffer entityOutlinesFramebuffer;

    // Minecraft Development plugin definitely doesn't like @Redirects of NEW :pensive_bread:
    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "loadTransparencyShader", at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/texture/TextureManager;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/gl/ShaderEffect;"))
    ShaderEffect onLoadTransparencyShader$new(TextureManager textureManager, ResourceManager resourceManager,
                                              Framebuffer framebuffer, Identifier location) throws IOException {
        return ShaderReload.onLoadShader$new(textureManager, resourceManager, framebuffer, location);
    }

    @Inject(method = "loadTransparencyShader", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer$ShaderException;<init>(Ljava/lang/String;Ljava/lang/Throwable;)V"),
            cancellable = true)
    void onLoadTransparencyShader$error(CallbackInfo ci) {
        client.options.graphicsMode = GraphicsMode.FANCY;
        client.options.write();
        ShaderReload.onLoadShader$end();
        ci.cancel();
    }

    @Inject(method = "loadTransparencyShader", at = @At("TAIL"))
    void onLoadTransparencyShader$success(CallbackInfo ci) {
        ShaderReload.onLoadShader$end();
    }

    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference", "InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @Redirect(method = "loadEntityOutlineShader", at = @At(value = "NEW",
            target = "(Lnet/minecraft/client/texture/TextureManager;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/gl/ShaderEffect;"))
    ShaderEffect onLoadEntityOutlineShader$new(TextureManager textureManager, ResourceManager resourceManager,
                                               Framebuffer framebuffer, Identifier location) throws IOException {
        return ShaderReload.onLoadShader$new(textureManager, resourceManager, framebuffer, location);
    }

    @Inject(method = "loadEntityOutlineShader", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false), cancellable = true)
    void onLoadEntityOutlineShader$error(CallbackInfo ci) {
        entityOutlineShader = null;
        entityOutlinesFramebuffer = null;
        ShaderReload.onLoadShader$end();
        ci.cancel();
    }

    @Inject(method = "loadEntityOutlineShader", at = @At("TAIL"))
    void onLoadEntityOutlineShader$success(CallbackInfo ci) {
        ShaderReload.onLoadShader$end();
    }
}
