package suso.shaderreload;

import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import suso.shaderreload.mixin.KeyboardInvoker;

import java.io.IOException;
import java.util.List;

import static net.minecraft.resource.ResourceType.CLIENT_RESOURCES;

@Environment(EnvType.CLIENT)
public class ShaderReload implements ClientModInitializer {
    public static final int GLFW_KEY = GLFW.GLFW_KEY_R;
    public static final Logger LOGGER = LogManager.getLogger("Shader Reload");

    private static final StopException STOP = new StopException();
    private static boolean reloading = false;
    private static boolean stopReloading = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Suso's Shader Reload is installed! Use with F3 + R");
    }

    public static void reloadShaders() {
        MinecraftClient client = MinecraftClient.getInstance();
        reloading = true;
        stopReloading = false;
        try {
            client.gameRenderer.reload(client.getResourceManager());
            client.worldRenderer.reload(client.getResourceManager());
            ((KeyboardInvoker) client.keyboard).invokeDebugLog("debug.reload_shaders.message");
        } catch (StopException ignored) {}
        reloading = false;
    }

    // Print a shader exception in chat.
    private static void printShaderException(Exception exception, boolean builtin) {
        MinecraftClient client = MinecraftClient.getInstance();
        Throwable throwable = exception;
        while (!(throwable instanceof ShaderParseException)) {
            Throwable cause = throwable.getCause();
            if (cause != null) throwable = cause;
            else {
                String translationKey = "debug.reload_shaders.unknown_error" + (builtin ? ".builtin" : "");
                ((KeyboardInvoker) client.keyboard).invokeDebugError(translationKey);
                throwable.printStackTrace();
                return;
            }
        }
        String translationKey = "debug.reload_shaders.error" + (builtin ? ".builtin" : "");
        ((KeyboardInvoker) client.keyboard).invokeDebugError(translationKey);
        client.inGameHud.getChatHud().addMessage(new LiteralText(throwable.getMessage()).formatted(Formatting.GRAY));
    }

    // Try loading a core shader; if it fails, stop shader reloading or try loading a built-in core shader.
    public static Shader onLoadShaders$new(ResourceFactory factory, String name, VertexFormat format) throws IOException {
        try {
            return new Shader(factory, name, format);
        } catch (IOException e) {
            printShaderException(e, false);
            if (reloading) throw STOP;
        }
        try {
            return new Shader(MinecraftClient.getInstance().getResourcePackProvider().getPack(), name, format);
        } catch (IOException e) {
            printShaderException(e, true);
            throw e;
        }
    }

    // Try loading a shader effect; if it fails, request stopping and try loading a built-in shader effect.
    @SuppressWarnings("resource")
    public static ShaderEffect onLoadShader$new(TextureManager textureManager, ResourceManager resourceManager,
                                                Framebuffer framebuffer, Identifier location) throws IOException {
        try {
            return new ShaderEffect(textureManager, resourceManager, framebuffer, location);
        } catch (IOException | JsonSyntaxException e) {
            printShaderException(e, false);
            stopReloading = true;
        }
        try {
            DefaultResourcePack defaultPack = MinecraftClient.getInstance().getResourcePackProvider().getPack();
            resourceManager = new LifecycledResourceManagerImpl(CLIENT_RESOURCES, List.of(defaultPack));
            return new ShaderEffect(textureManager, resourceManager, framebuffer, location);
        } catch (IOException | JsonSyntaxException e) {
            printShaderException(e, true);
            throw e;
        }
    }

    // Stop shader reloading if it's requested.
    public static void onLoadShader$end() {
        if (reloading && stopReloading) throw STOP;
    }

    private static class StopException extends RuntimeException {
        private StopException() {}
    }
}
