package suso.shaderreload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Environment(EnvType.CLIENT)
public class ShaderReload implements ClientModInitializer {
    public static final int GLFW_KEY = GLFW.GLFW_KEY_R;
    public static final Logger LOGGER = LogManager.getLogger("Shader Reload");

    private static boolean reloading = false;
    private static boolean expectError = false;
    private static ResourceReloader shaderLoader;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Suso's Shader Reload is installed! Use with F3 + R");
    }

    public static void reloadShaders() {
        if(reloading) {
            return;
        }

        reloading = true;
        expectError = false;
        MinecraftClient client = MinecraftClient.getInstance();

        SimpleResourceReload.start(client.getResourceManager(), List.of(shaderLoader, client.worldRenderer), Util.getMainWorkerExecutor(), client, CompletableFuture.completedFuture(Unit.INSTANCE), false).whenComplete().whenComplete((result, throwable) -> {
            reloading = false;
            if(throwable == null) {
                debugLog(Text.translatable("debug.reload_shaders.message"));
                expectError = false;
                return;
            }

            if(throwable instanceof CompletionException ex && ex.getCause() != null) {
                throwable = ex.getCause();
            }

            if(!expectError) {
                debugLog(Text.translatable("debug.reload_shaders.unknown_error"));
                LOGGER.error(throwable);
            }

            expectError = false;
        });
    }

    // Print a shader exception in chat.
    public static void printShaderException(Exception exception, boolean builtin) {
        MinecraftClient client = MinecraftClient.getInstance();
        Throwable throwable = exception;
        while (!(throwable instanceof InvalidHierarchicalFileException)) {
            Throwable cause = throwable.getCause();
            if(cause != null) {
                throwable = cause;
            } else {
                String translationKey = "debug.reload_shaders.unknown_error" + (builtin ? ".builtin" : "");
                debugLog(Text.translatable(translationKey));
                LOGGER.error(throwable);
                return;
            }
        }

        String translationKey = "debug.reload_shaders.error" + (builtin ? ".builtin" : "");
        debugLog(Text.translatable(translationKey));
        client.inGameHud.getChatHud().addMessage(Text.literal(throwable.getMessage()).formatted(Formatting.GRAY));
    }

    public static void setShaderLoader(ResourceReloader value) {
        shaderLoader = value;
    }

    public static void tripError() {
        expectError = true;
    }

    private static void debugLog(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.getChatHud().addMessage(text);
    }
}
