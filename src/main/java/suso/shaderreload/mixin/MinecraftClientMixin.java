package suso.shaderreload.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import suso.shaderreload.CustomShaderLoader;
import suso.shaderreload.ShaderReload;

import java.util.function.Consumer;

@Mixin(MinecraftClient.class) @Environment(EnvType.CLIENT)
public class MinecraftClientMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/client/gl/ShaderLoader;"))
    ShaderLoader replaceShaderLoader(TextureManager textureManager, Consumer<Exception> onError) {
        ShaderLoader loader = new CustomShaderLoader(textureManager);
        ShaderReload.setShaderLoader(loader);
        return loader;
    }
}
