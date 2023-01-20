package suso.shaderreload;

import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import suso.shaderreload.mixin.KeyboardInvoker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.resource.ResourceType.CLIENT_RESOURCES;

@Environment(EnvType.CLIENT)
public class ShaderReload implements ClientModInitializer {
    public static final int GLFW_KEY = GLFW.GLFW_KEY_R;
    public static final Logger LOGGER = LogManager.getLogger("Shader Reload");

    private static final StopException STOP = new StopException();
    private static boolean reloading = false;
    private static boolean stopReloading = false;
    private static List<ResourceReloader> gameResourceReloader = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Suso's Shader Reload is installed! Use with F3 + R");
    }

    public static void reloadShaders() {
        MinecraftClient client = MinecraftClient.getInstance();
        reloading = true;
        stopReloading = false;
        try {
            if (gameResourceReloader == null)
                gameResourceReloader = List.of(client.gameRenderer.createProgramReloader());
            SimpleResourceReload.start(client.getResourceManager(), gameResourceReloader, Util.getMainWorkerExecutor(),
                    client, CompletableFuture.completedFuture(Unit.INSTANCE), false);
            client.worldRenderer.reload(client.getResourceManager());
            ((KeyboardInvoker) client.keyboard).invokeDebugLog("debug.reload_shaders.message");
        } catch (StopException ignored) {}
        reloading = false;
    }

    // Print a shader exception in chat.
    private static void printShaderException(Exception exception, boolean builtin) {
        MinecraftClient client = MinecraftClient.getInstance();
        Throwable throwable = exception;
        while (!(throwable instanceof InvalidHierarchicalFileException)) {
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
        client.inGameHud.getChatHud().addMessage(Text.literal(throwable.getMessage()).formatted(Formatting.GRAY));
    }

    // Try loading a core shader; if it fails, stop shader reloading or try loading a built-in core shader.
    public static ShaderProgram onLoadShaders$new(ResourceFactory factory, String name, VertexFormat format) throws IOException {
        try {
            return new ShaderProgram(factory, name, format);
        } catch (IOException e) {
            printShaderException(e, false);
            if (reloading) throw STOP;
        }
        try {
            DefaultResourcePack defaultPack = MinecraftClient.getInstance().getDefaultResourcePack();
            return new ShaderProgram(defaultPack.getFactory(), name, format);
        } catch (IOException e) {
            printShaderException(e, true);
            throw e;
        }
    }

    // Try loading a shader effect; if it fails, request stopping and try loading a built-in shader effect.
    @SuppressWarnings("resource")
    public static PostEffectProcessor onLoadShader$new(TextureManager textureManager, ResourceManager resourceManager,
                                                       Framebuffer framebuffer, Identifier location) throws IOException {
        try {
            return new PostEffectProcessor(textureManager, resourceManager, framebuffer, location);
        } catch (IOException | JsonSyntaxException e) {
            printShaderException(e, false);
            stopReloading = true;
        }
        try {
            DefaultResourcePack defaultPack = MinecraftClient.getInstance().getDefaultResourcePack();
            resourceManager = new LifecycledResourceManagerImpl(CLIENT_RESOURCES, List.of(defaultPack));
            return new PostEffectProcessor(textureManager, resourceManager, framebuffer, location);
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
