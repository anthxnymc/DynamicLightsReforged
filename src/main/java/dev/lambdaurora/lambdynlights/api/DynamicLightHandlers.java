/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.api;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.lambdynlights.config.DynamicLightsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

/**
 * @author LambdAurora
 * @version 2.1.0
 * @since 1.1.0
 */
public final class DynamicLightHandlers {
	private DynamicLightHandlers() {
		throw new UnsupportedOperationException("DynamicLightHandlers only contains static definitions.");
	}

	/**
	 * Registers the default handlers.
	 */
	public static void registerDefaultHandlers() {
		registerDynamicLightHandler(EntityType.BLAZE, DynamicLightHandler.makeHandler(blaze -> 10, blaze -> true));
		registerDynamicLightHandler(EntityType.CREEPER, DynamicLightHandler.makeCreeperEntityHandler(null));
		registerDynamicLightHandler(EntityType.ENDERMAN, entity -> {
			int luminance = 0;
			if (entity.getCarriedBlock() != null)
				luminance = entity.getCarriedBlock().getLightEmission();
			return luminance;
		});
		registerDynamicLightHandler(EntityType.ITEM,
				entity -> LambDynLights.getLuminanceFromItemStack(entity.getItem(), entity.isUnderWater()));
		registerDynamicLightHandler(EntityType.ITEM_FRAME, entity -> {
			var world = entity.getCommandSenderWorld();
			return LambDynLights.getLuminanceFromItemStack(entity.getItem(), !world.getFluidState(entity.blockPosition()).isEmpty());
		});
		registerDynamicLightHandler(EntityType.GLOW_ITEM_FRAME, entity -> {
			var world = entity.getCommandSenderWorld();
			return Math.max(14, LambDynLights.getLuminanceFromItemStack(entity.getItem(),
					!world.getFluidState(entity.blockPosition()).isEmpty()));
		});
		registerDynamicLightHandler(EntityType.MAGMA_CUBE, entity -> (entity.squish > 0.6) ? 11 : 8);
		registerDynamicLightHandler(EntityType.SPECTRAL_ARROW, entity -> 8);
//		registerDynamicLightHandler(EntityType.GLOW_SQUID,
//				entity -> (int) Mth.clampedLerp(0.f, 12.f, 1.f - entity.getDarkTicksRemaining() / 10.f)
//		);
	}

	/**
	 * Registers an entity dynamic light handler.
	 *
	 * @param type the entity type
	 * @param handler the dynamic light handler
	 * @param <T> the type of the entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> void registerDynamicLightHandler(EntityType<T> type, DynamicLightHandler<T> handler) {
		register((DynamicLightHandlerHolder<T>) type, handler);
	}

	/**
	 * Registers a block entity dynamic light handler.
	 *
	 * @param type the block entity type
	 * @param handler the dynamic light handler
	 * @param <T> the type of the block entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockEntity> void registerDynamicLightHandler(BlockEntityType<T> type, DynamicLightHandler<T> handler) {
		register((DynamicLightHandlerHolder<T>) type, handler);
	}

	private static <T> void register(DynamicLightHandlerHolder<T> holder, DynamicLightHandler<T> handler) {
		var registeredHandler = holder.lambdynlights$getDynamicLightHandler();
		if (registeredHandler != null) {
			DynamicLightHandler<T> newHandler = entity -> Math.max(registeredHandler.getLuminance(entity), handler.getLuminance(entity));
			holder.lambdynlights$setDynamicLightHandler(newHandler);
		} else {
			holder.lambdynlights$setDynamicLightHandler(handler);
		}
	}

	/**
	 * Returns the registered dynamic light handler of the specified entity.
	 *
	 * @param type the entity type
	 * @param <T> the type of the entity
	 * @return the registered dynamic light handler
	 */
	public static <T extends Entity> @Nullable DynamicLightHandler<T> getDynamicLightHandler(EntityType<T> type) {
		return DynamicLightHandlerHolder.cast(type).lambdynlights$getDynamicLightHandler();
	}

	/**
	 * Returns the registered dynamic light handler of the specified block entity.
	 *
	 * @param type the block entity type
	 * @param <T> the type of the block entity
	 * @return the registered dynamic light handler
	 */
	public static <T extends BlockEntity> @Nullable DynamicLightHandler<T> getDynamicLightHandler(BlockEntityType<T> type) {
		return DynamicLightHandlerHolder.cast(type).lambdynlights$getDynamicLightHandler();
	}

	/**
	 * Returns whether the given entity can light up.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return {@code true} if the entity can light up, otherwise {@code false}
	 */
	public static <T extends Entity> boolean canLightUp(T entity) {
		return DynamicLightsConfig.EntityLighting.get();
	}

	/**
	 * Returns whether the given block entity can light up.
	 *
	 * @param entity the entity
	 * @param <T> the type of the block entity
	 * @return {@code true} if the block entity can light up, otherwise {@code false}
	 */
	public static <T extends BlockEntity> boolean canLightUp(T entity) {
		return DynamicLightsConfig.TileEntityLighting.get();
	}

	/**
	 * Returns the luminance from an entity.
	 *
	 * @param entity the entity
	 * @param <T> the type of the entity
	 * @return the luminance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> int getLuminanceFrom(T entity) {
		if (!DynamicLightsConfig.EntityLighting.get())
			return 0;

		var handler = (DynamicLightHandler<T>) getDynamicLightHandler(entity.getType());
		if (handler == null)
			return 0;
		if (!canLightUp(entity))
			return 0;
		if (handler.isWaterSensitive(entity)
				&& !entity.getCommandSenderWorld().getFluidState(new BlockPos(entity.getBlockX(), entity.getBlockY() + (int) entity.getEyeHeight(), entity.getBlockZ())).isEmpty())
			return 0;
		return handler.getLuminance(entity);
	}

	/**
	 * Returns the luminance from a block entity.
	 *
	 * @param entity the block entity
	 * @param <T> the type of the block entity
	 * @return the luminance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BlockEntity> int getLuminanceFrom(T entity) {
		if (!DynamicLightsConfig.TileEntityLighting.get())
			return 0;
		DynamicLightHandler<T> handler = (DynamicLightHandler<T>) getDynamicLightHandler(entity.getType());
		if (handler == null)
			return 0;
		if (!canLightUp(entity))
			return 0;
		if (handler.isWaterSensitive(entity) && entity.getLevel() != null && !entity.getLevel().getFluidState(entity.getBlockPos()).isEmpty())
			return 0;
		return handler.getLuminance(entity);
	}
}
