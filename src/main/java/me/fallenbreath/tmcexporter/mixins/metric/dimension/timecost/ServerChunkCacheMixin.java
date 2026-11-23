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
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin
{
	@Shadow @Final private ServerLevel level;

	@Inject(method = "tick", at = @At("HEAD"))
	private void timeCost_chunkManagingStart(CallbackInfo ci)
	{
		MetricCollector.getDimStats(this.level).ifPresent(ds -> ds.phaseCosts.push(GamePhase.CHUNK_MANAGING));
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void timeCost_chunkManagingEnd(CallbackInfo ci)
	{
		MetricCollector.getDimStats(this.level).ifPresent(ds -> {
			ds.phaseCosts.pop(GamePhase.CHUNK_MANAGING);
			ds.phaseCosts.reset();
		});
	}
}
