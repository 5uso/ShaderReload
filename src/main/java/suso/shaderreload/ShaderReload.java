package suso.shaderreload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.profiler.DummyProfiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import suso.shaderreload.mixin.KeyboardInvoker;
import suso.shaderreload.mixin.ShaderLoaderAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static net.minecraft.resource.ResourceType.CLIENT_RESOURCES;

@Environment(EnvType.CLIENT)
public class ShaderReload implements ClientModInitializer {
    public static final int GLFW_KEY = GLFW.GLFW_KEY_R;
    public static final Logger LOGGER = LogManager.getLogger("Shader Reload");

    private static boolean reloading = false;
    public static boolean isReloadingBuiltin = false;
    private static @Nullable ShaderLoader.Definitions vanillaOnlyDefinitions = null;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Suso's Shader Reload is installed! Use with F3 + R");
    }

    public static void reloadShaders() {
        if (reloading) return;
        var client = MinecraftClient.getInstance();
        reloading = true;
        isReloadingBuiltin = false;
        SimpleResourceReload.start(client.getResourceManager(), List.of(client.getShaderLoader(), client.worldRenderer),
                        Util.getMainWorkerExecutor(), client, CompletableFuture.completedFuture(Unit.INSTANCE), false)
                .whenComplete()
                .whenComplete((result, throwable) -> {
                    reloading = false;
                    if (throwable == null) {
                        ((KeyboardInvoker) client.keyboard).invokeDebugLog("debug.reload_shaders.message");
                        return;
                    }
                    if (throwable instanceof CompletionException ex && ex.getCause() != null) {
                        throwable = ex.getCause();
                    }
                    ((KeyboardInvoker) client.keyboard).invokeDebugError("debug.reload_shaders.unknown_error");
                    throwable.printStackTrace();
                });
    }

    public static void printShaderLogError(String errorMessage, Object... arguments) {
        var client = MinecraftClient.getInstance();
        var translationKey = "debug.reload_shaders.error" + (isReloadingBuiltin ? ".builtin" : "");
        ((KeyboardInvoker) client.keyboard).invokeDebugError(translationKey);
        var logText = Text.translatable(
                errorMessage.replace("{}", "%s"),
                Arrays.stream(arguments)
                        .map(String::valueOf)
                        .map(ShaderReload::removeEx)
                        .toArray()
        ).formatted(Formatting.GRAY);
        client.inGameHud.getChatHud().addMessage(logText);
    }

    public static String removeEx(String msg) {
        msg = msg.substring(msg.indexOf(": ") + 2);
        while(true) {
            final var colonIndex = msg.indexOf(": ");
            if(colonIndex == -1)
                break;
            if(msg.substring(0, colonIndex).contains(" ")) {
                // Definitely not an error name, so it shouldn't be removed
                break;
            }
            msg = msg.substring(colonIndex + 2);
        }
        return msg;
    }

    public static ResourceManager getDefaultResourceManager() {
        var defaultPack = MinecraftClient.getInstance().getDefaultResourcePack();
        return new LifecycledResourceManagerImpl(CLIENT_RESOURCES, List.of(defaultPack));
    }

    public static ShaderLoader.Definitions getVanillaOnlyDefinitions() {
        if(vanillaOnlyDefinitions != null)
            return vanillaOnlyDefinitions;

        vanillaOnlyDefinitions = ((ShaderLoaderAccessor)MinecraftClient.getInstance().getShaderLoader())
                .callPrepare(getDefaultResourceManager(), DummyProfiler.INSTANCE);
        return vanillaOnlyDefinitions;
    }
}
