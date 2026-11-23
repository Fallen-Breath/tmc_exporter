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

package me.fallenbreath.tmcexporter.mixins.metric.dimension.tiletick;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.metric.collect.fake.ServerLevelAssociated;
import me.fallenbreath.tmcexporter.metric.common.TileTickKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin<T> implements ServerLevelAssociated
{
	@Unique
	private ServerLevel serverLevel$TMCE;

	@Override
	public void setServerLevel$TMCE(ServerLevel world)
	{
		this.serverLevel$TMCE = world;
	}

	@Override
	public ServerLevel getServerLevel$TMCE()
	{
		return this.serverLevel$TMCE;
	}

	@ModifyVariable(
			method = "runCollectedTicks(Ljava/util/function/BiConsumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"
			)
	)
	private ScheduledTick<T> countTileTickTicked(ScheduledTick<T> t)
	{
		MetricCollector.getDimStats(this.serverLevel$TMCE).ifPresent(ds -> {
			TileTickKey key = TileTickKey.ofTileTickObject(t.type());
			if (key != null)
			{
				ds.tileTick.access(key, v -> v.ticked++);
			}
		});
		return t;
	}

	@Inject(
			method = "schedule",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/ticks/LevelChunkTicks;schedule(Lnet/minecraft/world/ticks/ScheduledTick;)V"
			)
	)
	private void startScheduleTileTickEvent(
			CallbackInfo ci,
			@Local LevelChunkTicks<T> chunkTicks,
			@Share("tickCountBeforeSchedule") LocalIntRef tickCountBeforeSchedule
	)
	{
		tickCountBeforeSchedule.set(chunkTicks.count());
	}

	@Inject(
			method = "schedule",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/ticks/LevelChunkTicks;schedule(Lnet/minecraft/world/ticks/ScheduledTick;)V",
					shift = At.Shift.AFTER
			)
	)
	private void endScheduleTileTickEvent(
			CallbackInfo ci,
			@Local(argsOnly = true) ScheduledTick<T> t,
			@Local LevelChunkTicks<T> chunkTicks,
			@Share("tickCountBeforeSchedule") LocalIntRef tickCountBeforeSchedule
	)
	{
		MetricCollector.getDimStats(this.serverLevel$TMCE).ifPresent(ds -> {
			TileTickKey key = TileTickKey.ofTileTickObject(t.type());
			if (key != null)
			{
				ds.tileTick.access(key, v -> {
					boolean success = chunkTicks.count() > tickCountBeforeSchedule.get();
					if (success)
					{
						v.schedule_succeeded++;
					}
					else
					{
						v.schedule_failed++;
					}
				});
			}
		});
	}
}
