package suso.shaderreload.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
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
    @Shadow private PostEffectProcessor entityOutlinePostProcessor;
    @Shadow private Framebuffer entityOutlinesFramebuffer;

    @Redirect(method = "loadTransparencyPostProcessor", at = @At(value = "NEW", target = "Lnet/minecraft/client/gl/PostEffectProcessor;"))
    PostEffectProcessor onLoadTransparencyPostProcessor$new(TextureManager textureManager, ResourceManager resourceManager,
                                              Framebuffer framebuffer, Identifier location) throws IOException {
        return ShaderReload.onLoadShader$new(textureManager, resourceManager, framebuffer, location);
    }

    @Inject(method = "loadTransparencyPostProcessor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer$ProgramInitException;<init>(Ljava/lang/String;Ljava/lang/Throwable;)V"),
            cancellable = true)
    void onLoadTransparencyShader$error(CallbackInfo ci) {
        client.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
        client.options.write();
        ShaderReload.onLoadShader$end();
        ci.cancel();
    }

    @Inject(method = "loadTransparencyPostProcessor", at = @At("TAIL"))
    void onLoadTransparencyPostProcessor$success(CallbackInfo ci) {
        ShaderReload.onLoadShader$end();
    }

    @Redirect(method = "loadEntityOutlinePostProcessor", at = @At(value = "NEW", target = "Lnet/minecraft/client/gl/PostEffectProcessor;"))
    PostEffectProcessor onLoadEntityOutlinePostProcessor$new(TextureManager textureManager, ResourceManager resourceManager,
                                               Framebuffer framebuffer, Identifier location) throws IOException {
        return ShaderReload.onLoadShader$new(textureManager, resourceManager, framebuffer, location);
    }

    @Inject(method = "loadEntityOutlinePostProcessor", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false), cancellable = true)
    void onLoadEntityOutlinePostProcessor$error(CallbackInfo ci) {
        entityOutlinePostProcessor = null;
        entityOutlinesFramebuffer = null;
        ShaderReload.onLoadShader$end();
        ci.cancel();
    }

    @Inject(method = "loadEntityOutlinePostProcessor", at = @At("TAIL"))
    void onLoadEntityOutlinePostProcessor$success(CallbackInfo ci) {
        ShaderReload.onLoadShader$end();
    }
}
