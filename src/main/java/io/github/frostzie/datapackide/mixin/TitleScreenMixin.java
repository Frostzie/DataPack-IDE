package io.github.frostzie.datapackide.mixin;

import io.github.frostzie.datapackide.screen.MainApplication;
import io.github.frostzie.datapackide.utils.JavaFXInitializer;
import io.github.frostzie.datapackide.utils.LoggerProvider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private final @NotNull Logger logger = LoggerProvider.INSTANCE.getLogger("TitleScreenMixin");
    protected TitleScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        TitleScreen self = (TitleScreen)(Object)this;

        Button button = Button.builder(Component.literal("IDE"), b -> {
            if (JavaFXInitializer.INSTANCE.isJavaFXAvailable()) {
                MainApplication.Companion.showMainWindow();
            } else {
                logger.error("Cannot open IDE window - JavaFX is not available");
            }
        }).bounds(self.width / 2 + 128, self.height / 4 + 132, 30, 20).build();

        this.addRenderableWidget(button);
    }
}