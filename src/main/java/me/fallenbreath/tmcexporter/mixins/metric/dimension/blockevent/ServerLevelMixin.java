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

package me.fallenbreath.tmcexporter.mixins.metric.dimension.blockevent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.utils.IdentifierUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin
{
	@Shadow @Final
	private ObjectLinkedOpenHashSet<BlockEventData> blockEvents;

	@Inject(method = "blockEvent", at = @At("HEAD"))
	private void countBlockEventAdd_storeSizeBefore(CallbackInfo ci, @Share("") LocalIntRef sizeBeforeAdd)
	{
		sizeBeforeAdd.set(this.blockEvents.size());
	}

	@Inject(method = "blockEvent", at = @At("TAIL"))
	private void countBlockEventAdd_compareSizeAndRecord(CallbackInfo ci, @Local(argsOnly = true) Block block, @Share("") LocalIntRef sizeBeforeAdd)
	{
		if (sizeBeforeAdd.get() + 1 == this.blockEvents.size())
		{
			MetricCollector.getDimStats((ServerLevel)(Object)this).ifPresent(ds -> {
				ds.blockEvent.access(IdentifierUtils.of(block), v -> v.added++);
			});
		}
	}

	@ModifyExpressionValue(
			method = "runBlockEvents",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;doBlockEvent(Lnet/minecraft/world/level/BlockEventData;)Z"
			)
	)
	private boolean countBlockEventTicked(boolean tickOk, @Local BlockEventData blockEvent)
	{
		MetricCollector.getDimStats((ServerLevel)(Object)this).ifPresent(ds -> {
			ds.blockEvent.access(IdentifierUtils.of(blockEvent.block()), v -> {
				v.removed++;
				if (tickOk)
				{
					v.ticked++;
				}
			});
		});
		return tickOk;
	}
}
