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

package me.fallenbreath.tmcexporter.metric.collect.stats;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.fallenbreath.tmcexporter.metric.collect.stats.server.ServerStats;
import me.fallenbreath.tmcexporter.metric.common.Dimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class PerTickStats
{
	public final ServerStats server = new ServerStats();
	public final Map<Dimension, DimensionStats> dimensions = new Object2ObjectArrayMap<>();

	public PerTickStats(MinecraftServer server)
	{
		for (ServerWorld world : server.getWorlds())
		{
			this.dimensions.put(Dimension.of(world), new DimensionStats());
		}
	}
}
