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
import me.fallenbreath.tmcexporter.metric.collect.TimeCostRecorder;
import me.fallenbreath.tmcexporter.metric.common.GamePhase;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin
{
	@Unique
	@Nullable
	private TimeCostRecorder<GamePhase> phaseCosts;

	@Inject(method = "tick", at = @At("HEAD"))
	private void timeCost_getDimensionStats(CallbackInfo ci)
	{
		this.phaseCosts = MetricCollector.getDimStats((ServerWorld)(Object)this).
				map(ds -> ds.phaseCosts).
				orElse(null);
	}

	@Inject(method = "tickChunk", at = @At("HEAD"))
	private void timeCost_chunkTickStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.push(GamePhase.CHUNK_TICK);
		}
	}

	@Inject(method = "tickChunk", at = @At("TAIL"))
	private void timeCost_chunkTickEnd(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.pop(GamePhase.CHUNK_TICK);
		}
	}

	@Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=tickPending"))
	private void timeCost_tileTickStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.switchTo(GamePhase.TILE_TICK);
		}
	}

	@Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=raid"))
	private void timeCost_tileTickEnd_raidStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.switchTo(GamePhase.RAID);
		}
	}

	@Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=blockEvents"))
	private void timeCost_raidEnd_blockEventStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.switchTo(GamePhase.BLOCK_EVENT);
		}
	}

	@Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=entities"))
	private void timeCost_blockEventEnd_entityStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.switchTo(GamePhase.ENTITY);
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V"))
	private void timeCost_entityEnd_blockEntityStart(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.switchTo(GamePhase.BLOCK_ENTITY);
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V", shift = At.Shift.AFTER))
	private void timeCost_blockEntityEnd(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.pop(GamePhase.BLOCK_ENTITY);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void timeCost_cleanDimensionStats(CallbackInfo ci)
	{
		if (this.phaseCosts != null)
		{
			this.phaseCosts.reset();
		}
		this.phaseCosts = null;
	}
}
