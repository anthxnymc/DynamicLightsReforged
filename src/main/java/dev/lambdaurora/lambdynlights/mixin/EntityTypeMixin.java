/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdynlights.mixin;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.accessor.DynamicLightHandlerHolder;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> implements DynamicLightHandlerHolder<T> {

	@Shadow @javax.annotation.Nullable private Component description;

	@Unique
	private DynamicLightHandler<T> lambdynlights$lightHandler;

	@Override
	public @Nullable DynamicLightHandler<T> lambdynlights$getDynamicLightHandler() {
		return this.lambdynlights$lightHandler;
	}

	@Override
	public void lambdynlights$setDynamicLightHandler(DynamicLightHandler<T> handler) {
		this.lambdynlights$lightHandler = handler;
	} 
}
