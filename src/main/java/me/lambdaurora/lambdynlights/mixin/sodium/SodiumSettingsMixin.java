package me.lambdaurora.lambdynlights.mixin.sodium;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;

import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import me.lambdaurora.lambdynlights.DynamicLightsReforged;
import me.lambdaurora.lambdynlights.config.DynamicLightsConfig;
import me.lambdaurora.lambdynlights.config.QualityMode;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(SodiumOptionsGUI.class)
public abstract class SodiumSettingsMixin {

    @Shadow(remap = false)
    @Final
    private List<OptionPage> pages;

    @Unique
    private static final SodiumOptionsStorage dynamicLightsOpts = new SodiumOptionsStorage();


    @Inject(method = "<init>", at = @At("RETURN"))
    private void DynamicLights(Screen prevScreen, CallbackInfo ci)
    {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<SodiumGameOptions, QualityMode> qualityMode = OptionImpl.createBuilder(QualityMode.class, dynamicLightsOpts)
                .setName("Dynamic Lights Speed")
                .setTooltip("Controls how often dynamic lights will update. " +
                        "\n\nLighting recalculation can be expensive, so slower values will give better performance." +
                        "\n\nOff - Self explanatory\nSlow - Twice a second\nFast - Five times a second\nRealtime - Every tick")
                .setControl(
                        (option) -> new CyclingControl<>(option, QualityMode.class, new ITextComponent[] {
                                new StringTextComponent("Off"),
                                new StringTextComponent("Slow"),
                                new StringTextComponent("Fast"),
                                new StringTextComponent("Realtime") }))
                .setBinding(
                        (options, value) -> {
                            DynamicLightsConfig.Quality.set(value.toString());
                            DynamicLightsReforged.clearLightSources();
                        },
                        (options) -> QualityMode.valueOf(DynamicLightsConfig.Quality.get()))
                .setImpact(OptionImpact.MEDIUM)
                .build();


        OptionImpl<SodiumGameOptions, Boolean> entityLighting = OptionImpl.createBuilder(Boolean.class, dynamicLightsOpts)
                .setName("Dynamic Entity Lights")
                .setTooltip("Turning this on will show dynamic lighting on entities (dropped items, mobs, etc). " +
                        "\n\nThis can drastically increase the amount of lighting updates, even when you're not holding a torch.")
                .setControl(TickBoxControl::new)
                .setBinding(
                        (options, value) -> DynamicLightsConfig.EntityLighting.set(value),
                        (options) -> DynamicLightsConfig.EntityLighting.get())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionImpl<SodiumGameOptions, Boolean> tileEntityLighting = OptionImpl.createBuilder(Boolean.class, dynamicLightsOpts)
                .setName("Dynamic Block Lights")
                .setTooltip("Turning this on will show dynamic lighting on tile entities (furnaces, modded machines, etc). " +
                        "\n\nThis can drastically increase the amount of lighting updates, even when you're not holding a torch.")
                .setControl(TickBoxControl::new)
                .setBinding(
                        (options, value) -> DynamicLightsConfig.TileEntityLighting.set(value),
                        (options) -> DynamicLightsConfig.TileEntityLighting.get())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        groups.add(OptionGroup
            .createBuilder()
                .add(qualityMode)
                .add(entityLighting)
                .add(tileEntityLighting)
            .build()
        );

        pages.add(new OptionPage("Dynamic Lights", ImmutableList.copyOf(groups)));
    }


}