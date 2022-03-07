package suso.shaderreload.client;

import net.fabricmc.api.ClientModInitializer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class ShaderReloadClient implements ClientModInitializer {

    public static boolean reloading = false;

    @Override
    public void onInitializeClient() {
        System.out.println("Suso's shader reload is installed! Use with F3+R");
    }
}
