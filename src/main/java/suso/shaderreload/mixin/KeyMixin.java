package suso.shaderreload.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderParseException;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class KeyMixin {
    @Inject(at = @At("RETURN"), method = "processF3", cancellable = true)
    public void inputReload(int key, CallbackInfoReturnable<Boolean> cir) {
        switch (key) {

            case 'Q':
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                        new LiteralText("F3 + R = Reload shaders ")
                .append(new LiteralText("[Fabric mod by Suso]").formatted(Formatting.AQUA)));
                return;

            case 'R':
                MinecraftClient client = MinecraftClient.getInstance();
                try {
                    client.gameRenderer.reload(client.getResourceManager());
                    client.inGameHud.getChatHud().addMessage(
                            new LiteralText("")
                    .append(new LiteralText("[Debug]:").formatted(Formatting.BOLD, Formatting.YELLOW))
                    .append(new LiteralText(" Reloaded shaders")));
                } catch (RuntimeException e) {
                    Throwable ex = e;
                    Throwable cause;

                    do {
                        cause = ex.getCause();
                        if (cause != null) ex = cause;
                        else {
                            client.inGameHud.getChatHud().addMessage(
                                    new LiteralText("")
                            .append(new LiteralText("[Debug]:").formatted(Formatting.BOLD, Formatting.YELLOW))
                            .append(new LiteralText(" Unknown error while reloading shaders").formatted(Formatting.RED)));
                            ex.printStackTrace();
                            return;
                        }
                    } while (!(ex instanceof ShaderParseException));

                    ShaderParseException s = (ShaderParseException) ex;
                    client.inGameHud.getChatHud().addMessage(
                            new LiteralText("")
                    .append(new LiteralText("[Debug]:").formatted(Formatting.BOLD, Formatting.YELLOW))
                    .append(new LiteralText(" Error while reloading shaders\n\n").formatted(Formatting.RED))
                    .append(new LiteralText(s.getMessage())));
                }
                cir.setReturnValue(true);

        }
    }
}
