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

package me.fallenbreath.tmcexporter.mixins.metric.dimension.chunk;

import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ThreadedAnvilChunkStorageMixin
{
	@Shadow @Final
	ServerWorld world;

	@Inject(
			method = "setLevel",
			at = @At(
					value = "INVOKE",
					target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;put(JLjava/lang/Object;)Ljava/lang/Object;",
					remap = false
			)
	)
	private void countChunkLoad(CallbackInfoReturnable<ChunkHolder> cir)
	{
		MetricCollector.getDimStats(this.world).ifPresent(ds -> ds.chunk.added++);
	}

	@Inject(
			method = "unloadChunks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;tryUnloadChunk(JLnet/minecraft/server/world/ChunkHolder;)V"
			)
	)
	private void countChunkUnload(CallbackInfo ci)
	{
		MetricCollector.getDimStats(this.world).ifPresent(ds -> ds.chunk.removed++);
	}
}
