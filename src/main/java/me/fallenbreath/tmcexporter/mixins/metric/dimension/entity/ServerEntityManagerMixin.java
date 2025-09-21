/*
 * This file is part of the TMC Exporter project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
 *
 * TechMetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TechMetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TechMetrics.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tmcexporter.mixins.metric.dimension.entity;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.utils.IdentifierUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerEntityManager.class)
public abstract class ServerEntityManagerMixin<T extends EntityLike>
{
	@Inject(
			method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/EntityTrackingSection;add(Lnet/minecraft/world/entity/EntityLike;)V"
			)
	)
	private void countEntityAdd(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) T entityLike)
	{
		if (entityLike instanceof Entity)
		{
			Entity entity = (Entity)entityLike;
			World world = entity.getEntityWorld();
			if (world instanceof ServerWorld)
			{
				MetricCollector.getDimStats(world).ifPresent(ds -> {
					ds.entity.access(IdentifierUtils.of(entity.getType()), v -> v.added++);
				});
			}
		}
	}
}
