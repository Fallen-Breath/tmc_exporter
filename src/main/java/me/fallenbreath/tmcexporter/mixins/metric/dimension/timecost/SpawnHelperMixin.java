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

package me.fallenbreath.tmcexporter.mixins.metric.dimension.timecost;

import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin
{
	@ModifyVariable(
			//#if MC >= 11600
			method = "spawn",
			//#else
			//$$ method = "spawnEntitiesInChunk",
			//#endif
			at = @At("HEAD"), argsOnly = true
	)

	//#if MC >= 11500
	private static ServerWorld timeCost_spawningStart(ServerWorld world)
	//#else
	//$$ private static World timeCost_spawningStart(World world)
	//#endif
	{
		MetricCollector.getDimStats(world).ifPresent(ds -> ds.phaseCosts.push(GamePhase.SPAWNING));
		return world;
	}

	@ModifyVariable(
			//#if MC >= 11600
			method = "spawn",
			//#else
			//$$ method = "spawnEntitiesInChunk",
			//#endif
			at = @At("RETURN"),
			argsOnly = true
	)
	//#if MC >= 11500
	private static ServerWorld timeCost_spawningEnd(ServerWorld world)
	//#else
	//$$ private static World timeCost_spawningEnd(World world)
	//#endif
	{
		MetricCollector.getDimStats(world).ifPresent(ds -> ds.phaseCosts.pop(GamePhase.SPAWNING));
		return world;
	}
}
