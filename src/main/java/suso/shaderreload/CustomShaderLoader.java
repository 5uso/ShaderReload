package suso.shaderreload;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.util.Set;

public class CustomShaderLoader extends ShaderLoader {
    private final DefaultResourcePack defaultPack = MinecraftClient.getInstance().getDefaultResourcePack();

    public CustomShaderLoader(TextureManager textureManager) {
        super(textureManager, CustomShaderLoader::onShaderError);
    }

    @Override
    public PostEffectProcessor loadPostEffect(Identifier id, Set<Identifier> availableExternalTargets) {
        PostEffectProcessor result = super.loadPostEffect(id, availableExternalTargets);
        if(result != null) return result;

        try {
            return loadDefaultPostEffect(id, availableExternalTargets);
        } catch (Exception e) {
            ShaderReload.printShaderException(e, true);
        }

        return null;
    }

    static void onShaderError(Exception e) {
        ShaderReload.tripError();
        ShaderReload.printShaderException(e, false);
    }

    private ShaderProgram loadDefaultProgram(ShaderProgram key) throws FileNotFoundException {
        return null;
    }

    private PostEffectProcessor loadDefaultPostEffect(Identifier id, Set<Identifier> availableExternalTargets) {
        return null;
    }
}