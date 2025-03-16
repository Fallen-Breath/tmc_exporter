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

import me.fallenbreath.tmcexporter.metric.collect.MetricCollector;
import me.fallenbreath.tmcexporter.metric.collect.fake.ServerWorldAssociated;
import me.fallenbreath.tmcexporter.metric.common.TileTickKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldTickScheduler.class)
public abstract class ServerTickSchedulerMixin<T> implements ServerWorldAssociated
{
	@Unique
	private ServerWorld serverWorld$TMCE;

	@Override
	public void setServerWorld$TMCE(ServerWorld world)
	{
		this.serverWorld$TMCE = world;
	}

	@Override
	public ServerWorld getServerWorld$TMCE()
	{
		return this.serverWorld$TMCE;
	}

	@ModifyVariable(
			method = "tick(Ljava/util/function/BiConsumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"
			)
	)
	private OrderedTick<T> countTileTickTicked(OrderedTick<T> t)
	{
		MetricCollector.getDimStats(this.serverWorld$TMCE).ifPresent(ds -> {
			TileTickKey key = TileTickKey.ofTileTickObject(t.type());
			if (key != null)
			{
				ds.tileTick.access(key, v -> v.ticked++);
			}
		});
		return t;
	}
}
